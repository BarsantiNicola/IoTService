package rabbit.in;

//  internal services
import config.interfaces.IConfiguration;
import iot.DeviceType;
import iot.SmarthomeManager;
import rabbit.Receiver;

//  utils
import java.util.Date;

/**
 * Class designed to receive the updates from the middleware and update the smarthome associated with
 * a user. The class is similar to the class WebServer.rabbit.in.WebUpdateReceiver but necessary in order
 * to update the smarthome even if no client in currectly connected(so when no websockets are present to perform the update)
 */

public class SmarthomeUpdater extends Receiver{

    private final SmarthomeManager smarthome;   //  reference to the smarthome to update

    public SmarthomeUpdater( String endPointName, SmarthomeManager smarthome, IConfiguration configuration ) {

            //  each component need a unique endpointName in order for the underlying management layer to been able to forward
            //  to the correct receivers the messages(endpointName = username = email)
            super( endPointName, configuration );
            this.smarthome = smarthome;

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
            this.smarthome.addLocation( location, lID, hostname, Integer.parseInt( port ), false );

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

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.addSubLocation( location, sublocation, subID, false );

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

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.addDevice( location, sublocation, dID, name, DeviceType.StringToType( type ), false );

        }

        /**
         * Method automatically called when a new request of renaming a location is received
         * @param username Username associated on the message(needed by Federico)
         * @param old_name Current name of location to be renamed
         * @param new_name New name to associate to the location
         */
        @Override
        protected void renameLocation( String username, String old_name, String new_name ) {

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.changeLocationName( old_name, new_name, false );

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

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.changeSublocationName( location, old_name, new_name, false );

        }

        /**
         * Method automatically called when a new request of renaming a device is received
         * @param username Username associated on the message(needed by Federico)
         * @param old_name Current name of device to be renamed
         * @param new_name New name to associate to the device
         */
        @Override
        protected void renameDevice( String username, String old_name, String new_name ) {

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.changeDeviceName( old_name, new_name, false );

        }

        /**
         * Method automatically called when a new request of removing a location is received
         * @param username Username associated on the message(needed by Federico)
         * @param location Name of the location to be removed
         */
        @Override
        protected void removeLocation( String username, String location ){

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.removeLocation( location, false );

        }

        /**
         * Method automatically called when a new request of removing a subLocation is received
         * @param username Username associated on the message(needed by Federico)
         * @param location Location in which the subLocation is deployed
         * @param sublocation Name of the subLocation to be removed
         */
        @Override
        protected void removeSubLocation( String username, String location, String sublocation ){

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.removeSublocation( location, sublocation, false );

        }

    /**
     * Method automatically called when a new request of removing a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param name Name of the device to be removed
     * @param dID Unique identifier of the device(needed by Federico)
     */
        @Override
        protected void removeDevice( String username, String name, String dID ){

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.removeDevice( name, false );
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

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.changeDeviceSubLocation( location, sublocation, new_sublocation, false );

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

            //  SmarthomeManager.class uses device names as unique identifier
            String name = this.smarthome.giveDeviceNameById( dID );

            //  updating of the shared smartHome(can be already updated)
            this.smarthome.performAction( name, action, value, timestamp, false );

        }
}