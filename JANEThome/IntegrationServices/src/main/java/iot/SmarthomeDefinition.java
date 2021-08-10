package iot;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmarthomeDefinition implements Serializable {

    private final String username;
    private final List<SmarthomeLocation> locations;

    //  For testing purpouse
    public SmarthomeDefinition(String username){
        this.username = username;
        locations = new ArrayList<>();
    }

    public SmarthomeDefinition(String username, List<SmarthomeLocation> locations){
        this(username);
        this.locations.addAll(locations);
    }

    public boolean addLocation(String location, String address, int port){
        if(locationPresent(location))
            return false;
        this.locations.add(new SmarthomeLocation(location,address,port));
        return true;
    }

    public boolean addSubLocation(String location, String subLocation){
        for( SmarthomeLocation loc: this.locations) {
            if (loc.getLocation().compareTo(location) == 0) {
                return loc.addSublocation(subLocation);
            }
        }
        return false;
    }

    public boolean addDevice(String location, String sublocation, String dID, String name, SmarthomeDevice.DeviceType device_type){
        if(devicePresent(dID))
            return false;

        for( SmarthomeLocation loc: this.locations)
            if(loc.getLocation().compareTo(location) == 0 )
                return loc.addDevice(sublocation,dID,name, device_type);
        return false;
    }

    public boolean removeLocation(String location){
        for( SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location)==0) {
                this.locations.remove(loc);
                return true;
            }
        return false;
    }

    public boolean removeSublocation(String location, String sublocation){
        for( SmarthomeLocation loc: this.locations)
            if(loc.getLocation().compareTo(location)==0)
                return loc.removeSublocation(sublocation);
        return false;
    }

    public boolean removeDevice(String dID){
        for( SmarthomeLocation loc: this.locations)
            if( loc.removeDevice(dID))
                return true;
        return false;

    }

    public boolean changeDeviceSublocation(String location, String new_sublocation, String dID){
        for( SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location) == 0)
                return loc.changeDeviceSublocation(new_sublocation, dID);
        return false;
    }

    public String getUsername(){ return username; }

    public List<SmarthomeLocation> getLocations() {
        return locations;
    }

    public boolean changeLocationName(String old_name, String new_name){

        if( locationPresent(new_name))
            return false;

        for(SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(old_name)==0) {
                loc.setLocation(new_name);
                return true;
            }
        return false;

    }

    public boolean locationPresent(String location){
        for(SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location)==0)
                return true;
        return false;
    }

    public boolean changeSublocationName(String location, String old_name, String new_name){

        if(sublocationPresent(new_name))
            return false;

        for(SmarthomeLocation loc: this.locations)
            if( loc.getLocation().compareTo(location) == 0)
                return loc.changeSublocationName(old_name, new_name);
        return false;

    }

    public boolean sublocationPresent(String sublocation){
        for(SmarthomeLocation loc: this.locations)
            if( loc.sublocationPresent(sublocation))
                return true;
        return false;
    }

    public boolean changeDeviceName(String old_name, String new_name){

        if(devicePresent(new_name))
            return false;

        for(SmarthomeLocation loc: this.locations)
            if( loc.changeDeviceName(old_name, new_name))
                return true;
        return false;
    }

    public boolean devicePresent(String dID){
        for(SmarthomeLocation loc: this.locations)
            if(loc.devicePresent(dID))
                return true;
        return false;
    }

    public String buildSmarthomeDefinition(){

        ArrayList<HashMap<String,Object>> response = new ArrayList<>();
        for(SmarthomeLocation loc: this.locations)
            response.add(loc.buildSmarthomeLocation());
        Gson gson = new Gson();
        return gson.toJson(response);

    }

    //  TODO To be removed
    public static SmarthomeDefinition createTestingEnvironment(String username){
        return new SmarthomeDefinition(username, SmarthomeLocation.createTestingEnvironment());

    }

}
