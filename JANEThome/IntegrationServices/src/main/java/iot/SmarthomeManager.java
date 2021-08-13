package iot;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//  Class used to manage a user's smarthome. It will be used as a container accessed by all the user's sessions.
//  The class will be used concurrently by many threads so it has to guarantee mutual exclusion access on its resources.
public class SmarthomeManager implements Serializable {

    private final String username;            //  username associated with the smarthome
    private final HashMap<String, SmarthomeLocation> locations;   //  locations of the smarthome
    private final HashMap<String, SmarthomeWebDevice> devices;    //  copy of all the devices for fast retrieval(optimization)
    private final Semaphore smartHomeMutex;   //  semaphore for mutual exclusion
    private transient Logger logger;

    ////////  TODO To be removed, only for testing purpose

    public static SmarthomeManager createTestingEnvironment(String username){
        return new SmarthomeManager(username, SmarthomeLocation.createTestingEnvironment());
    }

    ////////

    /////// CONSTRUCTORS

    public SmarthomeManager( String username ){

        this.username = username;
        this.locations = new HashMap<>();
        this.devices = new HashMap<>();
        this.smartHomeMutex = new Semaphore( 1 );
        initializeLogger();

    }

    public SmarthomeManager(String username, List<SmarthomeLocation> locs){

        this(username);
        locs.forEach( location -> {
            this.locations.put(location.getLocation(), location);
            location.getDevices().forEach( device -> this.devices.put( device.giveDeviceName(), device));
        });

    }

    //////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger(){

