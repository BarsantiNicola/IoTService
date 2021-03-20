package weblogic.login.interfaces;

import javax.ejb.Remote;
import java.io.Serializable;

@Remote
public interface BasicTokenRemote extends Serializable {

    boolean isValid( String token );
    String getToken();
    void createToken( String user );
    void resetToken();
    String getUser();

}
