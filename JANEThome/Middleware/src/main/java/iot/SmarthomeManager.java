package iot;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import config.interfaces.ConfigurationInterface;
import db.model.MongoEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import rabbit.in.SmarthomeUpdater;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//  Class used to manage a user's smarthome. It will be used as a container accessed by all the user's sessions.
//  The class will be used concurrently by many threads so it has to guarantee mutual exclusion access on its resources.
@SuppressWarnings("all")
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
    private Semaphore smartHomeMutex = null;   //  semaphore for mutual exclusion
    @Transient
    private transient Logger logger;
    @Transient
    private SmarthomeUpdater updater;

    /////// CONSTRUCTORS

    public SmarthomeManager() {
        this.smartHomeMutex = new Semaphore( 1 );
        this.setKey(new ObjectId());
    }

    public SmarthomeManager(String username, boolean connected, ConfigurationInterface configuration) {
        this.setKey(new ObjectId());
        this.username = username;
        this.locations = new HashMap<>();
        this.devices = new HashMap<>();
        this.smartHomeMutex = new Semaphore(1);
        this.initializeLogger();
        if (connected)
            this.updater = new SmarthomeUpdater(username, this, configuration);

    }

    public SmarthomeManager(String username, boolean connected, ConfigurationInterface configuration, List<SmarthomeLocation> locs) {

        this(username, connected, configuration);
        this.setKey(new ObjectId());
        locs.forEach(location -> {
            this.locations.put(location.getLocation(), location);
            location.giveDevices().forEach(device -> this.devices.put(device.giveDeviceName(), device));
        });

    }

    ////// SETTERS

    public void setUsername( String username ){
        this.username = username;
    }

    public void setLocations( List<SmarthomeLocation> locs ){
        locs.forEach(location -> {
            this.locations.put(location.getLocation(), location);
            location.giveDevices().forEach(device -> this.devices.put(device.giveDeviceName(), device));
        });
    }

    public void setDevices( List<SmarthomeDevice> devices ){}


    ////// GETTERS

    public String getUsername() {
        return this.username;
    }

    public HashMap<String, SmarthomeLocation> getLocations(){
        return this.locations;
    }

    //  getter used by the mongodb database.
    //  To prevent replication of device information into the database we give back an empty hashmap(see giveDevices)
    public HashMap<String,SmarthomeLocation> getDevices() {
        return new HashMap<>();
    }

    public Collection<SmarthomeLocation> giveLocations() {
        return this.locations.values();
    }

    //////// UTILITY FUNCTIONS

    public void expiresDotToUnderscore(){
        HashMap<String,Date> temp;
        HashMap<String,Date> tempEx;
        for(SmarthomeWebDevice device: devices.values()){
            temp = new HashMap<>();
            tempEx = device.getExpires();
            for (String key: tempEx.keySet()){
                temp.put(key.replaceAll("\\.","_"),tempEx.get(key));
            }
            device.setExpiresForMongo(temp);
        }
    }

    public void expiresUnderscoreToDot(){
        HashMap<String,Date> temp;
        HashMap<String,Date> tempEx;
        for(SmarthomeWebDevice device: devices.values()){
            temp = new HashMap<>();
            tempEx = device.getExpiresForMongo();
            for (String key: tempEx.keySet()){
                temp.put(key.replaceAll("_","\\."),tempEx.get(key));
            }
            device.setExpires(temp);
        }
    }

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger() {

        if (this.logger != null)
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if (this.logger.getHandlers().length == 0) { //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

    }

    //  obtains the mutual exclusion on the smarthome resources. Returns false in case of success(optimization)
    private boolean giveSmartHomeMutex() {

        initializeLogger();
        try {

            this.smartHomeMutex.acquire();
            return false;

        } catch (InterruptedException e) {

            logger.severe("Interruption occurred while the thread was waiting the mutex release. Abort operation");
            e.printStackTrace();
            return true;

        }
    }


    public HashMap<String, SmarthomeWebDevice> giveDevices() {
        return devices;
    }

    public void relink(){

        this.devices.clear();
        this.locations.forEach( (key, location) -> location.giveDevices().forEach( device -> this.devices.put( device.giveDeviceName(), device)));
    }

    public void connect( ConfigurationInterface configuration ){
        this.updater = new SmarthomeUpdater(username, this, configuration);
    }

    public void addSmartHomeMutex(Semaphore smartHomeMutex) {
        this.smartHomeMutex = smartHomeMutex;
    }

    //  releases the mutual exclusion from the smarthome resources
    private void releaseSmarthomeMutex() {
        this.smartHomeMutex.release();
    }

    //  verifies that the given address:port is not already used into the smarthome
    private boolean verifyAddressUnivocity(String address, int port) {
        for (SmarthomeLocation loc : this.locations.values())
            if (loc.getIpAddress().compareTo(address) == 0 && loc.getPort() == port)
                return false;
        return true;
    }

    /////// PUBLIC FUNCTIONS

    //// LOCATIONS

    //  adds a new location into the smarthome. Return false in case the location is already present
    public boolean addLocation(String location, String locID, String address, int port, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && this.locations.containsKey(location)) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  if the location is not already present we add it
        if (this.verifyAddressUnivocity(address, port) && !this.locations.containsKey(location)) {

            if (!trial) {
                this.locations.put(location, new SmarthomeLocation(location, locID, address, port));
                this.logger.info("New location " + location + " added [" + address + "][" + port + "]");
            }
            result = true;

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    //  removes a location from the smarthome specified by its name. Returns true in case of success
    public boolean removeLocation(String location, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && !this.locations.containsKey(location)) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification of location presence
        if (this.locations.containsKey(location)) {

            if (!trial) {
                //  getting all the devices from the location(must be removed from the devices array too)
                Collection<SmarthomeWebDevice> devs = this.locations.get(location).giveDevices();

                result = this.locations.remove(location) != null;
                if (result) {  //  in case of success of location removal we drop all the device from the devices list

                    devs.forEach(device -> this.devices.remove(device.giveDeviceName()));
                    this.logger.info("Location " + location + " removed. Consequently removed " + devs.size() + " devices");

                }
            } else
                result = true;

        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    //  changes the location name. Returns true in case of success
    public boolean changeLocationName(String old_name, String new_name, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && !this.locations.containsKey(old_name) && this.locations.containsKey(new_name)) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification of the old location presence and that the new name is not already assigned
        if (this.locations.containsKey(old_name) && !this.locations.containsKey(new_name)) {

            if (!trial) {
                //  getting all the devices to update their information
                SmarthomeLocation location = this.locations.remove(old_name);
                Collection<SmarthomeWebDevice> devs = location.giveDevices();
                location.setLocation(new_name);
                this.locations.put(new_name, location);
                //  updating the location name for the devices stored into the location
                devs.forEach(device -> device.setStructureHint(new_name));
                this.logger.info("Location name correctly changed from " + old_name + " to " + new_name +
                        ". Device information updated: " + devs.size());
            }
            result = true;
        }

        this.releaseSmarthomeMutex();
        return result;

    }

    //// SUB-LOCATIONS

    //  adds a new sublocation into a defined location. Returns true in case of success
    public boolean addSubLocation(String location, String subLocation, String sublocID, boolean trial) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        // if the specified location is present we forward to it the request
        if (this.locations.containsKey(location)) {

            //  a request on the same smarthome(shared between websockets) can be done many times
            //  if the request is already satisfied by the smarthome status another copy of the request is already applied
            if (!trial && this.locations.get(location).isPresent(subLocation)) {

                this.releaseSmarthomeMutex();
                return true;

            }
            result = this.locations.get(location).addSublocation(subLocation, sublocID, trial);

        }
        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;
    }

    //  removes a subLocation from the specified location. Returns true in case of success
    public boolean removeSublocation(String location, String subLocation, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  verification of subLocation presence
        if (this.locations.containsKey(location)) {

            //  a request on the same smarthome(shared between websockets) can be done many times
            //  if the request is already satisfied by the smarthome status another copy of the request is already applied
            if (!trial && !this.locations.get(location).isPresent(subLocation)) {

                this.releaseSmarthomeMutex();
                return true;

            }

            //  getting all the devices from the subLocation(must be removed from the devices array too)
            Collection<SmarthomeWebDevice> devs = this.locations.get(location).giveDevices(subLocation);

            result = this.locations.get(location).removeSublocation(subLocation, trial);
            if (result && !trial) { //  in case of success of subLocation removal we drop all the device from the devices list

                devs.forEach(device -> this.devices.remove(device.giveDeviceName()));
                this.logger.info("Sublocation " + subLocation + " of location " + location + " removed. " +
                        "Consequently removed " + devs.size() + " devices");

            }
        }

        this.releaseSmarthomeMutex();
        return result;
    }

    //  changes the sub-location name. Returns true in case of success
    public boolean changeSublocationName(String location, String old_name, String new_name, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  verification of subLocation presence
        if (this.locations.containsKey(location)) {

            //  a request on the same smarthome(shared between websockets) can be done many times
            //  if the request is already satisfied by the smarthome status another copy of the request is already applied
            if (!trial && !this.locations.get(location).isPresent(old_name) && this.locations.get(location).isPresent(new_name)) {

                this.releaseSmarthomeMutex();
                return true;

            }

            result = this.locations.get(location).changeSublocationName(old_name, new_name, trial);
            //  we don't need to update the device information(done by the SmarthomeLocation.changeSublocationName)
            if (!trial)
                this.logger.info("Device sub-location name correctly updated from " + old_name + " to " + new_name);

        }

        this.releaseSmarthomeMutex();
        return result;

    }

    //// DEVICES

    //  adds a new device info the specified location-sublocation. Returns true in case of success
    public boolean addDevice(String location, String sublocation, String dID, String name, SmarthomeDevice.DeviceType device_type, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;
        SmarthomeWebDevice device = this.devices.get(name);

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && device != null && device.getStructureHint().compareTo(location) == 0 && device.getRoomHint().compareTo(sublocation) == 0) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification of location presence and that the device is not present
        if (device == null && this.locations.containsKey(location)) {

            //  generation of device isntance from given information
            device = new SmarthomeWebDevice(dID, name, location, sublocation, device_type);

            // adding the device to the smartHome structure
            result = this.locations.get(location).addDevice(sublocation, device, trial);
            if (result && !trial) {  //  in case of success we add the device on the devices array for fast retrieval
                this.devices.put(name, device);
                this.logger.info("New device " + name + " added to " + location + ":" + sublocation);
            }
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    //  removes a device specified by its user assigned name. Return true in case of success
    public boolean removeDevice(String name, boolean trial) {

        initializeLogger();

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && !this.devices.containsKey(name)) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  if the device is present
        if (this.devices.containsKey(name)) {

            //  getting the device to infer its location/sub-location
            SmarthomeWebDevice device = this.devices.get(name);

            //  verification of location presence and removal of device from smartHome structure
            if (this.locations.containsKey(device.getStructureHint())) {

                result = this.locations.get(device.getStructureHint()).removeDevice(device.getRoomHint(), name, trial);

                if (result && !trial) {

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
    public boolean changeDeviceSubLocation(String location, String new_sublocation, String name, boolean trial) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;
        SmarthomeWebDevice device = this.devices.get(name);

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && device != null && device.getStructureHint().compareTo(location) == 0 && device.getRoomHint().compareTo(new_sublocation) == 0) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification that the location and the device exists
        if (this.locations.containsKey(location) && device != null) {

            //  forward to the sub location the execution of the command
            result = this.locations.get(location).changeDeviceSubLocation(device.getRoomHint(), new_sublocation, name, trial);
            //  we don't need to update the device information(done by the SmarthomeLocation.changeDeviceSubLocation)

        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;
    }

    // changes the device name. Returns true in case of success
    public boolean changeDeviceName(String old_name, String new_name, boolean trial) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        //  a request on the same smarthome(shared between websockets) can be done many times
        //  if the request is already satisfied by the smarthome status another copy of the request is already applied
        if (!trial && !this.devices.containsKey(old_name) && this.devices.containsKey(new_name)) {

            this.releaseSmarthomeMutex();
            return true;

        }

        //  verification that the device is present and don't exist a device with the new name assigned
        if (this.devices.containsKey(old_name) && !this.devices.containsKey(new_name)) {
            result = true;

            if (!trial) {

                SmarthomeWebDevice device = this.devices.remove(old_name);  //  removes the device from the list of devices

                //  removing the device from the smartHome
                if (this.locations.get(device.getStructureHint()).removeDevice(device.getRoomHint(), device.giveDeviceName(), false)) {

                    device.changeDeviceName(new_name);  //  changing the device name
                    //  re-adding the device to the subLocation
                    this.locations.get(device.getStructureHint()).addDevice(device.getRoomHint(), device, false);
                    //  re-adding the device to the list of devices
                    this.devices.put(new_name, device);
                } else
                    result = false;
            }

        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    //  executes a command on the specified device. In case of success the command is valid and executable
    public boolean performAction(String name, String action, String value, Date timestamp, boolean trial) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = false;

        if (this.devices.containsKey(name)) {

            HashMap<String, String> param = new HashMap<>();
            Gson gson = new Gson();
            param.put("device_name", name);
            param.put("action", action);
            param.put("value", value);

            if (trial)
                param.put("timestamp", gson.toJson(new Date(System.currentTimeMillis())));
            else
                param.put("timestamp", gson.toJson(timestamp));

            try {
                result = this.devices.get(name).executeAction(param, trial, false);
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        this.releaseSmarthomeMutex();  //  release of mutual exclusion
        return result;

    }

    //  verifies if the given device is present into the smartHome
    public boolean devicePresent(String name) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return false;

        boolean result = this.devices.containsKey(name);

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    //  generates a representation of the smarthome and all its locations to be used by web clients
    public String buildSmarthomeDefinition() {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return "";

        Gson gson = new Gson();
        ArrayList<HashMap<String, Object>> response = new ArrayList<>();
        this.locations.values().forEach(location -> response.add(location.buildSmarthomeLocation()));

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return gson.toJson(response);

    }

    public String giveDeviceNetwork(String name) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return null;

        String result = null;

        if (this.devices.containsKey(name)) {
            String location = this.devices.get(name).getStructureHint();
            if (this.locations.containsKey(location)) {
                SmarthomeLocation loc = this.locations.get(location);
                result = loc.getIpAddress() + ":" + loc.getPort();
            }
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion
        return result;

    }

    public String giveLocationNetwork(String location) {

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return null;


        String result = null;

        if (this.locations.containsKey(location)) {
            SmarthomeLocation loc = this.locations.get(location);
            result = loc.getIpAddress() + ":" + loc.getPort();
        }

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;

    }

    public String giveDeviceIdByName(String name) {

        String result = "";
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        if (this.devices.containsKey(name))
            result = this.devices.get(name).getId();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    public String giveDeviceNameById(String dID) {

        String result = "";
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        for (SmarthomeWebDevice device : this.devices.values())
            if (device.getId().compareTo(dID) == 0)
                result = device.giveDeviceName();


        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;

    }

    public String giveDeviceSubLocation(String name) {

        String result = "";
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        if (this.devices.containsKey(name))
            result = this.devices.get(name).getRoomHint();
        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    public String giveLocIdByName(String locName) {

        String result = "";

        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        if (this.locations.containsKey(locName))
            result = this.locations.get(locName).getLocId();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

    public String giveNextSublocID(String locName) {

        String result = "";
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        if (this.locations.containsKey(locName))
            result = this.locations.get(locName).giveNextSublocID();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;

    }

    public String giveSubLocIdByName(String locName, String subLocName) {

        String result = "";
        //  mutual exclusion on the interactions with the data structure
        if (this.giveSmartHomeMutex())
            return result;

        if (this.locations.containsKey(locName) && this.locations.get(locName).getSublocations().containsKey(subLocName))
            result = this.locations.get(locName).getSublocations().get(subLocName).getSubLocId();

        this.releaseSmarthomeMutex(); //  release of mutual exclusion

        return result;
    }

}
