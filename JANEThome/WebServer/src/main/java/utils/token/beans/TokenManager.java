package utils.token.beans;

import utils.token.UserCredential;
import utils.token.interfaces.TokenManagerRemote;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

@Singleton
public class TokenManager implements TokenManagerRemote {

    private final HashMap<String, UserCredential> permissionArchive = new HashMap<>();
    private final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    @Override
    @Lock(LockType.WRITE)
    public synchronized String addPermission(String email){
        String token;
        do{

            token = createToken(25 );

        }while(this.permissionArchive.containsKey(token));
        this.permissionArchive.put( token, new UserCredential(email, 24));
        return token;

    }

    @Override
    @Lock(LockType.READ)
    public synchronized boolean verify(String token, String email){
        UserCredential credential = this.permissionArchive.get(token);
        return credential != null && credential.getEmail().compareTo(email) == 0;
    }

    @Override
    @Lock(LockType.WRITE)
    public void refresh(String token){

        UserCredential credential = this.permissionArchive.get(token);
        credential.refresh();
        this.permissionArchive.replace(token , credential);

    }

    @Override
    @Lock(LockType.WRITE)
    public void updatePermissions(){

        Set<String> tokens = this.permissionArchive.keySet();
        for( String token: tokens)
            if( this.permissionArchive.get(token).isExpired())
                this.permissionArchive.remove(token);
    }


    private String createToken( int length ){

        if (length < 15)
            length = 15;

        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++)
            password.append(this.allAllowed[random.nextInt(this.allAllowed.length)]);

        return password.toString();

    }

}
