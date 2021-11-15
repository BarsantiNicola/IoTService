package rabbit.in;

// internal services
import config.interfaces.IConfiguration;
import db.interfaces.DBinterface;
import iot.DeviceType;
import iot.Operation;
import rabbit.Receiver;
import rabbit.msg.DeviceUpdate;

//  ejb3.0
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

//  utils
import java.util.Date;



@Startup
@Singleton
public class DatabaseUpdater extends Receiver{

    @EJB
    private IConfiguration configuration;

    @EJB
    private DBinterface dB;

    public DatabaseUpdater() {}

    @PostConstruct
    public void init(){

        super.initialize( "db", configuration );

    }

    ////////--  UTILITIES  --////////

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

        //  updating of the shared smartHome(can be already updated)
        this.dB.addElementManager(
                username,
                DeviceUpdate.UpdateType.ADD_LOCATION,
                lID,
                location,
                hostname,
                Integer.parseInt( port ),
                null, null, null );

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

        dB.addElementManager(
                username,
                DeviceUpdate.UpdateType.ADD_SUB_LOCATION,
                subID,
                location,
                null,
                0,
                sublocation,
                null, null );

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

        dB.addElementManager( username,
                DeviceUpdate.UpdateType.ADD_DEVICE,
                dID,
                location,
                null,
                0,
                sublocation,
                name,
                DeviceType.StringToType( type ));

    }

    /**
     * Method automatically called when a new request of renaming a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param old_name Current name of location to be renamed
     * @param new_name New name to associate to the location
     */
    @Override
    protected void renameLocation( String username, String old_name, String new_name ) {

        dB.renameElementManager(
                username,
                DeviceUpdate.UpdateType.RENAME_LOCATION,
                old_name,
                new_name,
                null );

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

        dB.renameElementManager( username,
                DeviceUpdate.UpdateType.RENAME_SUB_LOCATION,
                old_name,
                new_name,
                location );

    }

    /**
     * Method automatically called when a new request of renaming a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param old_name Current name of device to be renamed
     * @param new_name New name to associate to the device
     */
    @Override
    protected void renameDevice( String username, String old_name, String new_name ) {

        dB.renameElementManager( username,
                DeviceUpdate.UpdateType.RENAME_DEVICE,
                old_name,
                new_name,
                null );

    }

    /**
     * Method automatically called when a new request of removing a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location to be removed
     */
    @Override
    protected void removeLocation( String username, String location ){

        dB.removeElementIntoManager( username,
                DeviceUpdate.UpdateType.REMOVE_LOCATION,
                location,
                null );

    }

    /**
     * Method automatically called when a new request of removing a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the subLocation is deployed
     * @param sublocation Name of the subLocation to be removed
     */
    @Override
    protected void removeSubLocation( String username, String location, String sublocation ){

        dB.removeElementIntoManager( username,
                DeviceUpdate.UpdateType.REMOVE_SUB_LOCATION,
                location,
                sublocation );

    }

    /**
     * Method automatically called when a new request of removing a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param name Name of the device to be removed
     * @param dID Unique identifier of the device(needed by Federico)
     */
    @Override
    protected void removeDevice( String username, String name, String dID ){

        dB.removeElementIntoManager( username,
                DeviceUpdate.UpdateType.REMOVE_DEVICE,
                name,
                null );

        dB.removeAllStatistics( dID );

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

        dB.moveDevice( username,
                location,
                sublocation,
                new_sublocation );

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

        dB.performAction(
                username,
                dID,
                action,
                value );

        dB.addOperation( new Operation( dID, action, value, timestamp ));

    }
}
