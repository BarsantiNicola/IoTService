package iot;

//  internal services
import rabbit.in.SmarthomeUpdater;
import config.interfaces.IConfiguration;

//  database management
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import db.model.MongoEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

//  utils
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Semaphore;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class developed to manage a user's smarthome. It will be used as a container accessed by all the user's sessions
 * The class will be used concurrently by many threads so it has to guarantee mutual exclusion access on its resources
 */
@Entity(value = "HomeManager", noClassnameStored = true)
public class SmarthomeManager extends MongoEntity implements Serializable {

    @Expose
    private String username = "";            //  username associated with the smarthome

    @Expose
    @Embedded
    private HashMap<String, SmarthomeLocation> locations = new HashMap<>();   //  locations of the smarthome

    @Expose
    @Embedded
    private HashMap<String, SmarthomeWebDevice> devices = new HashMap<>();    //  copy of all the devices for fast retrieval(optimization)

    @Transient
    private Semaphore smartHomeMutex;   //  semaphore for mutual exclusion

    @Transient
    private transient Logger logger;

    @Transient
    @SuppressWarnings("unused")
    private SmarthomeUpdater updater;  //  automatic updater of the class connected with rabbitMQ

    public SmarthomeManager() {

        this.smartHomeMutex = new Semaphore( 1 );
        this.setKey( new ObjectId() );

    }

    public SmarthomeManager( String username, boolean connected, IConfiguration configuration ){

        this.setKey( new ObjectId() );
        this.username = username;
        this.locations = new HashMap<>();
        this.devices = new HashMap<>();
        this.smartHomeMutex = new Semaphore( 1 );
        this.logger = LogManager.getLogger( getClass().getName() );

        if (connected)
            this.updater = new SmarthomeUpdater( username, this, configuration );

    }

    public SmarthomeManager( String username, boolean connected, IConfiguration configuration, List<SmarthomeLocation> locs ) {

        this( username, connected, configuration );
        this.setKey( new ObjectId() );
        locs.forEach( location -> {

            this.locations.put( location.getLocation(), location );
            location.giveDevices().forEach( device -> this.devices.put( device.giveDeviceName(), device ));

        });

    }


    ////////--  SETTERS  --////////


    public void setUsername( String username ){
        this.username = username;
    }

    public void setLocations( List<SmarthomeLocation> locs ){

        locs.forEach( location -> {
            this.locations.put( location.getLocation(), location );
            location.giveDevices().forEach( device -> this.devices.put( device.giveDeviceName(), device ));
        });
    }

    public void setDevices( List<SmarthomeWebDevice> devs ){
        this.devices = new HashMap<>();
        devs.forEach( device -> this.devices.put( device.giveDeviceName(), device ));
    }


    ////////--  GETTERS  --////////


    public String getUsername() {
        return this.username;
    }

    public HashMap<String, SmarthomeLocation> getLocations(){
        return this.locations;
    }

    public HashMap<String,SmarthomeLocation> getDevices() {
        return new HashMap<>();
    } //  patch for mongoDB to work

    public Collection<SmarthomeLocation> giveLocations() {
        return this.locations.values();
    }

    public HashMap<String, SmarthomeWebDevice> giveDevices() { return devices; }  //  needed to not collide with mongoDB


    ////////--  LOCATIONS MANAGEMENT  --////////


