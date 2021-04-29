package weblogic.login.interfaces;

import javax.ejb.Remote;

@Remote
public interface RegistrationInterface extends BasicTokenRemote {

    void setInformations(String name, String surname, String email, String password);

    String getFirstName();
    String getLastName();
    String getEmail();
    String getPassword();

}
