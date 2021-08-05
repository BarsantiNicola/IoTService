package iot;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

public class SmarthomeLocation implements Serializable {

    private final List<SmarthomeSublocation> sublocations = new ArrayList<>();
    private String name;
    private String ipAddress;
    private int port;

    //  TODO To be removed
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();
    private final static transient Random random = new SecureRandom();
    //  TODO To be removed
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 15; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

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

    public void setName(String name){
        this.name = name;
    }

    public String getName(){ return name; }

    public String getIpAddress(){ return ipAddress; }

    public void setIpAddress(String ip){
        this.ipAddress = ip;
    }

    public int getPort(){ return port; }

    public void setPort(int port){
        this.port = port;
    }

    public SmarthomeDevice getDevice(String sublocation, String dID){
        for( SmarthomeSublocation subloc: this.sublocations)
            if(subloc.getName().compareTo(sublocation) == 0)
                return subloc.getDevice(dID);
        return null;
    }
    public boolean addSublocation(String sublocation){
        if(sublocationPresent(sublocation))
            return false;
        this.sublocations.add(new SmarthomeSublocation(sublocation));
        return true;

    }

    public boolean addDevice(String sublocation, String dID, SmarthomeDevice.DeviceType device_type){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getName().compareTo(sublocation) == 0)
                return subloc.addDevice(dID, device_type);
        return false;
    }

    public boolean addDevice(String sublocation, SmarthomeDevice device){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getName().compareTo(sublocation) == 0)
                return subloc.addDevice(device);
        return false;
    }

    public boolean changeDeviceSublocation(String new_sublocation, String dID){
        if(!sublocationPresent(new_sublocation))
            return false;
        SmarthomeDevice device = null;
        String old_sublocation = null;
        for( SmarthomeSublocation subloc: this.sublocations) {
            old_sublocation = subloc.getName();
            device = getDevice(old_sublocation, dID);
            if (device != null)
                break;
        }

        if( device == null)
            return false;

        removeDevice(dID);
        if(!addDevice(new_sublocation, device)) {
            addDevice(old_sublocation, device);
            return false;
        }
        return true;
    }

    public boolean removeSublocation(String sublocation){
        for(SmarthomeSublocation subloc: this.sublocations)
            if(subloc.getName().compareTo(sublocation) == 0 ){
                this.sublocations.remove(subloc);
                return true;
            }
        return false;
    }

    public boolean removeDevice(String dID){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.removeDevice(dID))
                return true;
        return false;
    }

    public boolean changeSublocationName(String old_name, String new_name){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getName().compareTo(old_name) == 0 ){
                subloc.setName(new_name);
                return true;
            }
        return false;
    }

    public List<SmarthomeSublocation> getSublocations(){ return (List<SmarthomeSublocation>) sublocations; }

    public boolean sublocationPresent(String sublocation){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getName().compareTo(sublocation) == 0 )
                return true;
        return false;
    }

    public boolean changeDeviceName(String old_name, String new_name){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.changeDeviceName(old_name, new_name))
                return true;
        return false;
    }


    public boolean devicePresent(String dID){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.devicePresent(dID))
                return true;
        return false;
    }
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
