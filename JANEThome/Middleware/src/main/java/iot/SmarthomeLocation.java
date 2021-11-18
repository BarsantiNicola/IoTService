package iot;

//  utils
import java.io.Serializable;
import java.util.*;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class used to generate the smartHome location, a container for smarthome subLocations
 *
 */
public class SmarthomeLocation implements Serializable {

    private String locId;                              //  unique identifier of the location(needed by Riccardo)
    private String location;                           //  location name
    private String ipAddress;                          //  ip address used by the location
    private int port;                                  //  the port used by the location
    private int maxSublocID;                           //  value to be used for the next subLocation as sublocID
    private HashMap<String,SmarthomeSublocation> sublocations = new HashMap<>();   //  list of all the sublocations
    private transient Logger logger;


    public SmarthomeLocation(){

        this.logger = LogManager.getLogger( getClass().getName() );

    }

    public SmarthomeLocation( String location, String locID, String address, int port ){

        this();
        this.location = location;
        this.ipAddress = address;
        this.port = port;
        this.locId = locID;
        this.maxSublocID = 1;
        this.sublocations.put( "default", new SmarthomeSublocation("default", "0" ));

    }


    ////////--  SETTERS  --////////


    public void setLocId(String locId) { this.locId = locId; }

    public void setLocation( String location ){ this.location = location; }

    public void setIpAddress(String ip){ this.ipAddress = ip; }

    public void setPort(int port){ this.port = port; }

    public void setMaxSublocID( int maxSublocID ){ this.maxSublocID = maxSublocID; }

    public void setSublocations( HashMap<String, SmarthomeSublocation> sublocations ){ this.sublocations = sublocations; }


    ////////--  GETTERS  --////////


    public String getLocId() { return locId; }

    public String getLocation(){ return location; }

    public String getIpAddress(){ return ipAddress; }

    public int getPort(){ return port; }

    public int getMaxSublocID() { return maxSublocID; }

    public HashMap<String, SmarthomeSublocation> getSublocations() { return sublocations; }


    ////////--  UTILITIES  --////////

    /**
     * Returns the next sublocId and increment the stored one
     * @return A stringed integer representing an id for the next subLocation
     */
    public String giveNextSublocID() {

        return String.valueOf( this.maxSublocID++ );

    }

    /**
     * Adds a new sublocation into the location. It returns false if a sublocation with the given name is already present
     * @param sublocation Sublocation name
     * @param sublocID    Sublocation ID
     * @param trial       if true it will just test the command without making any modification
     * @return True in case of success false otherwise
     */
    boolean addSublocation( String sublocation, String sublocID, boolean trial ){

        //  patch for Federico, when the class is extracted from the db we lost the loggers
        this.logger = LogManager.getLogger( getClass().getName() );

        //  verification that the requested sublocation isn't already present
        if( this.sublocations.containsKey( sublocation ))
            return false;

        if( !trial ){  //  if not a trial we apply the changes

            this.sublocations.put( sublocation, new SmarthomeSublocation( sublocation, sublocID ));
            logger.info( "New Sublocation " + sublocation + " correctly added to " + this.location );

        }

        return true;

    }

    /**
     * Removes a sublocation from the location if present
     * @param subLocation Name of the subLocation to remove
     * @param trial       if true it will just test the command without making any modification
     * @return            True in case of success false otherwise
     */
    boolean removeSublocation( String subLocation, boolean trial ){

        if( trial )
            return this.sublocations.containsKey( subLocation );

        //  if not a trial we apply the changes

        return this.sublocations.remove( subLocation ) != null;

    }

