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

    public void setName(String name){
        this.name = name;
    }

    public List<SmarthomeDevice> getDevices(){ return (List<SmarthomeDevice>) devices; }

    public boolean changeDeviceName(String old_name, String new_name) {
        for (SmarthomeDevice dev : this.devices)
            if (dev.getName().compareTo(old_name) == 0){
                dev.setName(new_name);
                return true;
            }
        return false;
    }

    public boolean devicePresent(String dID){
        for(SmarthomeDevice dev: this.devices)
            if(dev.getName().compareTo(dID) == 0)
                return true;
        return false;
    }

    public boolean addDevice(String dID, SmarthomeDevice.DeviceType type){
        if(devicePresent(dID))
            return false;
        this.devices.add(new SmarthomeDevice(dID, type));
        return true;
    }

    public boolean addDevice(SmarthomeDevice dev){
        if(devicePresent(dev.getName()))
            return false;
        this.devices.add(dev);
        return true;
    }

    public SmarthomeDevice getDevice(String dID){
        for(SmarthomeDevice dev: this.devices)
            if( dev.getName().compareTo(dID) == 0 )
                return dev;
        return null;
    }

    public boolean removeDevice(String dID){
        for( SmarthomeDevice device: this.devices)
            if( device.getName().compareTo(dID)==0) {
                this.devices.remove(device);
                return true;
            }
        return false;
    }

    //  TODO To be removed
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }
    //  TODO To be removed
    public static List<SmarthomeSublocation> createTestingEnvironment(){

        List<SmarthomeSublocation> sublocations = new ArrayList<>();
        sublocations.add(new SmarthomeSublocation("default", new ArrayList<>()));
        int nSublocations = random.nextInt(2)+1;
        for( int a = 0;a<nSublocations; a++) {
            String name = createRandomString();
            sublocations.add(new SmarthomeSublocation(name, SmarthomeDevice.createTestingEnvironment()));
        }
        return sublocations;
    }
}
