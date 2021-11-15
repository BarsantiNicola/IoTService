package rabbit.out.interfaces;

//  internal services
import rabbit.msg.DeviceUpdateMessage;

//  ejb3.0
import javax.ejb.Remote;

/**
 * Interface available to all the service components to broadcasting messages via rabbitMQ
 */
@Remote
public interface IRabbitSender{

    /**
     * Sends a message preformatted to contains all the information needed for the delivery
     * @param message {@link DeviceUpdateMessage} Set of requests to be sent
     * @return The number of requests correctly forwarded
     */
    int sendMessage( DeviceUpdateMessage message );

}
