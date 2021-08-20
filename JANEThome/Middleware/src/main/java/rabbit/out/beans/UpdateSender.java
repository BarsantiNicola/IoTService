package rabbit.out.beans;

import com.google.gson.Gson;
import iot.SmarthomeDevice;
import iot.SmarthomeManager;
import rabbit.EndPoint;
import java.io.IOException;
import java.io.Serializable;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import rabbit.out.DeviceUpdate;
import rabbit.out.DeviceUpdateMessage;
import rabbit.out.interfaces.SenderInterface;
import org.apache.commons.lang.SerializationUtils;

//  EJB class to send notification messages by rabbitMQ. Can be used to send
//  messages in a non-blocking way to two different targets:
//    - Users by using their email as keyword
//    - Database Connectors by using the keyword 'db'
@Stateless
public class UpdateSender extends EndPoint implements SenderInterface {

    private final Logger logger;

    public UpdateSender(){

        this.logger = Logger.getLogger( getClass().getName() );

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }
    }

    //  split the sending of the message in several distinct message exchange(one per DeviceUpdate).
    //  The function automatically verifies the correctness of the updates and only if they are correct they will be sent.
    //  Returns the number of message sent
    public int sendMessage( DeviceUpdateMessage message ){

        int sentCount = 0;

        //  verification that the destination is present
        String destination = message.getDestination();
        if( destination == null || destination.length() == 0 ) {
            this.logger.severe("Error, a message must contain a destination field");
            return sentCount;
        }

        //  verificationi that the destination is valid
        if( destination.indexOf( '@' ) == -1 && destination.compareTo( "db" ) != 0 ) {
            this.logger.severe("Error, invalid destination. A destination can consist of a user email or the keyword 'db'");
            return sentCount;
        }

        List<DeviceUpdate> updates = message.getAllDeviceUpdate();

        //  getting the updates. For each update we will verify its consistence and then send it
        for( DeviceUpdate update : updates )
            if( this.verifyUpdate(update) && this.sendMessage( update , destination ))
                sentCount++;
            else
                this.logger.severe( "The current update will not been sent" );

        return sentCount;
    }

    //  message verification. For each message type verifies that all the mandatory fields are present
    private boolean verifyUpdate( DeviceUpdate update ){

        Gson gson = new Gson();
        System.out.println(" UPDATE " + gson.toJson(update));
        switch( update.getUpdateType() ){
            case ADD_LOCATION:
                return update.areSet("location", "address", "port" );

            case REMOVE_LOCATION:
                return update.areSet( "location" );

            case RENAME_LOCATION:
                return update.areSet( "old_name", "new_name" );

            case ADD_SUB_LOCATION: case REMOVE_SUB_LOCATION:
                return update.areSet( "location", "sublocation" );

            case RENAME_SUB_LOCATION:
                return update.areSet( "location", "old_name", "new_name" );

            case ADD_DEVICE:
                return update.areSet( "location", "sublocation", "dID", "name", "type" );

            case REMOVE_DEVICE:
                return update.areSet( "dID", "name" );

            case RENAME_DEVICE:
                return update.areSet( "dID", "new_name", "old_name" );

            case CHANGE_DEVICE_SUB_LOCATION:
                return update.areSet( "location", "sublocation", "dID", "name" );

            case UPDATE:
                return update.areSet( "dID" , "device_name", "action", "value" );

        }
        this.logger.severe( "ERROR UNMATCHED TYPE" );
        return false;
    }

    //  private function to send a message via rabbitMQ. It returns true in case of success
    private boolean sendMessage( Serializable object, String uID ){

            try{
                logger.info("Sending message: " + object );
                channel.basicPublish("DeviceUpdate", uID, null, SerializationUtils.serialize(object));
                return true;

            }catch(IOException e){

                logger.severe("Error, unable to send the message");
                e.printStackTrace();
                return false;

            }
    }

    //  private function to update the smarthome definition used by the web server sessions
    private boolean updateSmartHome( DeviceUpdate message, String username ){

        SmarthomeManager smarthome;
        InitialContext context = null;
        try{

            context = new InitialContext();
            smarthome = (SmarthomeManager) context.lookup("smarthome_" + username);
            if( smarthome == null ){

                //  TODO To be removed and substitute with request to the db to build the smarthome
                smarthome = SmarthomeManager.createTestingEnvironment( username );
                context.bind("smarthome_" + username, smarthome);
            }

        } catch (NamingException e) {

            //  TODO To be removed and substitute with request to the db to build the smarthome
            smarthome = SmarthomeManager.createTestingEnvironment( username );

            if( context != null ) {

                try {

                    context.bind("smarthome_" + username, smarthome);

                } catch (NamingException namingException) {

                    logger.info("Error during the save of the smarthome");
                    namingException.printStackTrace();

                }
            }

        }

        switch( message.getUpdateType() ){
            case ADD_LOCATION:
                return smarthome.addLocation(
                        message.getData("location"),
                        message.getData("address"),
                        Integer.parseInt(message.getData("port")), false);

            case ADD_SUB_LOCATION:
                return smarthome.addSubLocation(
                        message.getData( "location" ),
                        message.getData( "sublocation"), false );

            case ADD_DEVICE:
                return smarthome.addDevice(
                        message.getData( "location" ),
                        message.getData("sublocation"),
                        message.getData("dID"),
                        message.getData("name"),
                        SmarthomeDevice.DeviceType.StringToType(message.getData("type")), false );

            case RENAME_LOCATION:
                return smarthome.changeLocationName(
                        message.getData("old_name"),
                        message.getData( "new_name"), false );

            case RENAME_SUB_LOCATION:
                return smarthome.changeSublocationName(
                        message.getData( "location" ),
                        message.getData("old_name"),
                        message.getData( "new_name"), false );

            case RENAME_DEVICE:
                return smarthome.changeDeviceName(
                        message.getData("old_name"),
                        message.getData( "new_name"), false );

            case REMOVE_LOCATION:
                return smarthome.removeLocation(
                        message.getData( "location" ), false );

            case REMOVE_SUB_LOCATION:
                return smarthome.removeSublocation(
                        message.getData( "location" ),
                        message.getData("sublocation" ), false );

            case REMOVE_DEVICE:
                return smarthome.removeDevice(
                        message.getData("name"), false );

            case CHANGE_DEVICE_SUB_LOCATION:
                return smarthome.changeDeviceSubLocation(
                        message.getData("location"),
                        message.getData( "sublocation" ),
                        message.getData("name"), false );

            case UPDATE:
                return smarthome.performAction(
                        message.getData( "name" ),
                        message.getData( "action" ),
                        message.getData( "value" ) , false );

            default:
                return false;
        }
    }
}

