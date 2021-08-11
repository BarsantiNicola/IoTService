package iot;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//  Class used to generate the structure of the smarthome, in particular to define a sublocation.
//  A sublocation is a container for devices and has to be deployed into a location

@SuppressWarnings("unused")
public class SmarthomeLocation implements Serializable {

    private final List<SmarthomeSublocation> sublocations = new ArrayList<>();   //  list of all the sublocations
    private String location;                           //  location name
    private String ipAddress;                          //  ip address used by the location
    private int port;                                  //  the port used by the location
    private final transient Logger logger;


    ////////  TODO To be removed only for testing purpose
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();
    private final static transient Random random = new SecureRandom();

    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    static List<SmarthomeLocation> createTestingEnvironment(){

        List<SmarthomeLocation> locations = new ArrayList<>();
        int nLocations = random.nextInt(3)+1;
        for( int a = 0;a<nLocations; a++) {
            String name = createRandomString();
            locations.add( new SmarthomeLocation(name, "8.8.8.8", 300, SmarthomeSublocation.createTestingEnvironment(name)));
        }
        return locations;
    }

    ////////

    /////// CONSTRUCTORS

    SmarthomeLocation(String location, String address, int port){

        this.location = location;
        this.ipAddress = address;
        this.port = port;
        logger = initializeLogger();

    }

    SmarthomeLocation(String name, String address, int port, List<SmarthomeSublocation> sublocations){

        this(name,address,port);
        this.sublocations.addAll(sublocations);

    }

