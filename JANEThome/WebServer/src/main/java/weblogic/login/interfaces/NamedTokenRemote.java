package weblogic.login.interfaces;

import javax.ejb.Local;

@Local
public interface NamedTokenRemote extends BasicTokenRemote {

    void setInformations(String name, String surname, String email, String password);
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPassword();
    void   resetInformations();

}
