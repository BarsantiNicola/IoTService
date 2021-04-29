package weblogic.login.beans;

import weblogic.login.interfaces.BasicTokenRemote;

import javax.ejb.StatefulTimeout;
import java.security.SecureRandom;
import java.util.Random;

@StatefulTimeout(60)
public class BasicData implements BasicTokenRemote {

    protected String token;
    private String email;
    protected static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    public void createToken(String email){

        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 25; i++)
            password.append(allAllowed[random.nextInt(allAllowed.length)]);

        this.token = password.toString();
        this.email = email;

    }

    public void recreateToken(){
        createToken(this.email);
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

    public String getUser(){ return email; }
}
