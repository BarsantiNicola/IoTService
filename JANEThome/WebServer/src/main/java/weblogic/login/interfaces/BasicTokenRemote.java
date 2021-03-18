package weblogic.login.interfaces;

import javax.ejb.Local;
import java.io.Serializable;

@Local
public interface BasicTokenRemote extends Serializable {

    boolean isValid( String token );
    String getToken();
    void createToken();
    void resetToken();

}
