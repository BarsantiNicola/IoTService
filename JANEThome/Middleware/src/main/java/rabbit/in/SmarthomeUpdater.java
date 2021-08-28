package rabbit.in;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import config.interfaces.ConfigurationInterface;
import iot.SmarthomeDevice;
import iot.SmarthomeManager;
import org.apache.commons.lang.SerializationUtils;
import rabbit.EndPoint;
import rabbit.msg.DeviceUpdate;
import java.io.IOException;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//  Class designed to receive the updates from the middleware and update the smarthome associated with
//  a user. The class is similar to the class WebServer.rabbit.in.WebUpdateReceiver but necessary in order
//  to update the smarthome even if no client in currectly connected(so when no websockets are present to perform the update)
public class SmarthomeUpdater extends EndPoint implements Consumer{

    private final Logger logger;
    private final SmarthomeManager smarthome;   //  reference to the smarthome to update

    public SmarthomeUpdater(String endPointName, SmarthomeManager smarthome, ConfigurationInterface configuration ){

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if( this.logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

        this.smarthome = smarthome;

        //  initialization of parent class, cannot be performed into constructor
        //  (EJBs and in particular ConfigurationInterface is not available into constructors)
        if( super.inizialize( configuration )){
            this.logger.info( "Connection with rabbitMQ message exchange correctly executed" );

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                DeviceUpdate message;

                try {

                    message = (DeviceUpdate) SerializationUtils.deserialize( delivery.getBody() );
                    switch( message.getUpdateType() ) {

                        case ADD_LOCATION:

                            if( message.areSet("location", "address", "port" ))
                                this.smarthome.addLocation( message.getData("location"), message.getData("address"), Integer.parseInt(message.getData( "port")), false );
                            break;

                        case ADD_SUB_LOCATION:

                            if( message.areSet("location", "sublocation" ))
                                this.smarthome.addSubLocation( message.getData("location"), message.getData("sublocation"), false );
                            break;

                        case ADD_DEVICE:

                            if( message.areSet("location", "sublocation", "name", "type", "dID" ))
                                this.smarthome.addDevice( message.getData("location"), message.getData("sublocation"), message.getData("dID"),
                                            message.getData("name"), SmarthomeDevice.DeviceType.StringToType(message.getData("type")), false );

                            break;

                        case RENAME_LOCATION:

                            if( message.areSet("old_name", "new_name" ))
                                this.smarthome.changeLocationName( message.getData("old_name"), message.getData("new_name"), false );
                            break;

                        case RENAME_SUB_LOCATION:

                            if( message.areSet("location", "old_name", "new_name" ))
                                this.smarthome.changeSublocationName( message.getData("location"), message.getData("old_name"), message.getData("new_name"), false );
                            break;

                        case RENAME_DEVICE:

                            if( message.areSet("old_name", "new_name" ))
                                this.smarthome.changeDeviceName( message.getData("old_name"), message.getData("new_name"), false );
                            break;

                        case REMOVE_LOCATION:

                            if( message.areSet("location" ))
                                this.smarthome.removeLocation( message.getData("location"), false );
                            break;

                        case REMOVE_SUB_LOCATION:

                            if( message.areSet("location", "sublocation" ))
                                this.smarthome.removeSublocation( message.getData("location"), message.getData("sublocation"), false );
                            break;

                        case REMOVE_DEVICE:

                            if( message.areSet("name" ))
                                this.smarthome.removeDevice( message.getData("name"), false );
                            break;

                        case CHANGE_DEVICE_SUB_LOCATION:

                            if( message.areSet("location", "sublocation", "name" ))
                                this.smarthome.changeDeviceSubLocation( message.getData( "location" ), message.getData( "sublocation"), message.getData("name"), false );
                            break;

                        case UPDATE:

                            if( message.areSet("device_name", "action", "value" ))
                                this.smarthome.performAction( message.getData("device_name"), message.getData("action"), message.getData("value"), false );
                            break;

                        default:
                    }

                }catch( Exception e ) {

                    e.printStackTrace();

                }

            };

            try {

                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind( queueName, "DeviceUpdate", endPointName);
                channel.basicConsume( queueName, true, deliverCallback, consumerTag -> {});
                this.logger.info( "Configuration of rabbitMQ client correctly executed. Client ready to exchange messages" );

            } catch (IOException e) {

                e.printStackTrace();
                this.logger.info( "An error has occurred during the rabbitMQ client configuration" );
            }

        }else
            logger.severe( "Error, connection with the rabbitMQ message exchange failed" );
    }

    //  called when a new message is received and correctly processed
    public void handleConsumeOk( String consumerTag ){

        this.logger.info( "Message " + consumerTag + " processed" );

    }

   //  called when a new message is delivered
    @SuppressWarnings( "all" )
    public void handleDelivery(String consumerTag, Envelope env,
                               BasicProperties props, byte[] body){
        Map map = (Map)SerializationUtils.deserialize(body);
        this.logger.info("Message Number "+ map.get( "message number" ) + " received but not managed" );

    }

    //  called when a new message is not managed and leaved into the queue
    public void handleCancel( String consumerTag ) {
        this.logger.info( "Message received unhandled. Starting management" );
    }

    //  called when a new message is correctly removed from the queue without a management
    public void handleCancelOk( String consumerTag ) {
        this.logger.info( "Message received unhandled. Operation correctly aborted" );
    }

    //  called when a previously unmanaged message is recovered
    public void handleRecoverOk( String consumerTag ) {
        this.logger.info( "Recovery of previously unhandled message. Operation correctly done" );
    }

    //  called when the rabbitMQ client receive a request to be terminated
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException arg1 ) {
        this.logger.info( "Request to close the rabbitMQ client received" );
    }

}