    /////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private Logger initializeLogger(){
        Logger logger = Logger.getLogger(getClass().getName());
        if( logger.getHandlers().length == 0 ) {
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);
        }
        return logger;
    }

    /////// PUBLIC FUNCTIONS

    // returns the port used by the location component
    int givePort(){ return port; }

    //  changes the port used by the location component
    void changePort(int port){
        this.port = port;
    }

    //  gives the device if it is present in one of the sublocations deployed into the location
    SmarthomeWebDevice getDevice(String sublocation, String dID){
        for( SmarthomeSublocation subloc: this.sublocations)
            if(subloc.getSubLocation().compareTo(sublocation) == 0)
                return subloc.getDevice(dID);
        return null;
    }

    //  adds a new sublocation into the location. It returns false if a sublocation with the given name is already present
    boolean addSublocation(String sublocation){

        logger.entering( "addSublocation" , "Request of adding the sublocation " + sublocation + " into " + this.location);

        //  verification of presence of the sublocation
        if(sublocationPresent(sublocation)) {
            logger.severe( "Error sublocation already present into the smarthome. Abort operation");
            return false;
        }

        this.sublocations.add(new SmarthomeSublocation(this.location, sublocation));
        logger.exiting( "addSublocation" , "Request of adding the sublocation " + sublocation + " into " + this.location + " correctly done");
        return true;

    }

    //  if the location maintains the requested sublocation it will request to it to add a new device
    boolean addDevice(String sublocation, String dID, String name, SmarthomeDevice.DeviceType device_type){
        logger.entering( "addDevice", "Request to add a new device " + dID + " into the sublocation " + sublocation);
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(sublocation) == 0) {
                logger.exiting( "addDevice", "Sublocation " + sublocation + " found. Forward request to add device " + dID);
                return subloc.addDevice(dID, name, device_type);
            }
        return false;
    }

    //  if the location maintains the requested sublocation it will request to it to add a new device
    boolean addDevice(String sublocation, SmarthomeWebDevice device){
        logger.entering( "addDevice", "Request to add a new device " + device.getName() + " into the sublocation " + sublocation);
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(sublocation) == 0) {
                logger.exiting( "addDevice", "Sublocation " + sublocation + " found. Forward request to add device " + device.getName());
                return subloc.addDevice(device);
            }
        return false;
    }

    //  changes the sublocation associated with a device. A device cannot be moved outside a location
    //  so if the location contains the old sublocation it can move the device to the new requested sublocation
    boolean changeDeviceSublocation(String new_sublocation, String dID){

        //  verification of the presence of the new sublocation
        if(!sublocationPresent(new_sublocation)) {
            logger.severe("Error, requested sublocation " + new_sublocation + " not present into " + this.location);
            return false;
        }

        SmarthomeWebDevice device = null;
        String old_sublocation = null;

        //  searching for the current device sublocation
        for( SmarthomeSublocation subloc: this.sublocations) {
            old_sublocation = subloc.getSubLocation();
            device = getDevice(old_sublocation, dID);
            if (device != null)
                break;
        }

        if( device == null) {
            logger.severe("Error, requested device not present into the location " + this.location);
            return false;
        }

        //  removing the device from the sublocation
        removeDevice(dID);

        //  adding the device to the new location
        if(!addDevice(new_sublocation, device)) {
            //  in case of error we undo the operation
            logger.severe("Error during the device location change. Rollback of the operation");
            addDevice(old_sublocation, device);
            return false;
        }

        logger.exiting("changeDeviceSublocation", "Device " + dID + " sublocation update correctly done");
        return true;

    }

    //  if present it removes the sublocation from the location returning true
    boolean removeSublocation(String sublocation){
        logger.entering( "removeSublocation", "Request to remove the sublocation " + sublocation + " from the location " + this.location);

        for(SmarthomeSublocation subloc: this.sublocations)
            if(subloc.getSubLocation().compareTo(sublocation) == 0 ){
                this.sublocations.remove(subloc);
                logger.entering( "removeSublocation", "Request to remove the sublocation " + sublocation + " from the location " + this.location + " correctly done");
                return true;
            }
        return false;
    }

    //  if the device is present in one of the sublocation it will be removed
    boolean removeDevice(String dID){

        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.removeDevice(dID))
                return true;
        return false;
    }

    //  if the sublocation is present into the location it will be renamed
    boolean changeSublocationName(String old_name, String new_name){

        logger.entering( "removeSublocation", "Request to change the sublocation " + old_name + " name to " + new_name);

        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(old_name) == 0 ){
                subloc.setSubLocation(new_name);
                logger.entering( "removeSublocation", "Request to change the sublocation " + old_name + " name to " + new_name + " correctly done");
                return true;
            }
        return false;
    }

    //  verification of sublocation presence
    boolean sublocationPresent(String sublocation){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(sublocation) == 0 )
                return true;
        return false;
    }

    //  if the device is present in one of the sublocation it will be renamed
    boolean changeDeviceName(String old_name, String new_name){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.changeDeviceName(old_name, new_name))
                return true;
        return false;
    }

    //  verification of device presence
    boolean devicePresent(String dID){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.devicePresent(dID))
                return true;
        return false;
    }

    //  if the device is present in one of the sublocation it will execute a command
    public boolean performAction(String dID, String action, String value){
        for(SmarthomeSublocation subLoc: this.sublocations)
            if( subLoc.performAction(dID, action, value))
                return true;
        return false;
    }

    //  generates a representation of the location and all its sublocations to be used by web clients
    HashMap<String,Object> buildSmarthomeLocation(){

        logger.entering("buildSmarthomeSublocation", "Generation of " + this.location + " description");
        HashMap<String,Object> location = new HashMap<>();
        ArrayList<HashMap<String,Object>> sublocation = new ArrayList<>();

        //  for every sublocation we generate its description
        for(SmarthomeSublocation subloc: this.sublocations)
            sublocation.add(subloc.buildSmarthomeSublocation());

        location.put("location" , this.location);
        location.put("sublocations" ,sublocation );
        logger.exiting("buildSmarthomeSublocation", "Generation of " + this.location + " description correctly done");

        return location;
    }

    /////// SETTERS

    void setLocation(String location){
        this.location = location;
    }

    String setIpAddress(){ return ipAddress; }

    /////// GETTERS

    String getLocation(){ return location; }

    void getIpAddress(String ip){
        this.ipAddress = ip;
    }

    List<SmarthomeSublocation> getSublocations(){ return sublocations; }

}
