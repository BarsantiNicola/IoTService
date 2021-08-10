package iot;


import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

@SuppressWarnings("unused")
public class SmarthomeLocation implements Serializable {

    private final List<SmarthomeSublocation> sublocations = new ArrayList<>();
    private String location;
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

    SmarthomeLocation(String location, String address, int port){
        this.location = location;
        this.ipAddress = address;
        this.port = port;
    }

    SmarthomeLocation(String name, String address, int port, List<SmarthomeSublocation> sublocations){
        this(name,address,port);
        this.sublocations.addAll(sublocations);
    }

    //// SETTERS

    void setLocation(String location){
        this.location = location;
    }

    String getLocation(){ return location; }

    //// GETTERS

    String setIpAddress(){ return ipAddress; }

    void getIpAddress(String ip){
        this.ipAddress = ip;
    }

    int givePort(){ return port; }

    void changePort(int port){
        this.port = port;
    }

    SmarthomeDevice getDevice(String sublocation, String dID){
        for( SmarthomeSublocation subloc: this.sublocations)
            if(subloc.getSubLocation().compareTo(sublocation) == 0)
                return subloc.getDevice(dID);
        return null;
    }

    boolean addSublocation(String sublocation){
        if(sublocationPresent(sublocation))
            return false;
        this.sublocations.add(new SmarthomeSublocation(this.location, sublocation));
        return true;

    }

    boolean addDevice(String sublocation, String dID, String name, SmarthomeDevice.DeviceType device_type){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(sublocation) == 0)
                return subloc.addDevice(dID, name, device_type);
        return false;
    }

    boolean addDevice(String sublocation, SmarthomeDevice device){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(sublocation) == 0)
                return subloc.addDevice(device);
        return false;
    }

    boolean changeDeviceSublocation(String new_sublocation, String dID){
        if(!sublocationPresent(new_sublocation))
            return false;
        SmarthomeDevice device = null;
        String old_sublocation = null;
        for( SmarthomeSublocation subloc: this.sublocations) {
            old_sublocation = subloc.getSubLocation();
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

    boolean removeSublocation(String sublocation){
        for(SmarthomeSublocation subloc: this.sublocations)
            if(subloc.getSubLocation().compareTo(sublocation) == 0 ){
                this.sublocations.remove(subloc);
                return true;
            }
        return false;
    }

    boolean removeDevice(String dID){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.removeDevice(dID))
                return true;
        return false;
    }

    boolean changeSublocationName(String old_name, String new_name){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(old_name) == 0 ){
                subloc.setSubLocation(new_name);
                return true;
            }
        return false;
    }

    List<SmarthomeSublocation> getSublocations(){ return sublocations; }

    boolean sublocationPresent(String sublocation){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.getSubLocation().compareTo(sublocation) == 0 )
                return true;
        return false;
    }

    boolean changeDeviceName(String old_name, String new_name){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.changeDeviceName(old_name, new_name))
                return true;
        return false;
    }

    boolean devicePresent(String dID){
        for(SmarthomeSublocation subloc: this.sublocations)
            if( subloc.devicePresent(dID))
                return true;
        return false;
    }

    HashMap<String,Object> buildSmarthomeLocation(){
        HashMap<String,Object> location = new HashMap<>();
        ArrayList<HashMap<String,Object>> sublocation = new ArrayList<>();
        for(SmarthomeSublocation subloc: this.sublocations)
            sublocation.add(subloc.buildSmarthomeSublocation());
        location.put("location" , this.location);
        location.put("sublocations" ,sublocation );
        return location;
    }
    //  TODO To be removed
    static List<SmarthomeLocation> createTestingEnvironment(){

        List<SmarthomeLocation> locations = new ArrayList<>();
        int nLocations = random.nextInt(3)+1;
        for( int a = 0;a<nLocations; a++) {
            String name = createRandomString();
            locations.add( new SmarthomeLocation(name, "8.8.8.8", 300, SmarthomeSublocation.createTestingEnvironment(name)));
        }
        return locations;
    }

}
