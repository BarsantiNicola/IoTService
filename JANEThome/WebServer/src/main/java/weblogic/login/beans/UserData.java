package weblogic.login.beans;

import weblogic.login.interfaces.RegistrationInterface;

import javax.ejb.StatefulTimeout;

//////////////////////////////////////////////////[ UserData ]///////////////////////////////////////////////////////
//                                                                                                                 //
//  class designed for user registration cache. It mantains the information about an account registration request  //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@StatefulTimeout(60)  //  information will be removed after 60m
public class UserData extends BasicData implements RegistrationInterface{

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public void setInformations( String firstName, String lastName, String email, String password ){

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createToken(email);

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
