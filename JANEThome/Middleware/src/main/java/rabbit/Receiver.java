package rabbit;

//  rabbitMQ
import com.rabbitmq.client.*;
import config.interfaces.ConfigurationInterface;
import org.apache.commons.lang.SerializationUtils;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//  internal services
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;

//  exceptions
import java.io.IOException;

//  collections
import java.util.Date;
import java.util.Map;

/**
 * Basic class for rabbitMQ receiver creation. The class gives a set of functions to be defined that
 * will be called whenever the associated request is received
 */
public abstract class Receiver extends EndPoint implements Consumer {

    protected final Logger logger;

    protected Receiver( String endpointName, ConfigurationInterface configuration ){

        super.inizialize( configuration );
        this.logger = LogManager.getLogger( getClass().getName() );

        //  callback function called when a new message is received
        DeliverCallback deliverCallback = ( consumerTag, delivery ) -> {

            try {

                DeviceUpdateMessage message = (DeviceUpdateMessage) SerializationUtils.deserialize( delivery.getBody() );

                //  each message can contain several requests
                for( DeviceUpdate request : message.getAllDeviceUpdate())
                        switch(request.getUpdateType()) {  //  filtering of the request

                            case ADD_LOCATION:
                                //  many components can be connected to rabbitMQ, it's better to consider the received data as tainted
                                //  and perform a quick verification that into the message the mandatory fields are present
                                if( request.areSet( "location", "locID", "address", "port" ))
                                    this.addLocation(
                                            message.getDestination(),
                                            request.getData( "location" ),
                                            request.getData( "locID" ),
                                            request.getData( "address" ),
                                            request.getData( "port" ));
                                break;

                            case ADD_SUB_LOCATION:
                                if( request.areSet("location", "sublocation", "sublocID" ))
                                    this.addSubLocation(
                                            message.getDestination(),
                                            request.getData( "location" ),
                                            request.getData( "sublocation" ),
                                            request.getData( "sublocID" ));
                                break;

                            case ADD_DEVICE:
                                if( request.areSet( "location", "sublocation", "name", "type", "dID" ))
                                    this.addDevice(
                                            message.getDestination(),
                                            request.getData( "location" ),
                                            request.getData( "sublocation" ),
                                            request.getData( "name" ),
                                            request.getData( "type" ),
                                            request.getData( "dID"));
                                break;

                            case RENAME_LOCATION:
                                if( request.areSet( "old_name", "new_name" ))
                                    this.renameLocation(
                                            message.getDestination(),
                                            request.getData( "old_name" ),
                                            request.getData( "new_name" ));
                                break;

                            case RENAME_SUB_LOCATION:
                                if( request.areSet( "location", "old_name", "new_name" ))
                                    this.renameSubLocation(
                                            message.getDestination(),
                                            request.getData( "location" ),
                                            request.getData( "old_name" ),
                                            request.getData( "new_name" ));
                                break;

                            case RENAME_DEVICE:
                                if( request.areSet( "old_name", "new_name" ))
                                    this.renameDevice(
                                            message.getDestination(),
                                            request.getData( "old_name" ),
                                            request.getData( "new_name" ));
                                break;

                            case REMOVE_LOCATION:
                                if( request.areSet( "location" ))
                                    this.removeLocation(
                                            message.getDestination(),
                                            request.getData( "location" ));
                                break;

                            case REMOVE_SUB_LOCATION:
                                if( request.areSet( "location", "sublocation" ))
                                    this.removeSubLocation(
                                            message.getDestination(),
                                            request.getData( "location" ),
                                            request.getData( "sublocation" ));

                            case REMOVE_DEVICE:
                                if( request.areSet( "name" ))
                                    this.removeDevice(
                                            message.getDestination(),
                                            request.getData( "name" ));
                                break;

                            case CHANGE_DEVICE_SUB_LOCATION:
                                if( request.areSet( "location", "sublocation", "name" ))
                                    this.changeDeviceSubLocation(
                                            message.getDestination(),
                                            request.getData( "location" ),
                                            request.getData( "sublocation" ),
                                            request.getData( "name" ));
                                break;

                            case UPDATE:
                                if( request.areSet( "dID", "action", "value" ))
                                    this.executeAction(
                                            message.getDestination(),
                                            request.getData( "dID" ),
                                            request.getData( "action" ),
                                            request.getData( "value" ),
                                            request.giveConvertedTimestamp());
                                break;

                            default:
                        }

            }catch( Exception e ) {

                e.printStackTrace();

            }


        };

        if( channel != null && connection != null ) {

            String queueName;
            try {

                queueName = channel.queueDeclare().getQueue();
                channel.queueBind( queueName, "DeviceUpdate", endpointName );
                channel.basicConsume( queueName, true, deliverCallback, consumerTag -> {});

            } catch (IOException e) {

                e.printStackTrace();

            }
        }
    }

