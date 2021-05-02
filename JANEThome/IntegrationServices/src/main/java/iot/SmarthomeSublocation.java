package iot;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

public class SmarthomeSublocation implements Serializable {

    private String name;
    private List<SmarthomeDevice> devices = new ArrayList<>();
    private final static transient Random random = new SecureRandom();
    //  TODO To be removed
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    SmarthomeSublocation(String name){
        this.name = name;
    }

    SmarthomeSublocation(String name, List<SmarthomeDevice> devices ){
        this(name);
        this.devices.addAll(devices);
    }

    public String getName(){
        return name;
    }

    public List<SmarthomeDevice> getDevices(){ return (List<SmarthomeDevice>) devices; }

    //  TODO To be removed
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString();

    }
    //  TODO To be removed
    public static List<SmarthomeSublocation> createTestingEnvironment(){

        List<SmarthomeSublocation> sublocations = new ArrayList<>();
        int nSublocations = random.nextInt(2)+1;
        for( int a = 0;a<nSublocations; a++) {
            String name = createRandomString();
            sublocations.add(new SmarthomeSublocation(name, SmarthomeDevice.createTestingEnvironment()));
        }
        return sublocations;
    }
}
