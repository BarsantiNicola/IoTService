package weblogic.login.beans;

import weblogic.login.interfaces.NamedTokenRemote;

import javax.ejb.Stateful;
import javax.inject.Named;

@Stateful
@Named("userData")
public class UserData extends BasicData implements NamedTokenRemote {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public void setInformations(String firstName, String lastName, String email, String password ){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createToken();
    }

    public void resetInformations(){

        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.password = null;
        super.resetToken();

    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

}
