package web.login.beans;

import utils.POJOType;
import utils.TimedDataPOJO;
import web.login.interfaces.DataContainerInterfaceLocal;

import javax.ejb.Singleton;
import java.security.SecureRandom;
import java.util.*;

@Singleton
public class InfoContainer implements DataContainerInterfaceLocal {

    private final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();
    private final HashMap<String, TimedDataPOJO> requestArchive = new HashMap<>();

    public String addToken( TimedDataPOJO token ){

        dataSkimming();
        StringBuilder name;
        Random random = new SecureRandom();
        do {
            name = new StringBuilder();
            for (int i = 0; i < 10; i++)
                name.append(allAllowed[random.nextInt(allAllowed.length)]);
        }while( requestArchive.containsKey(name.toString()));
        requestArchive.put(name.toString(),token);
        return name.toString();

    }

    public boolean verifyToken(String name, String token, POJOType type){

        dataSkimming();
        TimedDataPOJO data;
        if( requestArchive.containsKey(name))
            data = requestArchive.remove(name);
        else
            return false;

        return data.isValid(type) && data.getToken().compareTo(token) == 0;

    }

    private void dataSkimming(){

        Set<String> names = requestArchive.keySet();
        for( String name: names)
            if(requestArchive.get(name).isExpired())
                requestArchive.remove(name);

    }
}
