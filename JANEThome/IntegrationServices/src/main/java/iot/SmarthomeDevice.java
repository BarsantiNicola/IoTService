package iot;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SmarthomeDevice implements Serializable {

    enum DeviceType{
        LIGHT,
        FAN,
        DOOR,
        THERMOSTAT,
        CONDITIONER
    }

    private final String name;
    private final String type;
    private final static transient Random random = new SecureRandom();
    //  TODO To be removed
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    SmarthomeDevice(String name, DeviceType type ){
        this.name = name;
        switch(type){
            case LIGHT:
                this.type = "Light";
                break;
            case FAN:
                this.type = "Fan";
                break;
            case DOOR:
                this.type = "Door";
                break;
            case THERMOSTAT:
                this.type = "Thermostat";
                break;
            case CONDITIONER:
                this.type = "Conditioner";
                break;
            default:
                this.type = "UNKNOWN";
                break;
        }
    }

    public String getName(){ return name; }

    public String getType(){ return type; }

    //  TODO To be removed
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 10; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString();

    }

    //  TODO To be removed
    public static List<SmarthomeDevice> createTestingEnvironment(){

        List<SmarthomeDevice> devices = new ArrayList();
        int nDevices = random.nextInt(5)+2;

        for( int a = 0;a<nDevices; a++) {
            String name = createRandomString();
            devices.add(new SmarthomeDevice(name, DeviceType.values()[new Random().nextInt(DeviceType.values().length)]));
        }
        return devices;
    }
}
