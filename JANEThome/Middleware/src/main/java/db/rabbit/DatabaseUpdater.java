package db.rabbit;

import com.rabbitmq.client.*;
import config.interfaces.ConfigurationInterface;
import db.interfaces.DBinterface;
import iot.SmarthomeDevice;
import org.apache.commons.lang.SerializationUtils;
import rabbit.EndPoint;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static rabbit.msg.IMessage.*;


@Startup
@Singleton
public class DatabaseUpdater extends EndPoint implements Consumer {

    @EJB
    private ConfigurationInterface configuration;

    @EJB
    private DBinterface dB;

    private Logger logger;

    public DatabaseUpdater() {
    }

    @PostConstruct
    public void init() {
        initLogger();

        if (super.inizialize(configuration)) {
            logger.info("Connection with rabbitMQ message exchange correctly executed");
//            configuration.setParameter("rest", "control_address", "test");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                DeviceUpdateMessage request;
                try {
                    request = (DeviceUpdateMessage) SerializationUtils.deserialize(delivery.getBody());
                    for (DeviceUpdate message : request.getAllDeviceUpdate()) {
                        switch (message.getUpdateType()) {

                            case ADD_LOCATION:

                                if (message.areSet(LOCATION, LOCID, ADDRESS, PORT) || request.getDestination() != null) {
                                    dB.addElementManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.ADD_LOCATION, message.getData(LOCID),
                                            message.getData(LOCATION), message.getData(ADDRESS),
                                            Integer.parseInt(message.getData(PORT)),
                                            null, null, null);
                                }
                                break;

                            case ADD_SUB_LOCATION:

                                if (message.areSet(SUBLOCATION, SUBLOCID) || request.getDestination() != null)
                                    dB.addElementManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.ADD_SUB_LOCATION, message.getData(SUBLOCID),
                                            message.getData(LOCATION), null,
                                            0, message.getData(SUBLOCATION), null, null);
                                break;

                            case ADD_DEVICE:

                                if (message.areSet(LOCATION, SUBLOCATION, NAME, TYPE, DID) || request.getDestination() != null)
                                    dB.addElementManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.ADD_LOCATION, message.getData(DID),
                                            message.getData(LOCATION), null, 0, message.getData(SUBLOCATION),
                                            message.getData(NAME),
                                            SmarthomeDevice.DeviceType.StringToType(message.getData(TYPE)));

                                break;

                            case RENAME_LOCATION:

                                if (message.areSet(OLD_NAME, NEW_NAME) || request.getDestination() != null)
                                    dB.renameElementManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.RENAME_LOCATION,
                                            message.getData(OLD_NAME), message.getData(NEW_NAME), null);
                                break;

                            case RENAME_SUB_LOCATION:

                                if (message.areSet(LOCATION, OLD_NAME, NEW_NAME) || request.getDestination() != null)
                                    dB.renameElementManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.RENAME_SUB_LOCATION,
                                            message.getData(OLD_NAME), message.getData(NEW_NAME), message.getData(LOCATION));
                                break;

                            case RENAME_DEVICE:

                                if (message.areSet(OLD_NAME, NEW_NAME) || request.getDestination() != null)
                                    dB.renameElementManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.RENAME_DEVICE,
                                            message.getData(OLD_NAME), message.getData(NEW_NAME), null);
                                break;

                            case REMOVE_LOCATION:

                                if (message.areSet(LOCATION) || request.getDestination() != null)
                                    dB.removeElementIntoManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.REMOVE_LOCATION, message.getData(LOCATION),
                                            null);
                                break;

                            case REMOVE_SUB_LOCATION:

                                if (message.areSet(LOCATION, SUBLOCATION) || request.getDestination() != null)
                                    dB.removeElementIntoManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.REMOVE_SUB_LOCATION, message.getData(LOCATION),
                                            message.getData(SUBLOCATION));
                                break;

                            case REMOVE_DEVICE:

                                if (message.areSet(NAME) || request.getDestination() != null)
                                    dB.removeElementIntoManager(request.getDestination(),
                                            DeviceUpdate.UpdateType.REMOVE_DEVICE, message.getData(NAME),
                                            null);
                                break;

                            case CHANGE_DEVICE_SUB_LOCATION:

                                if (message.areSet(LOCATION, SUBLOCATION, NAME) || request.getDestination() != null)
                                    dB.moveDevice(request.getDestination(), message.getData(LOCATION),
                                            message.getData(SUBLOCATION), message.getData(NAME));
                                break;

                            case UPDATE:

                                if (message.areSet(DEVICE_NAME, ACTION, VALUE) || request.getDestination() != null)
                                    dB.performAction(request.getDestination(), message.getData(DEVICE_NAME),
                                            message.getData(ACTION), message.getData(VALUE));
                                break;

                            default:
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            };

            try {

                String queueName = channel.queueDeclare().getQueue();
                String ENDPOINTNAME = "db";
                channel.queueBind(queueName, "DeviceUpdate", ENDPOINTNAME);
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                });
                this.logger.info("Configuration of rabbitMQ client correctly executed. Client ready to exchange messages");

            } catch (IOException e) {
                e.printStackTrace();
                this.logger.info("An error has occurred during the rabbitMQ client configuration");
            }
        } else
            logger.severe("Error, connection with the rabbitMQ message exchange failed");
    }

    //  called when a new message is received and correctly processed
    public void handleConsumeOk(String consumerTag) {
        this.logger.info("Message " + consumerTag + " processed");
    }

    //  called when a new message is delivered
    @SuppressWarnings("all")
    public void handleDelivery(String consumerTag, Envelope env,
                               AMQP.BasicProperties props, byte[] body) {
        Map map = (Map) SerializationUtils.deserialize(body);
        this.logger.info("Message Number " + map.get("message number") + " received but not managed");

    }

    //  called when a new message is not managed and leaved into the queue
    public void handleCancel(String consumerTag) {
        this.logger.info("Message received unhandled. Starting management");
    }

    //  called when a new message is correctly removed from the queue without a management
    public void handleCancelOk(String consumerTag) {
        this.logger.info("Message received unhandled. Operation correctly aborted");
    }

    //  called when a previously unmanaged message is recovered
    public void handleRecoverOk(String consumerTag) {
        this.logger.info("Recovery of previously unhandled message. Operation correctly done");
    }

    //  called when the rabbitMQ client receive a request to be terminated
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {
        this.logger.info("Request to close the rabbitMQ client received");
    }


    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initLogger() {

        if (this.logger != null)
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if (this.logger.getHandlers().length == 0) { //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }
    }


}
