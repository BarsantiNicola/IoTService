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
public class SmarthomeSublocation implements Serializable {

    private String subLocation;              //  sublocation name
    private final String location;           //  location name
    private final transient Logger logger;

    private final List<SmarthomeWebDevice> devices = new ArrayList<>();  //  deployed devices

    ////////  TODO To be removed only for testing purpose
    private final static transient Random random = new SecureRandom();
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    public static List<SmarthomeSublocation> createTestingEnvironment(String location){

        List<SmarthomeSublocation> sublocations = new ArrayList<>();
        sublocations.add(new SmarthomeSublocation(location, "default"));
        int nSublocations = random.nextInt(2)+1;
        for( int a = 0;a<nSublocations; a++) {
            String name = createRandomString();
            sublocations.add(new SmarthomeSublocation(location, name, SmarthomeWebDevice.createTestingEnvironment(location, name)));
        }
        return sublocations;
    }
    ///////

    //////// CONSTRUCTORS

    SmarthomeSublocation(String location, String subLocation){
        this.location = location;
        this.subLocation = subLocation;
        logger = initializeLogger();
    }

    SmarthomeSublocation(String location, String subLocation, List<SmarthomeWebDevice> devices ){
        this(location, subLocation);
        this.devices.addAll(devices);
    }

    /////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private Logger initializeLogger(){

        Logger logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

        return logger;
    }

    //  PUBLIC FUNCTIONS

    //  returns the sublocation name
    public String getSubLocation(){
        return subLocation;
    }

    // changes the sublocation's name
    public void setSubLocation(String subLocation){

        this.subLocation = subLocation;
        for( SmarthomeWebDevice device: this.devices)
            device.setRoomHint(subLocation);
    }

    //  returns the list of all the sublocation's devices
    public List<SmarthomeWebDevice> getDevices(){ return devices; }

    //  returns a device if it is present into the sublocation's devices otherwise null
    public SmarthomeWebDevice getDevice(String dID){
        for(SmarthomeWebDevice dev: this.devices)
            if( dev.giveDeviceName().compareTo(dID) == 0 )
                return dev;
        return null;
    }

    //  changes the user assigned name of a device if it is present into the sublocation
    public boolean changeDeviceName(String old_name, String new_name) {
        logger.entering("changeDeviceName", "Searching the device " + old_name + " into sublocation " + this.subLocation);
        for (SmarthomeWebDevice dev : this.devices)
            if (dev.giveDeviceName().compareTo(old_name) == 0){
                dev.changeDeviceName(new_name);
                logger.exiting("changeDeviceName", "Device name found into " + this.subLocation + ". Name changed: " + old_name + " -> " + new_name);
                return true;
            }
        return false;
    }

    //  verifies if its present a device referred by the name assigned by the user
    public boolean devicePresent(String dID){
        for(SmarthomeWebDevice dev: this.devices)
            if(dev.giveDeviceName().compareTo(dID) == 0)
                return true;
        return false;
    }

    //  adds a new device to the sublocation. It returns false if a device with the given name is already present
    public boolean addDevice(String dID, String name, SmarthomeDevice.DeviceType type){
        logger.entering("addDevice", "Request to add a new device [" + dID + ":"+name+"] into " + this.subLocation);
        if(devicePresent(dID))
            return false;
        this.devices.add(new SmarthomeWebDevice(dID, name, location, subLocation, type));
        logger.exiting("addDevice", "Request to add a new device [" + dID + ":"+name+"] into " + this.subLocation +" correctly done");
        return true;
    }

    //  adds a new device to the sublocation. It returns false if a device with the given name is already present
    public boolean addDevice(SmarthomeWebDevice dev){
        logger.entering("addDevice", "Request to add a new device [" + dev.getId() + ":"+ dev.giveDeviceName()+"] into " + this.subLocation);
        if(devicePresent(dev.giveDeviceName()))
            return false;
        this.devices.add(dev);
        logger.exiting("addDevice", "Request to add a new device [" + dev.getId() + ":"+dev.giveDeviceName()+"] into " + this.subLocation +" correctly done");

        return true;
    }

    //  removes a new device from the sublocation
    public boolean removeDevice(String dID){
        for( SmarthomeWebDevice device: this.devices)
            if( device.giveDeviceName().compareTo(dID)==0) {
                this.devices.remove(device);
                logger.exiting("removeDevice", "Request to remove the device " + dID + " from " + this.subLocation + " correctly done");
                return true;
            }
        return false;
    }

    //  if the device is present into the sublocation it requires to perform the given action
    public boolean performAction(String dID, String action, String value){
        for(SmarthomeWebDevice dev: this.devices)
            if( dev.giveDeviceName().compareTo(dID) == 0 ) {
                logger.entering("performAction", "Request perform the action " + action + " on " + dID + " into " + this.subLocation);

                //  generating the hashmap containing the action
                HashMap<String,String> param = new HashMap<>();
                param.put("action", action);
                param.put("device_name" , dID);
                param.put( "value" , value);

                //  executing the command
                return dev.setParam(param);
            }
        return false;
    }

    //  generates a representation of the sublocation and all its devices to be used by web clients
    public HashMap<String,Object> buildSmarthomeSublocation(){

        HashMap<String, Object> result = new HashMap<>();
        ArrayList<HashMap<String,Object>> devs = new ArrayList<>();
        logger.entering("buildSmarthomeSublocation", "Generation of " + this.subLocation + " description");

        //  collect all the devices descriptions
        for( SmarthomeWebDevice device: this.devices)
            devs.add(device.buildSmarthomeWebDevice());

        //  generates the sublocation description
        result.put("sublocation" , this.subLocation);
        result.put( "devices" , devs);

        logger.exiting("buildSmarthomeSublocation", "Generation of " + this.subLocation + " description correctly done");
        return result;

    }
}
