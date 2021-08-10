package iot;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

public class SmarthomeSublocation implements Serializable {

    private String subLocation;
    private final String location;

    private final List<SmarthomeWebDevice> devices = new ArrayList<>();
    private final static transient Random random = new SecureRandom();
    //  TODO To be removed
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    SmarthomeSublocation(String location, String subLocation){
        this.location = location;
        this.subLocation = subLocation;
    }

    SmarthomeSublocation(String location, String subLocation, List<SmarthomeWebDevice> devices ){
        this(location, subLocation);
        this.devices.addAll(devices);
    }

    public String getSubLocation(){
        return subLocation;
    }

    public void setSubLocation(String subLocation){

        this.subLocation = subLocation;
        for( SmarthomeWebDevice device: this.devices)
            device.setRoomHint(subLocation);
    }

    public List<SmarthomeWebDevice> getDevices(){ return devices; }

    public boolean changeDeviceName(String old_name, String new_name) {
        for (SmarthomeWebDevice dev : this.devices)
            if (dev.giveDeviceName().compareTo(old_name) == 0){
                dev.changeDeviceName(new_name);
                return true;
            }
        return false;
    }

    public boolean devicePresent(String dID){
        for(SmarthomeWebDevice dev: this.devices)
            if(dev.giveDeviceName().compareTo(dID) == 0)
                return true;
        return false;
    }

    public boolean addDevice(String dID, String name, SmarthomeDevice.DeviceType type){
        if(devicePresent(dID))
            return false;
        this.devices.add(new SmarthomeWebDevice(dID, name, location, subLocation, type));
        return true;
    }

    public boolean addDevice(SmarthomeWebDevice dev){
        if(devicePresent(dev.giveDeviceName()))
            return false;
        this.devices.add(dev);
        return true;
    }

    public SmarthomeWebDevice getDevice(String dID){
        for(SmarthomeWebDevice dev: this.devices)
            if( dev.giveDeviceName().compareTo(dID) == 0 )
                return dev;
        return null;
    }

    public boolean removeDevice(String dID){
        for( SmarthomeWebDevice device: this.devices)
            if( device.giveDeviceName().compareTo(dID)==0) {
                this.devices.remove(device);
                return true;
            }
        return false;
    }

    public boolean performAction(String dID, String action, String value){
        for(SmarthomeWebDevice dev: this.devices)
            if( dev.giveDeviceName().compareTo(dID) == 0 ) {
                HashMap<String,String> param = new HashMap<>();
                param.put("action", action);
                param.put("device_name" , dID);
                param.put( "value" , value);
                return dev.setParam(param);
            }
        return false;
    }

    public HashMap<String,Object> buildSmarthomeSublocation(){
        HashMap<String, Object> result = new HashMap<>();
        ArrayList<HashMap<String,Object>> devs = new ArrayList<>();
        for( SmarthomeWebDevice device: this.devices)
            devs.add(device.buildSmarthomeWebDevice());
        result.put("sublocation" , this.subLocation);
        result.put( "devices" , devs);
        return result;
    }
    //  TODO To be removed
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    //  TODO To be removed
    public static List<SmarthomeSublocation> createTestingEnvironment(String location){

        List<SmarthomeSublocation> sublocations = new ArrayList<>();
        sublocations.add(new SmarthomeSublocation(location, "default"));
        int nSublocations = random.nextInt(2)+1;
        for( int a = 0;a<nSublocations; a++) {
            String name = createRandomString();
            sublocations.add(new SmarthomeSublocation(location, name, SmarthomeWebDevice.createTestingEnvironment(location, name)));
        }
        return sublocations;
    }
}
