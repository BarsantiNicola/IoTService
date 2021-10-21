package iot;

import java.util.*;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

//  Class used to generate the structure of the smarthome, in particular to define a sublocation.
//  A sublocation is a container for devices and has to be deployed into a location
@SuppressWarnings("unused")
public class SmarthomeSublocation implements Serializable {

    private String subLocId;
    private String subLocation;        //  sublocation name
    private transient Logger logger;
    private final HashMap<String,SmarthomeWebDevice> devices = new HashMap<>();  //  deployed devices

    //////// CONSTRUCTORS

    public SmarthomeSublocation(String subLocation, String sublocID ){

        this.subLocation = subLocation;
        this.subLocId = sublocID;
        initializeLogger();

    }

    SmarthomeSublocation( String subLocation, String sublocID, List<SmarthomeWebDevice> devices ){

        this( subLocation, sublocID );
        devices.forEach(device -> this.devices.put(device.giveDeviceName(), device));

    }

    public SmarthomeSublocation() {
        initializeLogger();
    }

    /////// GETTERS

    //  returns the sublocation name
    public String getSubLocation(){
        return this.subLocation;
    }

    public String getSubLocId() {
        return subLocId;
    }

    //  returns the list of all the sublocation's devices
    public HashMap<String,SmarthomeWebDevice> getDevices(){ return this.devices; }

    /////// SETTERS

    // changes the sublocation's name
    public void setSubLocation(String subLocation){

        this.subLocation = subLocation;

        //  changing all the devices information to link to the new subLocation
        this.devices.values().forEach( device -> device.setRoomHint( subLocation ));

    }

    public void setSubLocId(String subLocId) {
        this.subLocId = subLocId;
    }

    public void setDevices( HashMap<String,SmarthomeWebDevice> devices ){
        this.devices.putAll(devices);
    }

    /////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger(){

        if( this.logger != null )
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

    }

    //  PUBLIC FUNCTIONS

    //  returns a device if it is present into the sublocation's devices otherwise null
    public SmarthomeWebDevice giveDevice(String name){
        return this.devices.get(name);
    }

    //  adds a new device to the sublocation. It returns false if a device with the given name is already present
    public boolean addDevice(SmarthomeWebDevice dev, boolean trial ){

        if( trial )
            return !this.devices.containsKey( dev.giveDeviceName() );
        initializeLogger();
        if(!this.devices.containsKey(dev.giveDeviceName())) {
            this.devices.put(dev.giveDeviceName(), dev);
            logger.info("Request to add a new device [" + dev.getId() + ":"+dev.giveDeviceName()+"] into " + this.subLocation +" correctly done");
            return true;
        }
        return false;

    }

    //  removes a new device from the sublocation
    public boolean removeDevice( String name, boolean trial ){

        if( trial )
            return this.devices.containsKey( name );

        return this.devices.remove(name) != null;
    }

    //  generates a representation of the sublocation and all its devices to be used by web clients
    public HashMap<String,Object> buildSmarthomeSublocation(){

        HashMap<String, Object> result = new HashMap<>();
        ArrayList<HashMap<String,Object>> devs = new ArrayList<>();
        initializeLogger();
        //  collect all the devices descriptions
        Collection<SmarthomeWebDevice> devices = this.devices.values();
        for( SmarthomeWebDevice device: devices)
            devs.add(device.buildSmarthomeWebDevice());

        //  generates the sublocation description
        result.put("sublocation" , this.subLocation);
        result.put( "devices" , devs);

        logger.info( "Generation of " + this.subLocation + " description correctly done" );
        return result;

    }

    public Collection<SmarthomeWebDevice> giveDevices(){ return this.devices.values(); }

}