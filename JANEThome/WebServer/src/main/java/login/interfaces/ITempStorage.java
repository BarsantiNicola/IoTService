package login.interfaces;

import javax.ejb.Local;

/**
 * Class used as a data node for registration information
 * <p>
 *     During registration of an account, the data isn't stored into the database but maintained locally
 *     until the user will confirm its registration
 * </p>
 */
@Local
public interface ITempStorage extends IAuthStorage {

    void setInformations( String name, String surname, String email, String password );
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPassword();

}
