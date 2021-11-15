package rabbit.out.beans;

//  internal services
import rabbit.EndPoint;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.out.interfaces.IRabbitSender;
import config.interfaces.IConfiguration;

//  exceptions
import java.io.IOException;

//  ejb3.0

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

//  collections
import java.util.List;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//  utils
import java.io.Serializable;
import org.apache.commons.lang.SerializationUtils;


/**
 * Class designed to send update messages by rabbitMQ using topics
 */
@Stateless
public class UpdateSender extends EndPoint implements IRabbitSender {

    private final Logger logger;

    @EJB
    IConfiguration configuration;

    public UpdateSender(){

        this.logger = LogManager.getLogger( getClass().getName() );

    }

    /**
     * Needed to inizialize the rabbitMQ outside constructor(EJB not available at constructor time)
     */
    @PostConstruct
    private void init(){

        if(super.inizialize(configuration))
            logger.info( "RabbitMQ client ready to send messages" );
        else
            logger.error( "An error has occurred during the client initialization" );

    }

    /**
     * Method designed to send a message via rabbitMQ. The function extract from the message the requests,
     * verify them and remove them from the message if invalid then send the rebuilt message to the destinations
     * @param message {@link DeviceUpdateMessage} Set of requests to be sent
     * @return The number of requests composing the message sent
     */
    public int sendMessage( DeviceUpdateMessage message ){

        int sentCount = 0;  //  counter for sent messages

        //  verification of destination presence
        String destination = message.getDestination();
        if( destination == null || destination.length() == 0 ){

            this.logger.error( "Error, a message must contains a destination field" );
            return sentCount;

        }

        //  verification that the destination is valid(it must be an email)
        if( destination.indexOf( '@' ) == -1 ) {

            this.logger.error( "Error, invalid destination. A destination can consist of a user email or the keyword 'db'" );
            return sentCount;

        }

        //  a message might contains more requests
        List<DeviceUpdate> updates = message.getAllDeviceUpdate();

        //  for each request we will verify its consistence, if invalid we simply remove it from the message
        for( DeviceUpdate update : updates )
            if( this.verifyUpdate( update ))
                sentCount++;
            else
                message.removeUpdate( update );

        if( sentCount > 0 ) {

            //  each message will be forwarded to the selected keyword(all the session associated with the user)
            this.sendMessage( message, destination );
            logger.info( "Sent " + sentCount + " updates of " + updates.size() + " to " + destination );

            //  for giving persistence to the update we send a copy to the database manager
            this.sendMessage( message, "db" );
            logger.info( "Sent " + sentCount + " updates of " + updates.size() + " to the database for information storing" );

        }else
            logger.error( "No valid updates to be sent. Abort" );

        return sentCount;

    }


    ////////--  UTILITIES  --////////


    /**
     * Message verification. For each message type verifies that all the mandatory fields are present
     * @param update A request to be checked
     * @return True if the request contains all the mandatory fields false otherwise
     */
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
                return update.areSet( "dID", "name", "dID" );

            case RENAME_DEVICE:
                return update.areSet( "dID", "new_name", "old_name" );

            case CHANGE_DEVICE_SUB_LOCATION:
                return update.areSet( "location", "sublocation", "dID", "name" );

            case UPDATE:
                return update.areSet( "dID", "action", "value" );

        }

        this.logger.error( "Error during message verification. Unmatched update type" );
        return false;

    }

    /**
     * Method to send a message via rabbitMQ. Returns true in case of success
     * @param object A rabbitMQ serialized version of DeviceUpdate.class
     * @param uID Keyword for forwarding the messages to the right destinations
     */
    private void sendMessage( Serializable object, String uID ){

        try{

            logger.info("Sending a new message to " + uID );
            channel.basicPublish( "DeviceUpdate", uID, null, SerializationUtils.serialize( object ));

        }catch( IOException e ){

            logger.error( "Error, unable to send the message" );
            e.printStackTrace();

        }
    }

}

