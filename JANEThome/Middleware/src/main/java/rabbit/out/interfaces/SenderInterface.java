package rabbit.out.interfaces;

import rabbit.msg.DeviceUpdateMessage;
import javax.ejb.Remote;

@Remote
public interface SenderInterface {

    int sendMessage(DeviceUpdateMessage message );

}
