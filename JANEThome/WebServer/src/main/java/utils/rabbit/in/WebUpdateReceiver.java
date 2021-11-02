package utils.rabbit.in;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import config.interfaces.ConfigurationInterface;
import iot.SmarthomeDevice;
import iot.SmarthomeManager;
import iot.SmarthomeWebDevice;
import rabbit.EndPoint;
import org.apache.commons.lang.SerializationUtils;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;

import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//  Class designed to receive the updates from the middleware and notify the associated web client
public class WebUpdateReceiver extends EndPoint implements Consumer{

    private final Logger logger;
    private final Session target;
    private final SmarthomeManager smarthome;

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
        this.target = websocket;

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            Gson gson = new Gson();
            DeviceUpdateMessage request;
            HashMap<String, Object> response = new HashMap<>();
            HashMap<String, String> data = new HashMap<>();

            try {

                request = (DeviceUpdateMessage) SerializationUtils.deserialize( delivery.getBody() );

                for( DeviceUpdate message : request.getAllDeviceUpdate()) {
                    switch (message.getUpdateType()) {

                        case ADD_LOCATION:

                            if (message.areSet("location", "locID", "address", "port") &&
                                    this.smarthome.addLocation(message.getData("location"),
                                            message.getData( "locID"), message.getData("address"),
                                            Integer.parseInt(message.getData("port")), false)) {

                                data.put("location", message.getData("location"));
                                response.put("type", "ADD_LOCATION");
                                response.put("data", data);

                            }
                            break;

                        case ADD_SUB_LOCATION:

                            if (message.areSet("location", "sublocation", "sublocID" ) &&
                                    this.smarthome.addSubLocation(message.getData("location"), message.getData("sublocation"), message.getData("sublocID"), false)) {

                                data.put("location", message.getData("location"));
                                data.put("sublocation", message.getData("sublocation"));
                                response.put("type", "ADD_SUBLOCATION");
                                response.put("data", data);

                            }
                            break;

                        case ADD_DEVICE:

                            if (message.areSet("location", "sublocation", "name", "type", "dID") &&
                                    this.smarthome.addDevice(message.getData("location"), message.getData("sublocation"), message.getData("dID"),
                                            message.getData("name"), SmarthomeDevice.DeviceType.StringToType(message.getData("type")), false)) {

                                data.put("location", message.getData("location"));
                                data.put("sublocation", message.getData("sublocation"));
                                data.put("name", message.getData("name"));
                                data.put("type", message.getData("type"));

                                response.put("type", "ADD_DEVICE");
                                response.put("data", data);

                            }
                            break;

                        case RENAME_LOCATION:

                            if (message.areSet("old_name", "new_name") &&
                                    this.smarthome.changeLocationName(message.getData("old_name"), message.getData("new_name"), false)) {
                                data.put("old_name", message.getData("old_name"));
                                data.put("new_name", message.getData("new_name"));
                                response.put("type", "RENAME_LOCATION");
                                response.put("data", data);

                            }
                            break;

                        case RENAME_SUB_LOCATION:

                            if (message.areSet("location", "old_name", "new_name") &&
                                    this.smarthome.changeSublocationName(message.getData("location"), message.getData("old_name"), message.getData("new_name"), false)) {

                                data.put("location", message.getData("location"));
                                data.put("old_name", message.getData("old_name"));
                                data.put("new_name", message.getData("new_name"));
                                response.put("type", "RENAME_SUBLOCATION");
                                response.put("data", data);

                            }
                            break;

                        case RENAME_DEVICE:

                            if (message.areSet("old_name", "new_name") &&
                                    this.smarthome.changeDeviceName(message.getData("old_name"), message.getData("new_name"), false)) {

                                data.put("old_name", message.getData("old_name"));
                                data.put("new_name", message.getData("new_name"));
                                response.put("type", "RENAME_DEVICE");
                                response.put("data", data);

                            }

                            break;

                        case REMOVE_LOCATION:

                            if (message.areSet("location") &&
                                    this.smarthome.removeLocation(message.getData("location"), false)) {

                                data.put("location", message.getData("location"));
                                response.put("type", "REMOVE_LOCATION");
                                response.put("data", data);

                            }
                            break;

                        case REMOVE_SUB_LOCATION:

                            if (message.areSet("location", "sublocation")){
                                List<SmarthomeWebDevice> devices = smarthome.giveSublocationDevices( message.getData("location"), message.getData("sublocation"));

                                if( this.smarthome.removeSublocation(message.getData("location"), message.getData("sublocation"), false)) {

                                    devices.forEach( device -> {
                                        data.put("location", message.getData("location"));
                                        data.put("sublocation", "default");
                                        data.put("name", device.giveDeviceName());
                                        response.put("type", "CHANGE_SUBLOC");
                                        response.put("data", data);

                                        try {
                                            target.getBasicRemote().sendText(gson.toJson(response));
                                        }catch( IOException e ){}

                                        data.clear();
                                        response.clear();
                                    });

                                    data.put("location", message.getData("location"));
                                    data.put("sublocation", message.getData("sublocation"));
                                    response.put("type", "REMOVE_SUBLOCATION");
                                    response.put("data", data);
                                }

                            }
                            break;

                        case REMOVE_DEVICE:

                            if (message.areSet("name") &&
                                    this.smarthome.removeDevice(message.getData("name"), false)) {

                                data.put("name", message.getData("name"));
                                response.put("type", "REMOVE_DEVICE");
                                response.put("data", data);

                            }
                            break;

                        case CHANGE_DEVICE_SUB_LOCATION:

                            if (message.areSet("location", "sublocation", "name") &&
                                    this.smarthome.changeDeviceSubLocation(message.getData("location"), message.getData("sublocation"), message.getData("name"), false)) {

                                data.put("location", message.getData("location"));
                                data.put("sublocation", message.getData("sublocation"));
                                data.put("name", message.getData("name"));

                                response.put("type", "CHANGE_SUBLOC");
                                response.put("data", data);

                            }
                            break;

                        case UPDATE:
                            System.out.println("STARTING UPDATE");
                            if(message.areSet("dID", "action", "value")) {
                                System.out.println("STARTING UPDATE SELECTION");
                                String name = this.smarthome.giveDeviceNameById(message.getData("dID"));
                                if (this.smarthome.performAction(name, message.getData("action"), message.getData("value"), message.giveConvertedTimestamp(), false)) {

                                    System.out.println("Sending updated!!!!!");
                                    data.put("device_name", name);
                                    data.put("action", message.getData("action"));
                                    data.put("value", message.getData("value"));

                                    response.put("type", "UPDATE");
                                    response.put("data", data);

                                }else
                                    System.out.println("STRANOOO");
                            }
                            break;

                        default:
                    }

                    if (response.containsKey("type"))
                        target.getBasicRemote().sendText(gson.toJson(response));
                }

            }catch( Exception e ) {

                e.printStackTrace();

            }


        };

        if( channel != null && connection != null ) {

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