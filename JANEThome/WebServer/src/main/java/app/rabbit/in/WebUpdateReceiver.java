package app.rabbit.in;

//  internal services
import app.sockets.WebRequest;
import iot.DeviceType;
import rabbit.Receiver;
import config.interfaces.IConfiguration;
import iot.SmarthomeManager;
import iot.SmarthomeWebDevice;

//  utils
import com.google.gson.Gson;

//  http protocol management
import javax.websocket.Session;

//  exceptions
import java.io.IOException;

//  collections
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Class assigned to each websocket to manage the incoming updates. The class is based on a Receiver.class which
 * generates an underlying environment for the requests de-multiplexing
 */
public class WebUpdateReceiver extends Receiver{
    
    private final Session target;    //  endpoint for communicating with the webClient
    private final SmarthomeManager smarthome;   //  a pointer to the smarthome session instance

    public WebUpdateReceiver( String endPointName, Session websocket, SmarthomeManager smarthome, IConfiguration configuration ) {

        //  each component need a unique endpointName in order for the underlying management layer to been able to forward
        //  to the correct receivers the messages(endpointName = username = email)
        super( endPointName, configuration );
        this.smarthome = smarthome;
        this.target = websocket;
        
    }


    ////////--  UTILITIES  --////////

    /**
     * Send a message to the webClient
     * @param response A preformatted class for sending and receiving messages with the webClients
     */
    private void sendMessage( WebRequest response ){
        
        try {
            
            target.getBasicRemote().sendText( new Gson().toJson( response ));

        }catch( IOException e ){
            
            e.printStackTrace();
            
        }
    }

    /**
     * Method automatically called when a new request of adding a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name to be associated to the new location
     * @param lID Unique identifier of the location(needed by Riccardo)
     * @param hostname hostname of the destination smarthome location
     * @param port port of the destination smarthome location
     */
    @Override
    protected void addLocation( String username, String location, String lID, String hostname, String port ) {
        
        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if ( this.smarthome.addLocation( location, lID, hostname, Integer.parseInt( port ), false )){

            data.put( "location", location );
            this.sendMessage( new WebRequest( "ADD_LOCATION", data ));

        }
    }

