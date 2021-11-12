package login.beans;

//  internal services
import login.interfaces.IAuthStorage;

//  ejb3.0
import javax.ejb.StatefulTimeout;

//  csprng
import java.security.SecureRandom;
import java.util.Random;


/**
 * Class designed as a data node for authentication information storage
 * <p>
 *     The authentication is based on a token of 25 characters associated with the session of the user.
 *     The token is stored also into the user cookies, if the two tokens match then the user is authorized.
 *     After each request the token is updated to a new one
 * </p>
 */
@StatefulTimeout(60)  //  information will be removed after 60m
public class AuthData implements IAuthStorage {

    protected String token;  //  authToken
    private String email;   //   username associated with the token
    protected static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    /**
     * Generate a new token for a user
     * @param email Username to associate with the token
     */
    public void createToken( String email ){

        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();  // CSPRNG for cryptographic secure random number generator
        for ( int i = 0; i < 25; i++ )
            password.append( allAllowed[random.nextInt( allAllowed.length )] );

        this.token = password.toString();
        this.email = email;

    }

    /**
     * Refresh the token of the user
     */
    public void recreateToken(){
        createToken( this.email );
    }

    /**
     * Drop the token of the user to invalidate its session
     */
    public void resetToken(){

        this.token = null;

    }

    /**
     * Verification of a token with the stored one
     * @param token Token to be verified
     * @return false if the token is invalidated or not matching the given one otherwise true
     */
    public boolean isValid( String token ){

        return this.token != null && this.token.compareTo( token ) == 0;

    }

    public String getToken(){
        return token;
    }

    public String getUser(){ return email; }
}
