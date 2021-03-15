package web.login.interfaces;

import utils.POJOType;
import utils.TimedDataPOJO;
import javax.ejb.Local;
import java.io.Serializable;

@Local
public interface DataContainerInterfaceLocal extends Serializable {

    String addToken(TimedDataPOJO token);
    boolean verifyToken(String name, String token, POJOType type);

}
