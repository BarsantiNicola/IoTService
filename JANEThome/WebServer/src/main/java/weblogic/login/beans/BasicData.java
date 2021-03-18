package weblogic.login.beans;

import weblogic.login.interfaces.BasicTokenRemote;

import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import java.security.SecureRandom;
import java.util.Random;

@Stateful
@SessionScoped
public class BasicData implements BasicTokenRemote {

    protected String token;
    protected static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    public void createToken(){

        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 25; i++)
            password.append(allAllowed[random.nextInt(allAllowed.length)]);

        this.token = password.toString();

    }

    public void resetToken(){

        this.token = null;

    }

    public boolean isValid( String token ){

        return this.token !=null && this.token.compareTo(token) == 0;

    }

    public String getToken(){
        return token;
    }

}
