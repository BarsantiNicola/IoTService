package weblogic.login.interfaces;

import javax.ejb.Remote;

@Remote
public interface UserLoginLocal {

    String getName();
    String getSurname();
    void setParameters(String name, String surname);

}