    /**
     * Adds a new location into the smartHome
     * @param location The name of the location
     * @param locID    Unique identifier of the location
     * @param address  Destination address of the controller managing the location
     * @param port     Destination port of the controller managing the location
     * @param trial    if true the method only test the applicability of the request without any modification
     * @return Returns true in case of success otherwise false
     */
    public boolean addLocation( String location, String locID, String address, int port, boolean trial ) {

        this.logger = LogManager.getLogger( getClass().getName() );

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if ( !trial && this.locations.containsKey( location )){ //  the location is present -> operation already done

            this.releaseSmarthomeMutex();
            return true;

        }

        //  the request satisfies the pre-condition(location not present, address:port not used)
        if( this.verifyAddressUnivocity( address, port ) && !this.locations.containsKey( location )) {

            if( !trial ){  //  if not a trial we apply the changes

                this.locations.put( location, new SmarthomeLocation( location, locID, address, port ));
                this.logger.info( "New location " + location + " added [" + address + "][" + port + "]" );

            }
            result = true;

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    /**
     * Removes a location from the smarthome specified by its name
     * @param location Name of the location to be removed
     * @param trial    if true the method only test the applicability of the request without any modification
     * @return         Returns true in case of success otherwise false
     */
    public boolean removeLocation( String location, boolean trial ) {

        this.logger = LogManager.getLogger( getClass().getName() );

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if( !trial && !this.locations.containsKey( location )){ //  location not present -> already removed

            this.releaseSmarthomeMutex();
            return true;

        }

        //  the request satisfies the pre-condition(location present)
        if( this.locations.containsKey( location )){

            if( !trial ){  //  if not a trial we apply the changes

                //  getting all the devices from the location(must be removed too)
                Collection<SmarthomeWebDevice> devs = this.locations.get( location ).giveDevices();

                result = this.locations.remove( location ) != null;
                if( result ) {  //  in case of success of location removal we drop all the device from the devices list

                    devs.forEach( device -> this.devices.remove( device.giveDeviceName() ));
                    this.logger.info( "Location " + location + " removed. Consequently removed " + devs.size() + " devices" );

                }

            } else
                result = true;

        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    /**
     * Changes the location name
     * @param old_name Current name of the location
     * @param new_name New name to add to the location
     * @param trial    if true the method only test the applicability of the request without any modification
     * @return         Returns true in case of success otherwise false
     */
    public boolean changeLocationName( String old_name, String new_name, boolean trial ){

        this.logger = LogManager.getLogger( getClass().getName() );

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if( !trial && !this.locations.containsKey( old_name ) && this.locations.containsKey( new_name )){
            //  old name missing & new name present -> change already done

            this.releaseSmarthomeMutex();
            return true;

        }

        //  the request satisfies the pre-condition(old name present & new name not present)
        if( this.locations.containsKey( old_name ) && !this.locations.containsKey( new_name )){

            if( !trial ) {  //  if not a trial we apply the changes

                SmarthomeLocation location = this.locations.remove( old_name );

                //  getting all the devices to update their location information
                Collection<SmarthomeWebDevice> devs = location.giveDevices();

                //  updating location name
                location.setLocation( new_name );
                this.locations.put( new_name, location );

                //  updating the location name for the devices stored into the location
                devs.forEach( device -> device.setStructureHint( new_name ));
                this.logger.info("Location name correctly changed from " + old_name + " to " + new_name +
                        ". Device information updated: " + devs.size() );

            }
            result = true;

        }

        this.releaseSmarthomeMutex();
        return result;

    }


    ////////--  SUB-LOCATIONS MANAGEMENT  --////////


    /**
     * Adds a new sublocation into a defined location
     * @param location Location name in which deploy the subLocation
     * @param subLocation Sublocation name
     * @param sublocID    Unique sublocation identificator
     * @param trial       if true the method only test the applicability of the request without any modification
     * @return            Returns true in case of success otherwise false
     */
    public boolean addSubLocation( String location, String subLocation, String sublocID, boolean trial ) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        // if the specified location is present we forward to it the request
        if( this.locations.containsKey( location )) {

            //  a request on the same smarthome(shared between websockets) will be done many times
            //  if the request results already applied(destination status already reached) then we don't need to do it
            //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
            //  only one time)
            if( !trial && this.locations.get( location ).isPresent( subLocation )){ //  subLocation already present -> request already done

                this.releaseSmarthomeMutex();
                return true;

            }
            result = this.locations.get( location ).addSublocation( subLocation, sublocID, trial );  //  forward the request to the subLocation

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    /**
     * Removes a subLocation from the specified location
     * @param location    Name of the location in which the subLocation is deployed
     * @param subLocation Name of the subLocation to remove
     * @param trial       if true the method only test the applicability of the request without any modification
     * @return            Returns true in case of success otherwise false
     */
    public boolean removeSublocation( String location, String subLocation, boolean trial ) {

        if( subLocation.compareToIgnoreCase("default") == 0 )
            return false;

        this.logger = LogManager.getLogger( getClass().getName() );

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification of location presence
        if( this.locations.containsKey( location )){

            //  a request on the same smarthome(shared between websockets) will be done many times
            //  if the request results already applied(destination status already reached) then we don't need to do it
            //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
            //  only one time)
            if ( !trial && !this.locations.get( location ).isPresent( subLocation )) {

                this.releaseSmarthomeMutex();
                return true;

            }

            //  getting all the devices from the subLocation(must be moved from to the default subLocation)
            Collection<SmarthomeWebDevice> devs = this.locations.get( location ).giveDevices( subLocation );

            result = this.locations.get( location ).removeSublocation( subLocation, trial );  //  forwarding the request to the subLocation
            if( result && !trial ) { //  in case of success of subLocation removal we move all the device into the default sublocation

                devs.forEach( device -> {
                    device.setRoomHint( "default" );
                    this.locations.get( location ).addDevice( "default", device, false );
                });

                this.logger.info( "Sublocation " + subLocation + " of location " + location + " removed. " +
                        "Consequently moved " + devs.size() + " devices into default sublocation" );

            }
        }

        this.releaseSmarthomeMutex();
        return result;
    }

    /**
     * Changes the sub-location name
     * @param location    Name of the location in which the subLocation is deployed
     * @param old_name    Current name of the subLocation
     * @param new_name    New name for the subLocation
     * @param trial       if true the method only test the applicability of the request without any modification
     * @return            Returns true in case of success otherwise false
     */
    public boolean changeSublocationName(String location, String old_name, String new_name, boolean trial) {

        this.logger = LogManager.getLogger( getClass().getName() );
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  verification of subLocation presence
        if (this.locations.containsKey(location)) {

            //  a request on the same smarthome(shared between websockets) will be done many times
            //  if the request results already applied(destination status already reached) then we don't need to do it
            //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
            //  only one time)
            if (!trial && !this.locations.get(location).isPresent(old_name) && this.locations.get(location).isPresent(new_name)) {

                this.releaseSmarthomeMutex();
                return true;

            }

            result = this.locations.get(location).changeSublocationName(old_name, new_name, trial);
            //  we don't need to update the device information(done by the SmarthomeLocation.changeSublocationName)
            if( !trial )
                this.logger.info("Device sub-location name correctly updated from " + old_name + " to " + new_name);

        }

        this.releaseSmarthomeMutex();
        return result;

    }


    ////////--  DEVICES MANAGEMENT  --////////


    /**
     * Adds a new device into the specified location-sublocation
     * @param location    Location name in which deploy the device
     * @param sublocation SubLocation name in which deploy the device
     * @param dID         Unique Device Identifier(needed by Riccardo)
     * @param name        Name of the device
     * @param device_type {@link DeviceType} Type of the device
     * @param trial       if true the method only test the applicability of the request without any modification
     * @return            Returns true in case of success otherwise false
     */
    public boolean addDevice( String location, String sublocation, String dID, String name, DeviceType device_type, boolean trial ) {

        this.logger = LogManager.getLogger( getClass().getName() );

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;
        SmarthomeWebDevice device = this.devices.get( name );

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if( !trial && device != null &&
                device.getStructureHint().compareTo( location ) == 0
                    && device.getRoomHint().compareTo( sublocation ) == 0 ) {  //  device found into the same loc:subLoc -> request already done

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification of location presence and that the device is not present
        if( device == null && this.locations.containsKey( location )) {

            //  generation of a device instance from the given information
            device = new SmarthomeWebDevice( dID, name, location, sublocation, device_type );

            // adding the device to the smartHome structure
            result = this.locations.get( location ).addDevice( sublocation, device, trial );

            if( result && !trial ) {  //  in case of success we add the device on the devices array for fast retrieval

                this.devices.put( name, device );
                this.logger.info( "New device " + name + " added to " + location + ":" + sublocation );

            }
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }


    /**
     * Removes a device specified by its user assigned name
     * @param name        Name of the device to remove
     * @param trial       if true the method only test the applicability of the request without any modification
     * @return            Returns true in case of success otherwise false
     */
    public boolean removeDevice( String name, boolean trial ) {

        this.logger = LogManager.getLogger( getClass().getName() );

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if( !trial && !this.devices.containsKey( name )) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  if the device is present
        if( this.devices.containsKey( name )) {

            //  getting the device to infer its location/sub-location
            SmarthomeWebDevice device = this.devices.get( name );

            //  verification of location presence and removal of device from smartHome structure
            if( this.locations.containsKey( device.getStructureHint() )) {

                result = this.locations.get( device.getStructureHint() ).removeDevice( device.getRoomHint(), name, trial );

                if (result && !trial) {

                    this.devices.remove( name );
                    this.logger.info( "Device " + name + " correctly removed from subLocation " + device.getRoomHint() +
                            " of location " + device.getStructureHint() );

                }

            }
        }

        this.releaseSmarthomeMutex();
        return result;

    }

    /**
     * Changes the sublocation in which the device is deployed
     * @param name            Name of the location in which the device is deployed
     * @param new_sublocation Name of the subLocation to which move the device
     * @param trial           if true the method only test the applicability of the request without any modification
     * @return                Returns true in case of success otherwise false
     */
    public boolean changeDeviceSubLocation( String location, String new_sublocation, String name, boolean trial ) {

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;
        SmarthomeWebDevice device = this.devices.get( name );

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if( !trial && device != null &&
                device.getStructureHint().compareTo( location ) == 0 &&
                    device.getRoomHint().compareTo( new_sublocation ) == 0 ){ //  device present & loc:subLoc match -> request already done

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification that the location and the device exists
        if( this.locations.containsKey( location ) && device != null ){

            //  forward to the sub location the execution of the command
            result = this.locations.get( location ).changeDeviceSubLocation( device.getRoomHint(), new_sublocation, name, trial );
            //  we don't need to update the device information(done by the SmarthomeLocation.changeDeviceSubLocation)

        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;
    }


    /**
     * Changes the device name
     * @param old_name Current name of the device
     * @param new_name New name for the device
     * @param trial    if true the method only test the applicability of the request without any modification
     * @return         Returns true in case of success otherwise false
     */
    public boolean changeDeviceName(String old_name, String new_name, boolean trial) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) will be done many times
        //  if the request results already applied(destination status already reached) then we don't need to do it
        //  This cannot happen in case of testing the request(this is done only for the user requests and they are processed
        //  only one time)
        if ( !trial && !this.devices.containsKey( old_name ) && this.devices.containsKey( new_name )) {
            //  device(old name) not present & device(new name) present -> request already done

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification that the device is present and don't exist a device with the new name assigned
        if( this.devices.containsKey( old_name ) && !this.devices.containsKey( new_name )){

            result = true;

            if( !trial ){  //  if not a trial we apply the changes

                SmarthomeWebDevice device = this.devices.remove( old_name );  //  removes the device from the list of devices

                //  removing the device from the smartHome
                if( this.locations.get( device.getStructureHint() ).removeDevice( device.getRoomHint(), device.giveDeviceName(), false )) {

                    device.changeDeviceName( new_name );  //  changing the device name

                    //  re-adding the device to the subLocation
                    this.locations.get( device.getStructureHint()).addDevice( device.getRoomHint(), device, false );

                    //  re-adding the device to the list of devices
                    this.devices.put( new_name, device );

                } else
                    result = false;
            }

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    /**
     * Executes a command on the specified device
     * @param name      Name of the device in which execute the action
     * @param action    Name of the action according the google home traits
     * @param value     Stringed value of the action
     * @param timestamp Timestamp of the real execution of the action
     * @param trial     if true the method only test the applicability of the request without any modification
     * @return          Returns true in case of success otherwise false
     */
    public boolean performAction( String name, String action, String value, Date timestamp, boolean trial ) {

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = false;

        if( this.devices.containsKey( name )) {  //  verification of the device presence

            try{

                //  forwarding the request to the device
                result = this.devices.get( name )
                            .executeAction(
                                    action,
                                    value,
                                    trial? new Date( System.currentTimeMillis() ) : timestamp,
                                    trial,
                                    false );

            }catch( Exception e ) {

                e.printStackTrace();
            }
        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }


    ////////--  UTILITIES  --////////


    /**
     * Patch for mongoDB which not maintains relative links between devices and smarthome into storage.
     * Needs to be applied after getting a smarthome from the database to regenerate the references
     */
    public void relink(){

        this.devices.clear();
        this.locations.values().forEach(
                location -> location.giveDevices().forEach(
                        device -> this.devices.put( device.giveDeviceName(), device )));

    }

    /**
     * Patch for mongoDB which not take much care on given smarthome. Manually force automatic updates on smartHome
     * Needs to be applied after getting a smarthome from the database for WebServer.package usage
     */
    public void connect( IConfiguration configuration ){

        this.updater = new SmarthomeUpdater( username, this, configuration )
        ;
    }

    /**
     * Patch for mongoDB, regenerate the semaphore of operation mutual exclusion
     * Needs to be applied after getting a smarthome from the database for WebServer.package usage
     */
    public void addSmartHomeMutex( Semaphore smartHomeMutex ) {

        this.smartHomeMutex = smartHomeMutex;

    }
    /**
     * Releases the mutual exclusion from the smarthome resources
     */
    private void releaseSmarthomeMutex() {

        this.smartHomeMutex.release();

    }

    /**
     * Obtains the mutual exclusion on the smarthome resources
     * @return False in case of success otherwise true(optimization on return usage)
     */
    private boolean giveSmartHomeMutex() {

        this.logger = LogManager.getLogger( getClass().getName() );
        try{

            this.smartHomeMutex.acquire();
            return false;

        }catch( InterruptedException e ) {

            logger.error( "Interruption occurred while the thread was waiting the mutex release. Abort operation" );
            e.printStackTrace();
            return true;

        }
    }

    /**
     * Verifies that the given address:port is not already used into the smarthome
     */
    private boolean verifyAddressUnivocity( String address, int port ){

        for( SmarthomeLocation loc : this.locations.values() )
            if( loc.getIpAddress().compareTo(address) == 0 && loc.getPort() == port )
                return false;

        return true;

    }

    /**
     * Returns all the devices of a subLocation
     * @param location    Name of the location in which the subLocation is deployed
     * @param sublocation Name of the subLocation
     * @return A list of {@link SmarthomeWebDevice}
     */
    public List<SmarthomeWebDevice> giveSublocationDevices( String location, String sublocation ){

        List<SmarthomeWebDevice> devs = new ArrayList<>();

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return devs;

        this.devices.values().forEach( device -> {
            if( device.getStructureHint().compareTo( location ) == 0 && device.getRoomHint().compareTo( sublocation ) == 0 )
                devs.add( device );
        });

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return devs;
    }

    /**
     * Verifies if the given device is present into the smartHome
     * @param name Name of the device to test
     * @return True if the device is present otherwise false
     */
    public boolean devicePresent( String name ) {

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return false;

        boolean result = this.devices.containsKey( name );

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    /**
     * Returns the network information of a device
     * @param name Name of the device
     * @return Returns a string formatted as "IP:port" or null
     */
    public String giveDeviceNetwork( String name ) {

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return null;

        String result = null;

        if ( this.devices.containsKey( name )){

            //  we search the location in which the device is deployed
            String location = this.devices.get( name ).getStructureHint();
            if( this.locations.containsKey( location )){

                //  extracting from the location all the network information
                SmarthomeLocation loc = this.locations.get( location );
                result = loc.getIpAddress() + ":" + loc.getPort();

            }
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    /**
     * Returns the network information of a location
     * @param location Name of the location to search
     * @return Returns the network information as "IP:port" or null
     */
    public String giveLocationNetwork( String location ) {

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return null;


        String result = null;

        if( this.locations.containsKey( location )){

            SmarthomeLocation loc = this.locations.get( location );
            result = loc.getIpAddress() + ":" + loc.getPort();

        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;

    }

    public String giveDeviceIdByName( String name ){

        String result = "";
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        if (this.devices.containsKey(name))
            result = this.devices.get(name).getId();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    /**
     * Returns the device name giving its dID
     * @param dID unique identifier of the device
     * @return Returns its name or an empty string
     */
    public String giveDeviceNameById( String dID ) {

        String result = "";

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return result;

        for( SmarthomeWebDevice device : this.devices.values() )
            if( device.getId().compareTo(dID) == 0 )
                result = device.giveDeviceName();


        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;

    }

    /**
     * Returns the device subLocation givin its name
     * @param name The name of the device
     * @return Returns the subLocation or an empty string
     */
    public String giveDeviceSubLocation( String name ) {

        String result = "";

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return result;

        if( this.devices.containsKey( name ))
            result = this.devices.get( name ).getRoomHint();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    /**
     * Returns the locationID giving the location name
     * @param locName Name of the location
     * @return The Location Identifier or an empty string
     */
    public String giveLocIdByName( String locName ) {

        String result = "";

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return result;

        if( this.locations.containsKey( locName ))
            result = this.locations.get( locName ).getLocId();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    /**
     * Gives the next sublocation ID for a given location
     * @param locName Location name from which take the subID
     * @return Returns the subID or an empty string
     */
    public String giveNextSublocID( String locName ) {

        String result = "";

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return result;

        if( this.locations.containsKey( locName ))
            result = this.locations.get( locName ).giveNextSublocID();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;

    }

    /**
     * Gives the device type of a given device
     * @param name Name of the device
     * @return Its {@link DeviceType} if present or DeviceType.UNKNOWN
     */
    public DeviceType giveDeviceTypeByName( String name ){

        DeviceType type = DeviceType.UNKNOWN;
        if( this.giveSmartHomeMutex()  )
            return type;

        if( this.devices.containsKey( name ))
            type = DeviceType.StringToType( this.devices.get( name ).getType() );

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return type;

    }

    /**
     * Gives the subLocationID giving the subLocation name
     * @param locName Location name in which the subLocation is deployed
     * @param subLocName SubLocation name
     * @return Returns its sublocID or an empty string
     */
    public String giveSubLocIdByName( String locName, String subLocName ) {

        String result = "";

        //  mutual exclusion on the interactions with the data structure
        if( this.giveSmartHomeMutex() )
            return result;

        if( this.locations.containsKey( locName ) && this.locations.get( locName ).getSublocations().containsKey( subLocName ))
            result = this.locations.get( locName ).getSublocations().get( subLocName ).getSubLocId();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    /**
     * Generates a representation of the smarthome and all its locations to be used by web clients
     * @return A stringed description of the smartHome to be given to webClients
     */
    public String buildSmarthomeDefinition() {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return "";

        ArrayList<HashMap<String, Object>> response = new ArrayList<>();
        this.locations.values().forEach( location -> response.add( location.buildSmarthomeLocation() ));

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return new Gson().toJson(response);

    }
}
