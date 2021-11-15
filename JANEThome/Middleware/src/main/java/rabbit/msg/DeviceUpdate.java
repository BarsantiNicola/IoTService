package rabbit.msg;

//  internal services
import iot.DeviceType;

//  utils
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.Date;

//  collections
import java.util.HashMap;

/**
 * Class to be used with the rabbit package utilities. Defines a generic message that can be sent/received
 * by the developed rabbitMQ service
 */
public class DeviceUpdate implements Serializable{

    //  types of updates
    public enum UpdateType{

        ADD_LOCATION,
        ADD_SUB_LOCATION,
        ADD_DEVICE,
        RENAME_LOCATION,
        RENAME_SUB_LOCATION,
        RENAME_DEVICE,
        REMOVE_LOCATION,
        REMOVE_SUB_LOCATION,
        REMOVE_DEVICE,
        CHANGE_DEVICE_SUB_LOCATION,
        UPDATE

    }

    private final UpdateType updateType;   //  type of update
    private final String timestamp;        //  time in which the update is generated
    private final HashMap<String, String> data = new HashMap<>();  //  list of parameters given with the message

    //  for more details on the list of parameters a complete description is available on the README file
    DeviceUpdate( UpdateType updateType, Date timestamp ){

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" ).create();
        this.updateType = updateType;
        this.timestamp = gson.toJson( timestamp );

    }


    ////////--  SETTERS  --////////


    /**
     * Set data fields inside the object
     * @param key the name of the field to insert
     * @param value the value of the field to insert
     */
    private void setData( String key, String value ){

        if( this.data.containsKey(key))
            this.data.replace(key, value);
        else
            this.data.put(key, value);

    }


    ////////--  GETTERS  --////////


    public UpdateType getUpdateType(){

        return this.updateType;

    }

    /**
     * Returns the associated value to the given key
     * @param key the name of the field to get
     * @return The stringed value of the field or null in case it doesn't exist
     */
    public String getData( String key ){

        return this.data.get(key);

    }

    public HashMap<String,String> getData(){

        return this.data;

    }


    ////////--  UTILITIES  --////////


    /**
     * Converts in an appropriate way the timestamp of the update
     * @return {@link Date} The timestamp of the message
     */
    public Date giveConvertedTimestamp(){

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create();
        return gson.fromJson( this.timestamp, Date.class );

    }

    /**
     * Method to verify the presence of one or more keys inside the "data" field
     * @param keys List of keys to be tested
     * @return true in case all the given keys are present false otherwise
     */
    public boolean areSet( String...keys ){

        for( String key: keys )
            if( !this.data.containsKey( key ))
                return false;

        return true;

    }


    ////////--  MESSAGE BUILDERS  --////////