    /**
     * Changes the name of the subLocation
     * @param old_name    Current name of the subLocation
     * @param new_name    Name to apply to the subLocation
     * @param trial       if true it will just test the command without making any modification
     * @return            True in case of success false otherwise
     */
    boolean changeSublocationName( String old_name, String new_name, boolean trial ){

        if( !this.sublocations.containsKey( old_name ) || this.sublocations.containsKey( new_name ))
            return false;

        if( !trial ){  //  if not a trial we apply the changes

            SmarthomeSublocation subloc = this.sublocations.remove( old_name );  //  removing old subLocation
            subloc.changeSubLocation( new_name );                                   //  changing subLocation name
            this.sublocations.put( new_name, subloc );                           //  re-add the subLocation

        }

        return true;

    }

    /**
     * Adds a new device into the given subLocation(if present)
     * @param sublocation SubLocation in which insert the device
     * @param device      Device to add
     * @param trial       if true it will just test the command without making any modification
     * @return            True in case of success false otherwise
     */
    boolean addDevice( String sublocation, SmarthomeWebDevice device, boolean trial ){

        //  verification of the presence of the subLocation
        if( this.sublocations.containsKey( sublocation ))
            return this.sublocations.get( sublocation ).addDevice( device, trial ); //  forward the request

        return false;

    }

    /**
     * Removes the device from the given subLocatio
     * @param sublocation SubLocation in which remove the device
     * @param name        Name of the device to remove
     * @param trial       if true it will just test the command without making any modification
     * @return            True in case of success false otherwise
     */
    boolean removeDevice( String sublocation, String name, boolean trial ){

        //  verification of subLocation presence
        if( this.sublocations.containsKey( sublocation ))
            return this.sublocations.get( sublocation ).removeDevice( name, trial );  //  forward the request

        return false;
    }

    /**
     * Changes the sublocation associated with a device
     * @param old_sublocation Current subLocation name containing the device
     * @param new_sublocation SubLocation name in which move the device
     * @param name            Name of the device to move
     * @param trial           if true it will just test the command without making any modification
     * @return                True in case of success false otherwise
     */
    boolean changeDeviceSubLocation( String old_sublocation, String new_sublocation, String name, boolean trial ){

        //  patch for Federico, when the class is extracted from the db we lost the loggers
        this.logger = LogManager.getLogger( getClass().getName() );

        //  verification of the presence of both the subLocations
        if( !this.sublocations.containsKey( new_sublocation ) || !this.sublocations.containsKey( old_sublocation ))
            return false;

        //  getting the device from the old sub-location
        SmarthomeWebDevice device = this.sublocations.get( old_sublocation ).giveDevice( name );

        //  removing the device from the old sub-location
        if( this.sublocations.get( old_sublocation ).removeDevice( name, trial )){

            if( trial )  //  if it is just a trial, we ha ve done
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

    /**
     * Gives all the devices stored into the location
     * @return  {@link SmarthomeWebDevice} True in case of success false otherwise
     */
    List<SmarthomeWebDevice> giveDevices(){

        ArrayList<SmarthomeWebDevice> devs = new ArrayList<>();

        //  getting all the devices from all the subLocations
        this.sublocations.values().forEach( sublocation -> devs.addAll( sublocation.giveDevices()) );
        return devs;

    }

    /**
     * if present gives all the devices stored into the subLocation deployed inside the current location
     * @return {@link SmarthomeWebDevice} List of devices
     */
    List<SmarthomeWebDevice> giveDevices( String subLocation ){

        ArrayList<SmarthomeWebDevice> devs = new ArrayList<>();

        //  verification of subLocation presence
        if( this.sublocations.containsKey( subLocation ))
            devs.addAll( this.sublocations.get( subLocation ).giveDevices() );

        return devs;

    }

    /**
     * Used to generate a representation of the smartHome for the webclient initialization
     * @return {@link SmarthomeWebDevice} List of devices
     */
     HashMap<String,Object> buildSmarthomeLocation(){

         //  patch for Federico, when the class is extracted from the db we lost the loggers
        this.logger = LogManager.getLogger( getClass().getName() );

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

    /**
     * Verifies the presence of a subLocation
     * @return True in case the subLocation is found
     */
    boolean isPresent( String subLocation ){

        return this.sublocations.containsKey( subLocation );

    }

}