    /**
     * Method automatically called when a new request of adding a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name to be associated to the new location
     * @param locID Unique identifier of the location(needed by Riccardo)
     * @param hostname hostname of the destination smarthome location
     * @param port port of the destination smarthome location
     */
    protected abstract void addLocation( String username, String location, String locID, String hostname, String port );

    /**
     * Method automatically called when a new request of adding a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location in which insert the subLocation
     * @param sublocation Name to be associated with the subLocation
     * @param subID Unique identifier of the subLocation(needed by Riccardo)
     */
    protected abstract void addSubLocation( String username, String location, String sublocation, String subID );

    /**
     * Method automatically called when a new request of adding a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location in which insert the subLocation
     * @param sublocation Name to be associated with the subLocation
     * @param name Unique identifier of the subLocation(needed by Riccardo)
     * @param type Type of the device(LIGHT,FAN,DOOR,THERMOSTAT,CONDITIONER)
     */
    protected abstract void addDevice( String username, String location, String sublocation, String name, String type, String dID );

    /**
     * Method automatically called when a new request of renaming a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param old_name Current name of location to be renamed
     * @param new_name New name to associate to the location
     */
    protected abstract void renameLocation( String username, String old_name, String new_name );

    /**
     * Method automatically called when a new request of renaming a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the subLocation is deployed
     * @param old_name Current name of subLocation to be renamed
     * @param new_name New name to associate to the subLocation
     */
    protected abstract void renameSubLocation( String username, String location, String old_name, String new_name );

    /**
     * Method automatically called when a new request of renaming a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param old_name Current name of device to be renamed
     * @param new_name New name to associate to the device
     */
    protected abstract void renameDevice( String username, String old_name, String new_name );

    /**
     * Method automatically called when a new request of removing a location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Name of the location to be removed
     */
    protected abstract void removeLocation( String username, String location );

    /**
     * Method automatically called when a new request of removing a subLocation is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the subLocation is deployed
     * @param sublocation Name of the subLocation to be removed
     */
    protected abstract void removeSubLocation( String username, String location, String sublocation );

    /**
     * Method automatically called when a new request of removing a device is received
     * @param username Username associated on the message(needed by Federico)
     * @param name Name of the device to be removed
     */
    protected abstract void removeDevice( String username, String name );

    /**
     * Method automatically called when a new request of changing a device location is received
     * @param username Username associated on the message(needed by Federico)
     * @param location Location in which the device is deployed
     * @param sublocation Name of the current subLocation of the device
     * @param new_sublocation Name of the subLocation in which move the device
     */
    protected abstract void changeDeviceSubLocation( String username, String location, String sublocation, String new_sublocation );

    /**
     * Method automatically called when a new request of executing a device action is received
     * @param username Username associated on the message(needed by Federico)
     * @param dID Unique identifier associated with the device
     * @param action Name of the action to perform(OnOff, OpenClose...)
     * @param value Value to associate to the action(OnOff -> 0 close 1 open)
     * @param timestamp Timestamp representing the moment in which the action was performed
     */
    protected abstract void executeAction( String username, String dID, String action, String value, Date timestamp );

    /**
     * called when a new message is received and correctly processed
     */
    public void handleConsumeOk( String consumerTag ){

        this.logger.info( "Message " + consumerTag + " processed" );

    }

    /**
     * called when a new message is delivered
     */
    @SuppressWarnings( "all" )
    public void handleDelivery(String consumerTag, Envelope env, AMQP.BasicProperties props, byte[] body){

        Map map = (Map) SerializationUtils.deserialize(body);
        this.logger.info( "Message Number "+ map.get( "message number" ) + " received but not managed" );

    }

    /**
     * called when a new message is not managed and leaved into the queue
     */
    public void handleCancel( String consumerTag ) {

        this.logger.info( "Message received unhandled. Starting management" );

    }

    /**
     * called when a new message is correctly removed from the queue without a management
     */
    public void handleCancelOk( String consumerTag ) {

        this.logger.info( "Message received unhandled. Operation correctly aborted" );

    }

    /**
     * called when a previously unmanaged message is recovered
     */
    public void handleRecoverOk( String consumerTag ) {

        this.logger.info( "Recovery of previously unhandled message. Operation correctly done" );

    }

    /**
     * called when the rabbitMQ client receive a request to be terminated
     */
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException arg1 ) {

        this.logger.info( "Request to close the rabbitMQ client received" );

    }
}
