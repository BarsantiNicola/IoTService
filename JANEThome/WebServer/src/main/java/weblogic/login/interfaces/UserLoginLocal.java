package weblogic.login.interfaces;

import javax.ejb.Local;

@Local
public interface UserLoginLocal {

    String getUsername();
    String getPassword();
    void setParameters(String username, String password);

}
