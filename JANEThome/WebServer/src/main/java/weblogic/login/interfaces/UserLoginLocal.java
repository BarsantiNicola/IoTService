package weblogic.login.interfaces;

import javax.ejb.Remote;

@Remote
public interface UserLoginLocal {

    String getUsername();
    String getPassword();
    void setParameters(String username, String password);

}
