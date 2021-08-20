package rabbit.out;

import iot.SmarthomeDevice;

import java.io.Serializable;
import java.util.HashMap;

//  The class defines a generic update message that can be sent/received by the rabbitMQ clients
@SuppressWarnings("unused")
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
    private final HashMap<String, String> data = new HashMap<>();  //  list of parameters given with the message
    //  for more details on the list of parameters a complete description is available on the README file

    DeviceUpdate( UpdateType updateType ){

        this.updateType = updateType;

    }

    DeviceUpdate( String id, String name, UpdateType updateType,  HashMap<String,String> parameters ){
        this(updateType);
        data.putAll(parameters);
    }

    //  sets values inside the data field of the message
    private void setData( String key, String value ){
        if( this.data.containsKey(key))
            this.data.replace(key, value);
        else
            this.data.put(key, value);
    }

    ////  PUBLIC FUNCTIONS

    //  returns the update type
    public UpdateType getUpdateType(){
        return this.updateType;
    }

    //  returns the associated value to the key if present otherwise null
    public String getData( String key ){
        return this.data.get(key);
    }

    //  returns all the data field
    public HashMap<String,String> getData(){
        return this.data;
    }

    //  verify the presence of one or more keys inside the data field
    public boolean areSet( String...keys ){

        for( String key: keys )
            if( !this.data.containsKey( key ))
                return false;
        return true;

    }

    //  MESSAGE BUILDERS

    //  builds a DeviceUpdate instance for adding a location
    public static DeviceUpdate buildAddLocation( String location, String address, int port ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_LOCATION );
        update.setData( "location" , location );
        update.setData( "address" , address );
        update.setData( "port" , String.valueOf( port ));
        return update;

    }

    //  builds a DeviceUpdate instance for adding a sub-location
    public static DeviceUpdate buildAddSubLocation( String location, String subLocation ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_SUB_LOCATION );
        update.setData( "location" , location );
        update.setData( "sublocation" , subLocation );
        return update;

    }

    //  builds a DeviceUpdate instance for adding a device
    public static DeviceUpdate buildAddDevice( String location, String sublocation, String dID, String name, SmarthomeDevice.DeviceType type ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_DEVICE );
        update.setData("location" , location);
        update.setData("sublocation" , sublocation);
        update.setData("dID" , dID);
        update.setData("name" , name);
        update.setData("type" , SmarthomeDevice.DeviceType.typeToString(type));
        return update;

    }

    //  builds a DeviceUpdate instance for renaming a location
    public static DeviceUpdate buildRenameLocation( String old_name, String new_name ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_LOCATION );
        update.setData("old_name" , old_name );
        update.setData("new_name" , new_name );
        return update;

    }

    //  builds a DeviceUpdate instance for renaming a sub-location
    public static DeviceUpdate buildRenameSubLocation( String location, String old_name, String new_name ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_SUB_LOCATION );
        update.setData("location" , location);
        update.setData("old_name" , old_name);
        update.setData("new_name" , new_name);
        return update;

    }

    //  builds a DeviceUpdate instance for renaming a device
    public static DeviceUpdate buildRenameDevice( String dID, String old_name, String new_name ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_DEVICE );
        update.setData( "dID", dID );
        update.setData("old_name" , old_name);
        update.setData("new_name" , String.valueOf(new_name));
        return update;

    }

    //  builds a DeviceUpdate instance for removing a location
    public static DeviceUpdate buildRemoveLocation( String location ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_LOCATION );
        update.setData("location" , location);
        return update;

    }

    //  builds a DeviceUpdate instance for removing a sub-location
    public static DeviceUpdate buildRemoveSubLocation( String location, String subLocation ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_SUB_LOCATION);
        update.setData("location" , location);
        update.setData("sublocation" , subLocation);
        return update;

    }

    //  builds a DeviceUpdate instance for removing a device
    public static DeviceUpdate buildRemoveDevice( String dID, String name ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_DEVICE );
        update.setData("dID" , dID);
        update.setData("name" , name);
        return update;

    }

    public static DeviceUpdate buildChangeDeviceSubLocation( String location, String dID, String name, String new_sublocation ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.CHANGE_DEVICE_SUB_LOCATION );
        update.setData("location" , location);
        update.setData("dID" , dID);
        update.setData("name" , name);
        update.setData("sublocation", new_sublocation );
        return update;

    }

    public static DeviceUpdate buildDeviceUpdate( String dID, String name, String action, String value ){

        DeviceUpdate update = new DeviceUpdate( UpdateType.UPDATE );
        update.setData("dID" , dID );
        update.setData("device_name" , name );
        update.setData("action" , action );
        update.setData("value", value );
        return update;

    }
}