        if( logger != null )
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

    }

    //  obtains the mutual exclusion on the smarthome resources. Returns false in case of success(optimization)
    private boolean getSmartHomeMutex(){

        initializeLogger();
        try {

            this.smartHomeMutex.acquire();
            return false;

        }catch( InterruptedException e ){

            logger.severe( "Interruption occurred while the thread was waiting the mutex release. Abort operation" );
            e.printStackTrace();
            return true;

        }
    }

    //  releases the mutual exclusion from the smarthome resources
    private void releaseSmarthomeMutex(){
        this.smartHomeMutex.release();
    }


    /////// PUBLIC FUNCTIONS

    //// LOCATIONS

    //  adds a new location into the smarthome. Return false in case the location is already present
    public boolean addLocation( String location, String address, int port ){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  if the location is not already present we add it
        if( !this.locations.containsKey( location ) ) {

            this.locations.put( location, new SmarthomeLocation( location, address, port ));
            this.logger.info( "New location " + location + " added [" + address + "][" + port + "]" );
            result = true;

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    //  removes a location from the smarthome specified by its name. Returns true in case of success
    public boolean removeLocation( String location ){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification of location presence
        if( this.locations.containsKey( location )) {

            //  getting all the devices from the location(must be removed from the devices array too)
            Collection<SmarthomeWebDevice> devs = this.locations.get( location ).getDevices();

            result = this.locations.remove( location ) != null;
            if( result ) {  //  in case of success of location removal we drop all the device from the devices list

                devs.forEach( device -> this.devices.remove( device.giveDeviceName() ));
                this.logger.info( "Location " + location + " removed. Consequently removed " + devs.size() + " devices" );

            }
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    //  changes the location name. Returns true in case of success
    public boolean changeLocationName(String old_name, String new_name){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification of the old location presence and that the new name is not already assigned
        if( this.locations.containsKey(old_name) && !this.locations.containsKey(new_name) ){

            //  getting all the devices to update their information
            SmarthomeLocation location = this.locations.remove(old_name);
            Collection<SmarthomeWebDevice> devs = location.getDevices();
            location.setLocation(new_name);
            this.locations.put(new_name, location);
            //  updating the location name for the devices stored into the location
            devs.forEach(device -> device.setStructureHint(new_name));
            this.logger.info("Location name correctly changed from " + old_name + " to " + new_name +
                        ". Device information updated: " + devs.size());
            result = true;


        }

        this.releaseSmarthomeMutex();
        return result;

    }

    //// SUB-LOCATIONS

    //  adds a new sublocation into a defined location. Returns true in case of success
    public boolean addSubLocation( String location, String subLocation ){

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        // if the specified location is present we forward to it the request
        if( this.locations.containsKey( location ))
            result = this.locations.get( location ).addSublocation( subLocation );

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;
    }

    //  removes a subLocation from the specified location. Returns true in case of success
    public boolean removeSublocation( String location, String subLocation ){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification of subLocation presence
        if( this.locations.containsKey( location )) {

            //  getting all the devices from the subLocation(must be removed from the devices array too)
            Collection<SmarthomeWebDevice> devs = this.locations.get( location ).getDevices( subLocation );

            result = this.locations.get(location).removeSublocation(subLocation);
            if (result) { //  in case of success of subLocation removal we drop all the device from the devices list

                devs.forEach(device -> this.devices.remove(device.giveDeviceName()));
                this.logger.info("Sublocation " + subLocation + " of location " + location + " removed. " +
                        "Consequently removed " + devs.size() + " devices");

            }
        }

        this.releaseSmarthomeMutex();
        return result;
    }

    //  changes the sub-location name. Returns true in case of success
    public boolean changeSublocationName( String location, String old_name, String new_name ){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification of subLocation presence
        if( this.locations.containsKey( location )) {

            result = this.locations.get(location).changeSublocationName( old_name, new_name );
            //  we don't need to update the device information(done by the SmarthomeLocation.changeSublocationName)
            this.logger.info( "Device sub-location name correctly updated from " + old_name + " to " + new_name );

        }

        this.releaseSmarthomeMutex();
        return result;

    }

    //// DEVICES

    //  adds a new device info the specified location-sublocation. Returns true in case of success
    public boolean addDevice(String location, String sublocation, String dID, String name, SmarthomeDevice.DeviceType device_type){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification of location presence and that the device is not present
        if( !this.devices.containsKey(name) && this.locations.containsKey(location) ) {

            //  generation of device isntance from given information
            SmarthomeWebDevice device = new SmarthomeWebDevice( dID, name, location, sublocation, device_type );

            // adding the device to the smartHome structure
            result = this.locations.get(location).addDevice(sublocation, device);
            if(result) {  //  in case of success we add the device on the devices array for fast retrieval
                this.devices.put(name, device);
                this.logger.info( "New device " + name + " added to " + location + ":" + sublocation);
            }
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    //  removes a device specified by its user assigned name. Return true in case of success
    public boolean removeDevice( String name ){

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  if the device is present
        if( this.devices.containsKey(name)){

            //  getting the device to infer its location/sub-location
            SmarthomeWebDevice device = this.devices.get(name);

            //  verification of location presence and removal of device from smartHome structure
            if( this.locations.containsKey(device.getStructureHint())){

                result =  this.locations.get(device.getStructureHint()).removeDevice( device.getRoomHint(), name);
                if( result ) {

                    this.devices.remove(name);
                    this.logger.info("Device " + name + " correctly removed from subLocation " + device.getRoomHint() +
                            " of location " + device.getStructureHint());

                }

            }
        }

        this.releaseSmarthomeMutex();
        return result;

    }

    //  changes the sublocation in which the device is deployed. Returns true in case of success
    public boolean changeDeviceSubLocation( String location, String new_sublocation, String name ){

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification that the location and the device exists
        if( this.locations.containsKey( location ) && this.devices.containsKey( name )){

            //  getting the device to infer the current sub-location
            SmarthomeWebDevice device = this.devices.get( name );

            //  forward to the sub location the execution of the command
            result = this.locations.get( location ).changeDeviceSubLocation( device.getRoomHint(), new_sublocation, name );
            //  we don't need to update the device information(done by the SmarthomeLocation.changeDeviceSubLocation)
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;
    }

    // changes the device name. Returns true in case of success
    public boolean changeDeviceName(String old_name, String new_name){

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;

        //  verification that the device is present and don't exist a device with the new name assigned
        if( this.devices.containsKey( old_name ) && !this.devices.containsKey( new_name )){

            SmarthomeWebDevice device = this.devices.remove( old_name );
            device.changeDeviceName( new_name );
            this.devices.put( new_name, device );
            result = true;

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    //  executes a command on the specified device. In case of success the command is valid and executable
    public boolean performAction( String name, String action, String value ){

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = false;
        System.out.println("OK I'M HERE name: " + name + " action " + action + " value " + value );
        System.out.println("RESULT " + this.devices.containsKey( name ));
        if( this.devices.containsKey( name )) {
            HashMap<String,String> param = new HashMap<>();
            param.put( "device_name", name );
            param.put( "action", action );
            param.put( "value", value );
            System.out.println("APPLYING");
            try {
                result = this.devices.get(name).setParam(param);
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("ok " + result);
        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    //  verifies if the given device is present into the smartHome
    public boolean devicePresent( String name ){

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return false;

        boolean result = this.devices.containsKey( name );

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    //  generates a representation of the smarthome and all its locations to be used by web clients
    public String buildSmarthomeDefinition(){

        //  mutual exclusion on the interactions with the data structure
        if( this.getSmartHomeMutex() )
            return "";

        Gson gson = new Gson();
        ArrayList<HashMap<String,Object>> response = new ArrayList<>();
        this.locations.values().forEach( location -> response.add(location.buildSmarthomeLocation()));

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return gson.toJson( response );

    }

    ////// GETTERS

    public String getUsername(){ return this.username; }

    public Collection<SmarthomeLocation> getLocations() {
        return this.locations.values();
    }

}
