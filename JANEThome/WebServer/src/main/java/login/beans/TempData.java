package login.beans;

import login.interfaces.ITempStorage;
import javax.ejb.StatefulTimeout;

/**
 * Class designed as a data node for temporary information storage(change password/registered accounts)
 * <p>
 *     - The service doesn't store permanently registration information until the user will not confirm its email.
 *     - The service doesn't permit a direct change of the password but requires a user authentication made by sending
 *     a token to the its email.
 *
 *     The tempData class is based on a token which is used as jndi name for its retrieval. The token is then inserted
 *     into an email sent to the user(linked with an url for user redirection)
 * </p>
 */
@StatefulTimeout(60)  //  information will be removed after 60m
public class TempData extends AuthData implements ITempStorage {

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
