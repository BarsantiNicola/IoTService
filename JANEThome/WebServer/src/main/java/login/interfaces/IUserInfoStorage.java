package login.interfaces;

import javax.ejb.Local;

/**
 * Class used as a data node for user information
 * <p>
 *     Some information of the user are available into its session to build its web pages
 * </p>
 */
@Local
public interface IUserInfoStorage {

    String getName();
    String getSurname();
    void setParameters( String name, String surname );

}
