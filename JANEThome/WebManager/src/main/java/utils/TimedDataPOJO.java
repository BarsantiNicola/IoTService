package utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

public class TimedDataPOJO {

    private final LocalDateTime expireDate;
    private String token;
    private final POJOType type;
    private static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    public TimedDataPOJO(POJOType type, int hourToExpire){

        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 25; i++)
            password.append(allAllowed[random.nextInt(allAllowed.length)]);

        this.type = type;
        this.token = password.toString();
        this.expireDate = LocalDateTime.now().plusHours(hourToExpire);

    }

    public String getToken(){

        if( token != null && LocalDateTime.now().isAfter(expireDate))
            token = null;
        return token;

    }

    public boolean isExpired(){

        return token == null || LocalDateTime.now().isAfter(expireDate);

    }

    public boolean isValid( POJOType type){

        return this.type == type;

    }

}
