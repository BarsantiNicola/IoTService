package utils.jms;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import jms.EndPoint;
import org.apache.commons.lang.SerializationUtils;
import weblogic.login.websockets.WebappEndpoint;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 * The endpoint that consumes messages off of the queue. Happens to be runnable.
 * @author syntx
 *
 */
public class WebUpdateReceiver extends EndPoint implements Consumer{

    private final Logger logger;
    private Session target;

    public WebUpdateReceiver(String endPointName, Session websocket){

        logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);

        DeliverCallback deliverCallback = (consumerTag, delivery) ->
            target.getBasicRemote().sendText(new String(delivery.getBody()));


        if( channel != null && connection != null ) {
            String queueName;
            try {

                queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, "DeviceUpdate", endPointName);
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

            } catch (IOException e) {

                e.printStackTrace();

            }
            target = websocket;
        }
    }

    /**
     * Called when consumer is registered.
     */
    public void handleConsumeOk(String consumerTag) {
        logger.info("Consumer "+consumerTag +" registered");
    }

    /**
     * Called when new message is available.
     */
    public void handleDelivery(String consumerTag, Envelope env,
                               BasicProperties props, byte[] body) throws IOException {
        Map map = (HashMap)SerializationUtils.deserialize(body);
        logger.info("Message Number "+ map.get("message number") + " received.");

    }

    public void handleCancel(String consumerTag) {}
    public void handleCancelOk(String consumerTag) {}
    public void handleRecoverOk(String consumerTag) {}
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {}
}