package iot;

//  utils
import java.util.*;
import java.io.Serializable;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class used to generate the structure of the smarthome, in particular to define a sublocation
 * A sublocation is a container for devices and has to be deployed into a location
 */
public class SmarthomeSublocation implements Serializable {

    private String subLocId;           //  sublocation identifier(needed by Riccardo)
    private String subLocation;        //  sublocation name
    private transient Logger logger;
    private HashMap<String,SmarthomeWebDevice> devices = new HashMap<>();  //  deployed devices

    public SmarthomeSublocation(){

        this.logger = LogManager.getLogger( getClass().getName() );

    }

    public SmarthomeSublocation( String subLocation, String sublocID ){

        this();
        this.subLocation = subLocation;
        this.subLocId = sublocID;


    }



    ////////--  SETTERS  --////////



    public void changeSubLocation( String subLocation ){

        this.subLocation = subLocation;

        //  changing all the devices information to link to the new subLocation
        this.devices.values().forEach( device -> device.setRoomHint( subLocation ));

    }

    public void setSubLocation( String subLocation ){ this.subLocation = subLocation; }
    public void setSubLocId( String subLocId ){ this.subLocId = subLocId; }

    public void setDevices( HashMap<String,SmarthomeWebDevice> devices ){ this.devices = devices; }


    ////////--  GETTERS  --////////


    public String getSubLocation(){ return this.subLocation; }

    public String getSubLocId() { return subLocId; }

    public HashMap<String,SmarthomeWebDevice> getDevices(){ return this.devices; }


    ////////--  UTILITIES  --////////


    /**
     * If present returns the specified device
     * @param name The name of the device
     * @return Returns {@link SmarthomeWebDevice} if present otherwise null
     */
    public SmarthomeWebDevice giveDevice( String name ){ return this.devices.get( name ); }

    /**
     * Returns all the devices stored into the subLocation
     * @return {@link SmarthomeWebDevice} A collection containing all the devices
     */
    public Collection<SmarthomeWebDevice> giveDevices(){ return this.devices.values(); }

    /**
     * Adds a device to the subLocation
     * @param dev {@link SmarthomeWebDevice} instance to add
     * @param trial if true it will just test the command without making any modification
     * @return Returns true in case of success false otherwise(device already present)
     */
    public boolean addDevice(SmarthomeWebDevice dev, boolean trial ){

        if( trial )  //  if we just want to test the request, we need just to see if it is already present
            return !this.devices.containsKey( dev.giveDeviceName() );

        //  patch for Federico, when the class is extracted from the db we lost the loggers
        this.logger = LogManager.getLogger( getClass().getName() );

        if( !this.devices.containsKey( dev.giveDeviceName() )){

            this.devices.put( dev.giveDeviceName(), dev );
            logger.info( "Request to add a new device [" + dev.getId() + ":"+dev.giveDeviceName()+"] into " + this.subLocation +" correctly done" );
            return true;

        }

        return false;

    }

    /**
     * Removes a device from the subLocation
     * @param name Name of the device
     * @param trial if true it will just test the command without making any modification
     * @return True in case of success otherwise false
     */
    public boolean removeDevice( String name, boolean trial ){

        if( trial )  //  if we just want to test the request, we need just to see if it is already present
            return this.devices.containsKey( name );

        return this.devices.remove(name) != null;

    }

    /**
     * Generates a representation of the sublocation and all its devices to be used by web clients
     * @return {@link HashMap} An hashmap containing all the devices of the subLocation
     */
    public HashMap<String,Object> buildSmarthomeSublocation(){

        HashMap<String, Object> result = new HashMap<>();  //  main subLocation description structure
        ArrayList<HashMap<String,Object>> devs = new ArrayList<>();  //  devices description subStructure

        //  patch for Federico, when the class is extracted from the db we lost the loggers
        this.logger = LogManager.getLogger( getClass().getName() );

        //  collecting all the devices descriptions
        this.devices.values().forEach( dev -> devs.add( dev.buildSmarthomeWebDevice() ));

        //  generates the sublocation description
        result.put( "sublocation" , this.subLocation );
        result.put( "devices" , devs );

        logger.info( "Generation of " + this.subLocation + " description correctly done" );
        return result;

    }
}