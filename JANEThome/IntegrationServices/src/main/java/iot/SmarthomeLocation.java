package iot;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

public class SmarthomeLocation implements Serializable {

    private final List<SmarthomeSublocation> sublocations = new ArrayList<>();
    private final String name;
    private final String ipAddress;
    private final int port;

    //  TODO To be removed
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();
    private final static transient Random random = new SecureRandom();
    //  TODO To be removed
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString();

    }

    public SmarthomeLocation(String name, String address, int port){
        this.name = name;
        this.ipAddress = address;
        this.port = port;
    }

    public SmarthomeLocation(String name, String address, int port, List<SmarthomeSublocation> sublocations){
        this(name,address,port);
        this.sublocations.addAll(sublocations);
    }

    public String getName(){ return name; }

    public String getIpAddress(){ return ipAddress; }

    public int getPort(){ return port; }

    public List<SmarthomeSublocation> getSublocations(){ return (List<SmarthomeSublocation>) sublocations; }


    //  TODO To be removed
    public static List<SmarthomeLocation> createTestingEnvironment(){

        List<SmarthomeLocation> locations = new ArrayList<>();
        int nLocations = random.nextInt(3)+1;
        for( int a = 0;a<nLocations; a++) {
            String name = createRandomString();
            locations.add( new SmarthomeLocation(name, "8.8.8.8", 300, SmarthomeSublocation.createTestingEnvironment()));
        }
        return locations;
    }

}
