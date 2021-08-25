package utils.rabbit.in;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import config.beans.Configuration;
import config.interfaces.ConfigurationInterface;
import iot.SmarthomeDevice;
import iot.SmarthomeManager;
import rabbit.EndPoint;
import org.apache.commons.lang.SerializationUtils;
import rabbit.msg.DeviceUpdate;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//  Class designed to receive the updates from the middleware and notify the associated web client
public class WebUpdateReceiver extends EndPoint implements Consumer{

    private final Logger logger;
    private Session target;
    private final SmarthomeManager smarthome;
    private String endPointName;

    public WebUpdateReceiver( String endPointName, Session websocket, SmarthomeManager smarthome, ConfigurationInterface configuration ) {

        super.inizialize(configuration);
        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if (this.logger.getHandlers().length == 0) { //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }
        this.smarthome = smarthome;
        this.endPointName = endPointName;
        this.target = websocket;

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            Gson gson = new Gson();
            DeviceUpdate message;
            HashMap<String, Object> response = new HashMap<>();
            HashMap<String, String> data = new HashMap<>();

            try {

                message = (DeviceUpdate) SerializationUtils.deserialize( delivery.getBody() );
                switch( message.getUpdateType() ) {

                        case ADD_LOCATION:

                            if( message.areSet("location", "address", "port" ) &&
                                    this.smarthome.addLocation( message.getData("location"), message.getData("address"),
                                        Integer.parseInt(message.getData( "port")), false )) {

                                data.put( "location" , message.getData( "location" ));
                                response.put( "type", "ADD_LOCATION" );
                                response.put( "data", data );

                            }
                            break;

                        case ADD_SUB_LOCATION:

                            if( message.areSet("location", "sublocation" ) &&
                                    this.smarthome.addSubLocation( message.getData("location"), message.getData("sublocation"), false )) {

                                data.put( "location" , message.getData( "location" ));
                                data.put( "sublocation" , message.getData( "sublocation" ));
                                response.put("type", "ADD_SUBLOCATION" );
                                response.put( "data", data );

                            }
                            break;

                        case ADD_DEVICE:

                            if( message.areSet("location", "sublocation", "name", "type", "dID" ) &&
                                    this.smarthome.addDevice( message.getData("location"), message.getData("sublocation"), message.getData("dID"),
                                            message.getData("name"), SmarthomeDevice.DeviceType.StringToType(message.getData("type")), false )) {

                                data.put( "location" , message.getData( "location" ));
                                data.put( "sublocation" , message.getData( "sublocation" ));
                                data.put( "name" , message.getData( "name" ));
                                data.put( "type" , message.getData( "type" ));

                                response.put("type", "ADD_DEVICE" );
                                response.put( "data", data );
                                System.out.println("RESPONSE: " + gson.toJson(response));

                            }
                            break;

                        case RENAME_LOCATION:

                            if( message.areSet("old_name", "new_name" ) &&
                                    this.smarthome.changeLocationName( message.getData("old_name"), message.getData("new_name"), false )) {

                                data.put( "old_name" , message.getData( "old_name" ));
                                data.put( "new_name" , message.getData( "new_name" ));
                                response.put("type", "RENAME_LOCATION" );
                                response.put( "data", data );

                            }
                            break;

                        case RENAME_SUB_LOCATION:

                            if( message.areSet("location", "old_name", "new_name" ) &&
                                    this.smarthome.changeSublocationName( message.getData("location"), message.getData("old_name"), message.getData("new_name"), false )) {

                                data.put( "location" , message.getData( "location" ));
                                data.put( "old_name" , message.getData( "old_name" ));
                                data.put( "new_name" , message.getData( "new_name" ));
                                response.put("type", "RENAME_SUBLOCATION" );
                                response.put( "data", data );

                            }
                            break;

                        case RENAME_DEVICE:

                            if( message.areSet("old_name", "new_name" ) &&
                                    this.smarthome.changeDeviceName( message.getData("old_name"), message.getData("new_name"), false )) {

                                data.put( "old_name" , message.getData( "old_name" ));
                                data.put( "new_name" , message.getData( "new_name" ));
                                response.put("type", "RENAME_DEVICE" );
                                response.put( "data", data );

                            }
                            break;

                        case REMOVE_LOCATION:

                            if( message.areSet("location" ) &&
                                    this.smarthome.removeLocation( message.getData("location"), false )) {

                                data.put( "location" , message.getData( "location" ));
                                response.put("type", "REMOVE_LOCATION" );
                                response.put( "data", data );

                            }
                            break;

                        case REMOVE_SUB_LOCATION:

                            if( message.areSet("location", "sublocation" ) &&
                                    this.smarthome.removeSublocation( message.getData("location"), message.getData("sublocation"), false )) {

                                data.put( "location" , message.getData( "location" ));
                                data.put( "sublocation" , message.getData( "sublocation" ));
                                response.put("type", "REMOVE_SUBLOCATION" );
                                response.put( "data", data );

                            }
                            break;

                        case REMOVE_DEVICE:

                            if( message.areSet("name" ) &&
                                    this.smarthome.removeDevice( message.getData("name"), false )) {

                                data.put( "name" , message.getData( "name" ));
                                response.put("type", "REMOVE_DEVICE" );
                                response.put( "data", data );

                            }
                            break;

                        case CHANGE_DEVICE_SUB_LOCATION:

                            if( message.areSet("location", "sublocation", "name" ) &&
                                    this.smarthome.removeDevice( message.getData("name"), false )) {

                                data.put( "location" , message.getData( "location" ));
                                data.put( "sublocation" , message.getData( "sublocation" ));
                                data.put( "name" , message.getData( "name" ));

                                response.put("type", "CHANGE_SUBLOC" );
                                response.put( "data", data );

                            }
                            break;

                        case UPDATE:

                            if( message.areSet("device_name", "action", "value" ) &&
                                    this.smarthome.performAction( message.getData("device_name"), message.getData("action"), message.getData("value"), false )) {

                                data.put( "device_name" , message.getData( "device_name" ));
                                data.put( "action" , message.getData( "action" ));
                                data.put( "value", message.getData( "value" ));

                                response.put("type", "UPDATE" );
                                response.put( "data", data);

                            }
                            break;

                        default:
                    }

                    if( response.containsKey("type"))
                        target.getBasicRemote().sendText( gson.toJson(response));

            }catch( Exception e ) {

                e.printStackTrace();

            }

        };
        System.out.println("ENDING CONFIGURATION");
        if( channel != null && connection != null ) {
            System.out.println("ENDING2");
            String queueName;
            try {

                queueName = channel.queueDeclare().getQueue();
                channel.queueBind( queueName, "DeviceUpdate", endPointName);
                channel.basicConsume( queueName, true, deliverCallback, consumerTag -> {});

            } catch (IOException e) {

                e.printStackTrace();

            }
        }
    }

    //  called when consumer is registered
    public void handleConsumeOk(String consumerTag) {
        logger.info("Consumer "+consumerTag +" registered");
    }

   //  called when a new message is delivered
    @SuppressWarnings( "all" )
    public void handleDelivery(String consumerTag, Envelope env,
                               BasicProperties props, byte[] body){
        Map map = (Map)SerializationUtils.deserialize(body);
        logger.info("Message Number "+ map.get("message number") + " received.");

    }

    public void handleCancel( String consumerTag ) {}
    public void handleCancelOk( String consumerTag ) {}
    public void handleRecoverOk( String consumerTag ) {}
    public void handleShutdownSignal( String consumerTag, ShutdownSignalException arg1 ) {}
}