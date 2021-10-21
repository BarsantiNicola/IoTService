package iot;

import java.io.Serializable;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//  Class used to generate the structure of the smarthome, in particular to define a sublocation.
//  A sublocation is a container for devices and has to be deployed into a location
@SuppressWarnings("unused")
public class SmarthomeLocation implements Serializable {

    private String locId;
    private String location;                           //  location name
    private String ipAddress;                          //  ip address used by the location
    private int port;                                  //  the port used by the location
    private int maxSublocID;
    private final HashMap<String,SmarthomeSublocation> sublocations = new HashMap<>();   //  list of all the sublocations
    private transient Logger logger;

    /////// CONSTRUCTORS

    public SmarthomeLocation() {
        initializeLogger();
    }

    public SmarthomeLocation(String location, String locID, String address, int port ){

        initializeLogger();
        this.location = location;
        this.ipAddress = address;
        this.port = port;
        this.locId = locID;
        this.maxSublocID = 1;
        this.sublocations.put("default",new SmarthomeSublocation("default", "0" ));

    }

    public SmarthomeLocation(String name, String locID, String address, int port, List<SmarthomeSublocation> sublocations){

        this( name, locID, address, port );
        sublocations.forEach(subLocation -> this.sublocations.put( subLocation.getSubLocation(), subLocation ));

    }

    /////// SETTERS

    public void setLocId(String locId) { this.locId = locId; }

    public void setLocation( String location ){
        this.location = location;
    }

    public void setIpAddress(String ip){
        this.ipAddress = ip;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void setMaxSublocID( int maxSublocID ){
        this.maxSublocID = maxSublocID;
    }

    public void setSublocations( HashMap<String, SmarthomeSublocation> sublocations ){
        this.sublocations.putAll(sublocations);
    }

    /////// GETTERS

    public String getLocId() {
        return locId;
    }

    public String getLocation(){ return location; }

    public String getIpAddress(){ return ipAddress; }

    public int getPort(){ return port; }

    public int getMaxSublocID() {
        return maxSublocID;
    }

    public HashMap<String, SmarthomeSublocation> getSublocations() {
        return sublocations;
    }

    /////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger(){

        if( this.logger != null )
            return;

        this.logger = Logger.getLogger(getClass().getName());
        if( logger.getHandlers().length == 0 ) {
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);
        }
    }

    /////// PUBLIC FUNCTIONS

    //// SUB-LOCATIONS

    //  returns the next sublocation ID
    public String giveNextSublocID() {
        return String.valueOf( this.maxSublocID++ );
    }

    //  adds a new sublocation into the location. It returns false if a sublocation with the given name is already present
    boolean addSublocation( String sublocation, String sublocID, boolean trial ){

        initializeLogger();

        //  verification that the requested sublocation isn't already present
        if( this.sublocations.containsKey( sublocation ))
            return false;

        if( !trial ) {
            this.sublocations.put( sublocation, new SmarthomeSublocation( sublocation, sublocID ));
            logger.info("New Sublocation " + sublocation + " correctly added to " + this.location);
        }

        return true;

    }

    //  if present it removes the sublocation from the location
    boolean removeSublocation( String subLocation, boolean trial ){

        if( trial )
            return this.sublocations.containsKey( subLocation );

        return this.sublocations.remove( subLocation ) != null;
    }

    //  changed the name of the subLocation. Returns true in case of success
    boolean changeSublocationName( String old_name, String new_name, boolean trial ){

        if( !this.sublocations.containsKey( old_name ) || this.sublocations.containsKey( new_name ))
            return false;

        if( !trial ) {

            SmarthomeSublocation subloc = this.sublocations.remove(old_name);
            subloc.setSubLocation(new_name);
            this.sublocations.put(new_name, subloc);
        }
        return true;

    }

    //// DEVICES

    //  add a new device into the given subLocation(if present). Returns true in case of success
    boolean addDevice( String sublocation, SmarthomeWebDevice device, boolean trial ){

        //  verification of the presence of the subLocation
        if( this.sublocations.containsKey( sublocation ))
            return this.sublocations.get( sublocation ).addDevice( device, trial );

        return false;

    }

    //  removes the device from the given subLocation. Returns true in case of success
    boolean removeDevice( String sublocation, String name, boolean trial ){

        //  verification of subLocation presence
        if( this.sublocations.containsKey( sublocation ))
            return this.sublocations.get( sublocation ).removeDevice( name, trial );

        return false;
    }

    //  changes the sublocation associated with the device. A device cannot be moved outside a location
    //  so if the location contains the old sublocation it can move the device to the new requested sublocation
    boolean changeDeviceSubLocation( String old_sublocation, String new_sublocation, String name, boolean trial ){

        initializeLogger();

        //  verification of the presence of both the subLocations
        if( !this.sublocations.containsKey( new_sublocation ) || !this.sublocations.containsKey( old_sublocation ))
            return false;

        //  getting the device from the old sub-location
        SmarthomeWebDevice device = this.sublocations.get( old_sublocation ).giveDevice( name );

        //  removing the device from the old sub-location
        if( this.sublocations.get( old_sublocation ).removeDevice( name, trial )){

            if( trial )
                return true;

            //  changing the device information to update the subLocation
            //  the update will also affect the devices array into the SmarthomeManager
            device.setRoomHint( new_sublocation );

            //  putting the device into the new subLocation
            this.sublocations.get( new_sublocation ).addDevice( device, false );
            logger.info( "Device " + name + "'s sublocation correctly changed from " + old_sublocation + " to " + new_sublocation );
            return true;
        }

        return false;

    }

    //  gives all the devices stored into the location
    List<SmarthomeWebDevice> giveDevices(){

        ArrayList<SmarthomeWebDevice> devs = new ArrayList<>();

        //  getting all the devices from all the subLocations
        this.sublocations.values().forEach( sublocation -> devs.addAll(sublocation.giveDevices()) );
        return devs;

    }

    //  if present gives all the devices stored into the subLocation deployed inside the current location
    List<SmarthomeWebDevice> giveDevices( String subLocation ){

        ArrayList<SmarthomeWebDevice> devs = new ArrayList<>();

        //  verification of subLocation presence
        if( this.sublocations.containsKey( subLocation ))
            devs.addAll( this.sublocations.get( subLocation ).giveDevices() );

        return devs;

    }

    //  generates a representation of the location and all its sublocations to be used by web clients
    HashMap<String,Object> buildSmarthomeLocation(){

        initializeLogger();

        HashMap<String,Object> location = new HashMap<>();
        ArrayList<HashMap<String,Object>> places = new ArrayList<>();

        Collection<SmarthomeSublocation> subLocs = this.sublocations.values();

        //  for every sublocation we generate its description
        subLocs.forEach( subLoc -> places.add( subLoc.buildSmarthomeSublocation() ));

        location.put( "location" , this.location );
        location.put( "sublocations" ,places );
        logger.info( "Generation of " + this.location + " description correctly done" );

        return location;
    }

    boolean isPresent( String subLocation ){
        return this.sublocations.containsKey(subLocation);
    }

}
