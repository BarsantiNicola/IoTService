package weblogic.login.beans;

import weblogic.login.interfaces.UserLoginLocal;

import javax.ejb.Stateful;
import javax.inject.Named;

@Stateful
@Named("userInfo")
public class UserLogin implements UserLoginLocal {

    private String name = null;
    private String surname = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public void setParameters(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }
}
