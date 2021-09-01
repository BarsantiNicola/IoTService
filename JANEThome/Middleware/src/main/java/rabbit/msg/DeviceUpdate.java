package rabbit.msg;

import iot.SmarthomeDevice;
import java.io.Serializable;
import java.util.HashMap;

//  The class defines a generic update message that can be sent/received by the rabbitMQ clients
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

    //  UTILITY FUNCTIONS

    //  sets values inside the data field of the message
    private void setData( String key, String value ){

        if( this.data.containsKey(key))
            this.data.replace(key, value);
        else
            this.data.put(key, value);

    }

    ////  GETTERS

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

    //// PUBLIC FUNCTIONS

    //  verify the presence of one or more keys inside the "data" field
    public boolean areSet( String...keys ){

        for( String key: keys )
            if( !this.data.containsKey( key ))
                return false;
        return true;

    }

    ////  MESSAGE BUILDERS

    //  builds a DeviceUpdate instance for adding a location
    public static DeviceUpdate buildAddLocation( String location, String address, int port ){

        //  parameters verification
        if( location == null || location.length() == 0 || address == null || address.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_LOCATION );
        update.setData( "location" , location );
        update.setData( "address" , address );
        update.setData( "port" , String.valueOf( port ));
        return update;

    }

    //  builds a DeviceUpdate instance for adding a sub-location
    public static DeviceUpdate buildAddSubLocation( String location, String subLocation ){

        //  parameters verification
        if( location == null || location.length() == 0 || subLocation == null || subLocation.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.ADD_SUB_LOCATION );
        update.setData( "location" , location );
        update.setData( "sublocation" , subLocation );
        return update;

    }

    //  builds a DeviceUpdate instance for adding a device
    public static DeviceUpdate buildAddDevice( String location, String sublocation, String dID, String name, SmarthomeDevice.DeviceType type ){

        //  parameters verification
        if( location == null || location.length() == 0 || sublocation == null || sublocation.length() == 0 ||
                dID == null || dID.length() == 0 || name == null || name.length() == 0)
            return null;

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

        //  parameters verification
        if( old_name == null || old_name.length() == 0 || new_name == null || new_name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_LOCATION );
        update.setData("old_name" , old_name );
        update.setData("new_name" , new_name );
        return update;

    }

    //  builds a DeviceUpdate instance for renaming a sub-location
    public static DeviceUpdate buildRenameSubLocation( String location, String old_name, String new_name ){

        //  parameters verification
        if( location == null || location.length() == 0 || old_name == null || old_name.length() == 0 ||
                new_name == null || new_name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_SUB_LOCATION );
        update.setData("location" , location);
        update.setData("old_name" , old_name);
        update.setData("new_name" , new_name);
        return update;

    }

    //  builds a DeviceUpdate instance for renaming a device
    public static DeviceUpdate buildRenameDevice( String dID, String old_name, String new_name ){

        //  parameters verification
        if( dID == null || dID.length() == 0 || old_name == null || old_name.length() == 0 ||
                new_name == null || new_name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.RENAME_DEVICE );
        update.setData( "dID", dID );
        update.setData("old_name" , old_name );
        update.setData("new_name" , new_name );
        return update;

    }

    //  builds a DeviceUpdate instance for removing a location
    public static DeviceUpdate buildRemoveLocation( String location ){

        //  parameters verification
        if( location == null || location.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_LOCATION );
        update.setData("location" , location);
        return update;

    }

    //  builds a DeviceUpdate instance for removing a sub-location
    public static DeviceUpdate buildRemoveSubLocation( String location, String subLocation ){

        //  parameters verification
        if( location == null || location.length() == 0 || subLocation == null || subLocation.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_SUB_LOCATION);
        update.setData("location" , location);
        update.setData("sublocation" , subLocation);
        return update;

    }

    //  builds a DeviceUpdate instance for removing a device
    public static DeviceUpdate buildRemoveDevice( String dID, String name ){

        //  parameters verification
        if( dID == null || dID.length() == 0 || name == null || name.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.REMOVE_DEVICE );
        update.setData("dID" , dID);
        update.setData("name" , name);
        return update;

    }

    //  builds a DeviceUpdate instance for changing the subLocation associated to a device
    public static DeviceUpdate buildChangeDeviceSubLocation( String location, String dID, String name, String new_sublocation ){

        //  parameters verification
        if( dID == null || dID.length() == 0 || location == null || location.length() == 0 ||
                name == null || name.length() == 0 || new_sublocation == null || new_sublocation.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.CHANGE_DEVICE_SUB_LOCATION );
        update.setData("location" , location);
        update.setData("dID" , dID);
        update.setData("name" , name);
        update.setData("sublocation", new_sublocation );
        return update;

    }

    //  builds a DeviceUpdate instance for executing a command of a device
    public static DeviceUpdate buildDeviceUpdate( String dID, String action, String value ){

        //  parameters verification
        if( dID == null || dID.length() == 0 ||
                action == null || action.length() == 0 || value == null || value.length() == 0 )
            return null;

        DeviceUpdate update = new DeviceUpdate( UpdateType.UPDATE );
        update.setData("dID" , dID );
        update.setData("action" , action );
        update.setData("value", value );
        return update;

    }
}
