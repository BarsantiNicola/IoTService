package iot;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SmarthomeManager implements Serializable {

    private final String username;
    private final List<SmarthomeLocation> locations;
    private final Semaphore mutex;
    private final Logger logger;

    ////////  TODO To be removed, only for testing purpose

    public static SmarthomeManager createTestingEnvironment(String username){
        return new SmarthomeManager(username, SmarthomeLocation.createTestingEnvironment());
    }

    ////////

    /////// CONSTRUCTORS

    public SmarthomeManager(String username){
        this.username = username;
        locations = new ArrayList<>();
        mutex = new Semaphore(1 );
        logger = initializeLogger();
    }

    public SmarthomeManager(String username, List<SmarthomeLocation> locations){
        this(username);
        this.locations.addAll(locations);
    }

    //////// UTILITY FUNCTIONS

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

    //  adds a new location into the smarthome. Return false in case the location is already present
    public boolean addLocation(String location, String address, int port){

        if(locationPresent(location))  //  get mutual exclusion, to not be use in a mutual exclusion context
            return false;

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        this.locations.add(new SmarthomeLocation(location,address,port));
        mutex.release();  //  release of mutual exclusion
        return true;
    }

    //  adds a new sublocation into a defined location
    public boolean addSubLocation(String location, String subLocation){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for( SmarthomeLocation loc: this.locations) {
            if (loc.getLocation().compareTo(location) == 0) {
                boolean result = loc.addSublocation(subLocation);
                mutex.release();
                return result;
            }
        }
        mutex.release();  //  release of mutual exclusion
        return false;
    }

    //  adds a new device info the specified location-sublocation
    public boolean addDevice(String location, String sublocation, String dID, String name, SmarthomeDevice.DeviceType device_type){

        if(devicePresent(dID)) //  get mutual exclusion, to not be use in a mutual exclusion context
            return false;

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for( SmarthomeLocation loc: this.locations)
            if(loc.getLocation().compareTo(location) == 0 ) {
                boolean result = loc.addDevice(sublocation, dID, name, device_type);
                mutex.release();  //  release of mutual exclusion
                return result;
            }
        mutex.release();  //  release of mutual exclusion
        return false;
    }

    //  removes a location. It will return false in case it not exists
    public boolean removeLocation(String location){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for( SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location)==0) {
                this.locations.remove(loc);
                mutex.release();  //  release of mutual exclusion
                return true;
            }

        mutex.release();  //  release of mutual exclusion
        return false;
    }

    //  adds a sublocation into a defined location
    public boolean removeSublocation(String location, String sublocation){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for( SmarthomeLocation loc: this.locations)
            if(loc.getLocation().compareTo(location)==0) {
                boolean result = loc.removeSublocation(sublocation);
                mutex.release();  //  release of mutual exclusion
                return result;
            }
        mutex.release();  //  release of mutual exclusion
        return false;
    }

    //  if present it will remove the device
    public boolean removeDevice(String dID){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for( SmarthomeLocation loc: this.locations)
            if( loc.removeDevice(dID)) {
                mutex.release();  //  release of mutual exclusion
                return true;
            }

        mutex.release();  //  release of mutual exclusion
        return false;

    }

    public boolean changeDeviceSublocation(String location, String new_sublocation, String dID){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for( SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location) == 0) {
                boolean result = loc.changeDeviceSublocation(new_sublocation, dID);
                mutex.release();  //  release of mutual exclusion
                return result;
            }
        mutex.release();  //  release of mutual exclusion
        return false;
    }

    public String getUsername(){ return username; }

    public List<SmarthomeLocation> getLocations() {
        return locations;
    }

    public boolean changeLocationName(String old_name, String new_name){

        if( locationPresent(new_name))  //  get mutual exclusion, to not be use in a mutual exclusion context
            return false;

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(old_name)==0) {
                loc.setLocation(new_name);
                mutex.release();  //  release of mutual exclusion
                return true;
            }
        mutex.release();  //  release of mutual exclusion
        return false;

    }

    public boolean locationPresent(String location){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location)==0) {
                mutex.release();  //  release of mutual exclusion
                return true;
            }

        mutex.release();  //  release of mutual exclusion
        return false;
    }

    public boolean changeSublocationName(String location, String old_name, String new_name){

        if(sublocationPresent(new_name))   //  get mutual exclusion, to not be use in a mutual exclusion context
            return false;

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location) == 0) {
                mutex.release();  //  release of mutual exclusion
                return loc.changeSublocationName(old_name, new_name);
            }
        mutex.release();  //  release of mutual exclusion
        return false;

    }

    public boolean sublocationPresent(String sublocation){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if( loc.sublocationPresent(sublocation)) {
                mutex.release();  //  release of mutual exclusion
                return true;
            }

        mutex.release();  //  release of mutual exclusion
        return false;
    }

    public boolean changeDeviceName(String old_name, String new_name){

        if(devicePresent(new_name))   //  get mutual exclusion, to not be use in a mutual exclusion context
            return false;

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if( loc.changeDeviceName(old_name, new_name)) {
                mutex.release();  //  release of mutual exclusion
                return true;
            }
        mutex.release();  //  release of mutual exclusion
        return false;
    }

    public boolean devicePresent(String dID){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if(loc.devicePresent(dID)) {
                mutex.release();  //  release of mutual exclusion
                return true;
            }
        mutex.release();  //  release of mutual exclusion
        return false;
    }

    public boolean performAction(String dID, String action, String value){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return false;
        }

        for(SmarthomeLocation loc: this.locations)
            if(loc.performAction(dID, action, value)) {
                mutex.release();  //  release of mutual exclusion
                return true;
            }
        mutex.release();   //  release of mutual exclusion
        return false;
    }

    //  generates a representation of the smarthome and all its locations to be used by web clients
    public String buildSmarthomeDefinition(){

        //  mutual exclusion on the interactions with the data structure
        try {
            mutex.acquire();
        }catch(InterruptedException e){
            e.printStackTrace();
            return "";
        }

        ArrayList<HashMap<String,Object>> response = new ArrayList<>();
        //  getting a description of all the locations
        for(SmarthomeLocation loc: this.locations)
            response.add(loc.buildSmarthomeLocation());

        mutex.release();    //  release of mutual exclusion

        Gson gson = new Gson();
        return gson.toJson(response);

    }
}
