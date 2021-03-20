package weblogic.login.beans;

import weblogic.login.interfaces.UserLoginLocal;

import javax.ejb.Stateful;
import javax.inject.Named;

@Stateful
@Named("userLogin")
public class UserLogin implements UserLoginLocal {

    private String username = null;
    private String password = null;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setParameters(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
