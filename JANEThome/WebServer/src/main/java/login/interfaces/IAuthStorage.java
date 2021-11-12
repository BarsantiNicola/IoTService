package login.interfaces;

import javax.ejb.Local;
import java.io.Serializable;

/**
 * Class used as a data node for session auth information storage
 */
@Local
public interface IAuthStorage extends Serializable {

    boolean isValid( String token );  //  verification of a token
    String getToken();                //  returns the stored token
    void createToken( String user );  //  generate a token
    void recreateToken();             //  refresh the token
    void resetToken();                //  delete the token
    String getUser();                 //  returns the username associated with the token

}
