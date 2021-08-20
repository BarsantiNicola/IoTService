package rabbit.out.interfaces;

import rabbit.out.DeviceUpdateMessage;
import javax.ejb.Remote;

@Remote
public interface SenderInterface {

    int sendMessage(DeviceUpdateMessage message );

}
