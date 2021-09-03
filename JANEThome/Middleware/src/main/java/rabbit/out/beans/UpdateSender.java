package rabbit.out.beans;

import config.interfaces.ConfigurationInterface;
import rabbit.EndPoint;
import java.io.IOException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.out.interfaces.SenderInterface;
import org.apache.commons.lang.SerializationUtils;

//  EJB class to send notification messages by rabbitMQ. Can be used to send
//  messages in a non-blocking way to two different targets:
//    - Users by using their email as keyword
//    - Database Connectors by using the keyword 'db'
@Stateless
public class UpdateSender extends EndPoint implements SenderInterface {

    private final Logger logger;

    @EJB
    ConfigurationInterface configuration;

    public UpdateSender(){

        this.logger = Logger.getLogger( getClass().getName() );

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter( new SimpleFormatter() );
            logger.addHandler( consoleHandler );

        }
    }

    @PostConstruct
    //  needed to inizialize the parent class out of the constructor(ConfigurationInterface not available into constructor)
    private void init(){

        if( super.inizialize( configuration ))
            logger.info( "RabbitMQ client ready to send messages" );
        else
            logger.severe( "An error has occurred during the client initialization" );

    }

    //  split the sending of the message in several distinct message exchange(one per DeviceUpdate).
    //  The function automatically verifies the correctness of the updates and only if they are correct they will be sent.
    //  Returns the number of message sent
    public int sendMessage( DeviceUpdateMessage message ){

        int sentCount = 0;
        //  verification that the destination is present
        String destination = message.getDestination();
        if( destination == null || destination.length() == 0 ) {

            this.logger.severe( "Error, a message must contain a destination field" );
            return sentCount;

        }

        //  verificationi that the destination is valid
        if( destination.indexOf( '@' ) == -1 ) {

            this.logger.severe( "Error, invalid destination. A destination can consist of a user email or the keyword 'db'" );
            return sentCount;

        }

        List<DeviceUpdate> updates = message.getAllDeviceUpdate();

        //  getting the updates. For each update we will verify its consistence and then send it
        for( DeviceUpdate update : updates )
            if( this.verifyUpdate(update))
                sentCount++;
            else {
                message.removeUpdate( update );
                this.logger.severe("The current update will not been sent");
            }
        this.sendMessage( message , destination );
        logger.info( "Sent " + sentCount + " updates of " + updates.size() + " to " + destination );
        this.sendMessage( message, "db" );
        logger.info( "Sent " + sentCount + " updates of " + updates.size() + " to the database for information storing" );
        return sentCount;
    }

    //  message verification. For each message type verifies that all the mandatory fields are present
    private boolean verifyUpdate( DeviceUpdate update ){

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
                return update.areSet( "dID", "action", "value" );

        }
        this.logger.severe( "Error during message verification. Unmatched update type" );
        return false;
    }

    //  private function to send a message via rabbitMQ. It returns true in case of success
    private void sendMessage( Serializable object, String uID ){

        try{

            logger.info("Sending a new message to " + uID );
            channel.basicPublish("DeviceUpdate", uID, null, SerializationUtils.serialize(object));

        }catch(IOException e){

            logger.severe("Error, unable to send the message");
            e.printStackTrace();

        }
    }

}