    /**
     * Method automatically called when a new request of adding a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location in which insert the subLocation
     * @param sublocation Name to be associated with the subLocation
     * @param subID Unique identifier of the subLocation(needed by Riccardo)
     */
    @Override
    protected void addSubLocation( String username, String location, String sublocation, String subID ) {

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.addSubLocation( location, sublocation, subID, false )) {

            data.put("location", location );
            data.put("sublocation", subID);

            this.sendMessage( new WebRequest( "ADD_SUBLOCATION", data ));
        }
        
    }

    /**
     * Method automatically called when a new request of adding a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location in which insert the subLocation
     * @param sublocation Name to be associated with the subLocation
     * @param name Unique identifier of the subLocation(needed by Riccardo)
     * @param type Type of the device(LIGHT,FAN,DOOR,THERMOSTAT,CONDITIONER)
     */
    @Override
    protected void addDevice( String username, String location, String sublocation, String name, String type, String dID ) {

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.addDevice( location, sublocation, dID, name, DeviceType.StringToType( type ), false )) {

            data.put( "location", location);
            data.put( "sublocation", sublocation);
            data.put( "name", name);
            data.put( "type", type);

            this.sendMessage( new WebRequest( "ADD_DEVICE", data ));

        }
        
    }

    /**
     * Method automatically called when a new request of renaming a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param old_name Current name of location to be renamed
     * @param new_name New name to associate to the location
     */
    @Override
    protected void renameLocation( String username, String old_name, String new_name ) {

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.changeLocationName( old_name, new_name, false )) {

            data.put( "old_name", old_name );
            data.put( "new_name", new_name );

            this.sendMessage( new WebRequest( "RENAME_LOCATION", data ));

        }
        
    }

    /**
     * Method automatically called when a new request of renaming a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the subLocation is deployed
     * @param old_name Current name of subLocation to be renamed
     * @param new_name New name to associate to the subLocation
     */
    @Override
    protected void renameSubLocation( String username, String location, String old_name, String new_name ){

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.changeSublocationName( location, old_name, new_name, false )) {

            data.put( "location", location );
            data.put( "old_name", old_name );
            data.put( "new_name", new_name );

            this.sendMessage( new WebRequest( "RENAME_SUBLOCATION", data ));

        }
        
    }

    /**
     * Method automatically called when a new request of renaming a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param old_name Current name of device to be renamed
     * @param new_name New name to associate to the device
     */
    @Override
    protected void renameDevice( String username, String old_name, String new_name ) {

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.changeDeviceName( old_name, new_name, false )) {

            data.put( "old_name", old_name );
            data.put( "new_name", new_name );

            this.sendMessage( new WebRequest( "RENAME_DEVICE", data ));

        }
        
    }

    /**
     * Method automatically called when a new request of removing a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location to be removed
     */
    @Override
    protected void removeLocation( String username, String location ){

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.removeLocation( location, false )) {

            data.put( "location", location );

            this.sendMessage( new WebRequest( "REMOVE_LOCATION", data ));

        }
        
    }

    /**
     * Method automatically called when a new request of removing a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the subLocation is deployed
     * @param sublocation Name of the subLocation to be removed
     */
    @Override
    protected void removeSubLocation( String username, String location, String sublocation ){

        HashMap<String,String> data = new HashMap<>();

        //  list of devices involved into the subLocation removal
        List<SmarthomeWebDevice> devices = smarthome.giveSublocationDevices( location, sublocation );

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.removeSublocation( location, sublocation, false )) {

            //  small patch, as requested by riccardo, we move all the devices to the default subLocation
            devices.forEach( device -> {

                data.put( "location", location );
                data.put( "sublocation", "default" );
                data.put( "name", device.giveDeviceName());

                this.sendMessage( new WebRequest( "CHANGE_SUBLOC", data ));
                data.clear();

            });

            data.put( "location", location );
            data.put( "sublocation", sublocation );

            this.sendMessage( new WebRequest( "REMOVE_SUBLOCATION", data ));
        }
    }

    /**
     * Method automatically called when a new request of removing a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param name Name of the device to be removed
     * @param dID Unique identifier of the device(needed by Federico)
     */
    @Override
    protected void removeDevice( String username, String name, String dID ){

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.removeDevice( name, false )){

            data.put("name", name );

            this.sendMessage( new WebRequest( "REMOVE_DEVICE", data ));

        }
    }

    /**
     * Method automatically called when a new request of changing a device location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the device is deployed
     * @param sublocation Name of the current subLocation of the device
     * @param new_sublocation Name of the subLocation in which move the device
     */
    @Override
    protected void changeDeviceSubLocation( String username, String location, String sublocation, String new_sublocation ){

        HashMap<String,String> data = new HashMap<>();

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.changeDeviceSubLocation( location, sublocation, new_sublocation, false )){

            data.put( "location", location );
            data.put( "sublocation", sublocation );
            data.put( "name", new_sublocation );

            this.sendMessage( new WebRequest( "CHANGE_SUBLOC", data ));

        }
    }

    /**
     * Method automatically called when a new request of executing a device action is received
     * @param username Username associated on the message(needed by Federico)
     * @param dID Unique identifier associated with the device
     * @param action Name of the action to perform(OnOff, OpenClose...)
     * @param value Value to associate to the action(OnOff -> 0 close 1 open)
     */
    @Override
    protected void executeAction( String username, String dID, String action, String value, Date timestamp ) {

        HashMap<String,String> data = new HashMap<>();

        //  SmarthomeManager.class uses device names as unique identifier
        String name = this.smarthome.giveDeviceNameById( dID );

        //  updating of the shared smartHome(can be already updated)
        if( this.smarthome.performAction( name, action, value, timestamp, false )) {

            data.put( "device_name", name );
            data.put( "action", action);
            data.put( "value", value );

            this.sendMessage( new WebRequest( "UPDATE", data ));

        }
    }
}