    /**
     * Build a DeviceUpdate message for adding a location
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location to add
     * @param locID Unique identifier of the location(String integer)
     * @param address Hostname of the location
     * @param port Port used by the location
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildAddLocation( Date timestamp, String location, String locID, String address, int port ){

        //  parameters verification
        if( location == null || location.length() == 0 ||
                address == null || address.length() == 0 ||
                    timestamp == null || locID == null || locID.length() == 0 || port < 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_LOCATION, timestamp );
        update.setData( "location" , location );
        update.setData( "locID", locID );
        update.setData( "address" , address );
        update.setData( "port" , String.valueOf( port ));
        return update;

    }

    /**
     * Build a DeviceUpdate message for adding a subLocation
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location in which deploy the subLocation
     * @param subLocation Name of the subLocation to add
     * @param sublocID Unique identifier of the subLocation(stringed integer)
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildAddSubLocation( Date timestamp, String location, String subLocation, String sublocID ){

        //  parameters verification
        if( timestamp == null || location == null || location.length() == 0 ||
                subLocation == null || subLocation.length() == 0 ||
                    sublocID == null || sublocID.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_SUB_LOCATION, timestamp );
        update.setData( "location" , location );
        update.setData( "sublocation" , subLocation );
        update.setData( "sublocID" , sublocID );
        return update;

    }

    /**
     * Build a DeviceUpdate message for adding a device
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location in which deploy the device
     * @param sublocation Name of the subLocation in which deploy the device
     * @param dID Unique identifier of the device(Needed by Federico)
     * @param name Name of the device to insert
     * @param type {@link iot.DeviceType} Unique identifier of the subLocation(stringed integer)
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildAddDevice( Date timestamp, String location, String sublocation, String dID, String name, DeviceType type ){

        //  parameters verification
        if( timestamp == null || location == null || location.length() == 0 ||
                sublocation == null || sublocation.length() == 0 ||
                dID == null || dID.length() == 0 ||
                name == null || name.length() == 0 || type == DeviceType.UNKNOWN )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_DEVICE, timestamp );
        update.setData( "location" , location );
        update.setData( "sublocation" , sublocation );
        update.setData( "dID" , dID );
        update.setData( "name" , name );
        update.setData( "type" , type.toString().toLowerCase() );
        return update;

    }

    /**
     * Build a DeviceUpdate message for renaming a location
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param old_name Current name of the location
     * @param new_name New name for the location
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    //  builds a DeviceUpdate instance for renaming a location
    public static DeviceUpdate buildRenameLocation( Date timestamp, String old_name, String new_name ){

        //  parameters verification
        if( timestamp == null || old_name == null || old_name.length() == 0 ||
                new_name == null || new_name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_LOCATION, timestamp );
        update.setData( "old_name" , old_name );
        update.setData( "new_name" , new_name );
        return update;

    }

    /**
     * Build a DeviceUpdate message for renaming a subLocation
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location in which search the subLocation to rename
     * @param old_name Current name of the subLocation
     * @param new_name New name for the subLocation
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildRenameSubLocation( Date timestamp, String location, String old_name, String new_name ){

        //  parameters verification
        if( timestamp == null || location == null || location.length() == 0 ||
                old_name == null || old_name.length() == 0 ||
                    new_name == null || new_name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_SUB_LOCATION, timestamp );
        update.setData( "location" , location );
        update.setData( "old_name" , old_name );
        update.setData( "new_name" , new_name );
        return update;

    }

    /**
     * Build a DeviceUpdate message for renaming a device
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param dID Unique identificator of the device(Needed by Federico)
     * @param old_name Current name of the device
     * @param new_name New name for the device
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildRenameDevice( Date timestamp, String dID, String old_name, String new_name ){

        //  parameters verification
        if( timestamp == null || dID == null || dID.length() == 0 ||
                old_name == null || old_name.length() == 0 ||
                    new_name == null || new_name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_DEVICE, timestamp );
        update.setData( "dID", dID );
        update.setData( "old_name" , old_name );
        update.setData( "new_name" , new_name );
        return update;

    }

    /**
     * Build a DeviceUpdate message for removing a location
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location to remove
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildRemoveLocation( Date timestamp, String location ){

        //  parameters verification
        if( timestamp == null || location == null || location.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_LOCATION, timestamp );
        update.setData( "location" , location );
        return update;

    }

    /**
     * Build a DeviceUpdate message for removing a subLocation
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location in which the subLocation is deployed
     * @param subLocation Name of the subLocation to remove
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildRemoveSubLocation( Date timestamp, String location, String subLocation ){

        //  parameters verification
        if( location == null || location.length() == 0 || subLocation == null || subLocation.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_SUB_LOCATION, timestamp );
        update.setData("location" , location);
        update.setData("sublocation" , subLocation);
        return update;

    }

    /**
     * Build a DeviceUpdate message for removing a subLocation
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param dID Unique identifier of the device(Needed by Federico)
     * @param name Name of the device to remove
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildRemoveDevice( Date timestamp, String dID, String name ){

        //  parameters verification
        if( timestamp == null || dID == null || dID.length() == 0 ||
                name == null || name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_DEVICE, timestamp );
        update.setData( "dID" , dID );
        update.setData( "name" , name );
        return update;

    }

    /**
     * Build a DeviceUpdate message for changing the subLocation in which the device is deployed
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param location Name of the location in which the device is deployed
     * @param subLocation Name of the current subLocation in which the device is deployed
     * @param dID Unique identifier of the device(Needed by Federico)
     * @param new_sublocation Name of subLocation in which move the device
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildChangeDeviceSubLocation( Date timestamp, String location, String dID, String subLocation, String new_sublocation ){

        //  parameters verification
        if( timestamp == null || dID == null || dID.length() == 0 ||
                location == null || location.length() == 0 ||
                    subLocation == null || subLocation.length() == 0 ||
                        new_sublocation == null || new_sublocation.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.CHANGE_DEVICE_SUB_LOCATION, timestamp );
        update.setData( "location" , location );
        update.setData( "dID" , dID );
        update.setData( "sublocation" , subLocation );
        update.setData( "name", new_sublocation );
        return update;

    }

    /**
     * Build a DeviceUpdate message for executing an action on a device
     * @param timestamp {@link Date} Timestamp to be putted on the message
     * @param dID Unique identifier of the device(Needed by Federico)
     * @param action Name of subLocation in which move the device
     * @param value Name of the current subLocation in which the device is deployed
     * @return {@link DeviceUpdate} Returns a builded message ready to be sent or null in case some parameters are missing
     */
    public static DeviceUpdate buildDeviceUpdate( Date timestamp, String dID, String action, String value ){

        //  parameters verification
        if( timestamp == null || dID == null || dID.length() == 0 ||
                action == null || action.length() == 0 ||
                    value == null || value.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.UPDATE, timestamp );
        update.setData( "dID" , dID );
        update.setData( "action" , action );
        update.setData( "value", value );
        return update;

    }
}
