package iot;

import com.google.gson.Gson;

import java.security.SecureRandom;
import java.util.*;

public class SmarthomeWebDevice extends SmarthomeDevice{

    private HashMap<String,String> param;
    //////  TODO To be removed only for testing purpose
    private final static transient Random random = new SecureRandom();
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();
    //////

    public SmarthomeWebDevice(String id, String name, String location, String sub_location, DeviceType type) {
        super(id, name, location, sub_location, type);
        param = new HashMap<>();

    }

    SmarthomeWebDevice(SmarthomeDevice device) {
        super(device);
        param = new HashMap<>();
    }

    public boolean setParam(HashMap<String,String> param){

        if(!param.containsKey("action") || !param.containsKey("device_name") || !param.containsKey("value"))
            return false;
        Gson gson = new Gson();
        if( param.get("device_name").compareTo(this.giveDeviceName()) != 0 || !this.getTraits().contains(param.get("action")))
            return false;

        String value = param.get("action");
        value = value.substring(value.lastIndexOf(".")+1);

        if( !validateValue(value, param.get("value")))
            return false;

        if( this.param.containsKey(value))
            this.param.replace(value, param.get("value"));
        else
            this.param.put(value, param.get("value"));
        return true;

    }

    public HashMap<String,String> getParam(){
        return param;
    }

    private boolean validateValue(String action, String value){

        if( action.compareTo("ColorSetting") == 0)
            return value.charAt(0) == '#';

        if( action.compareTo("Brightness") == 0)
            try{
                int brightness = Integer.parseInt(value);
                return !( brightness<0 || brightness>100);

            }catch(Exception e){
                return false;
            }

        if( action.compareTo("FanSpeed") == 0 )
            try{
                int fanspeed = Integer.parseInt(value);
                return !( fanspeed<0 || fanspeed>100);

            }catch(Exception e){
                return false;
            }

        if( action.compareTo("TemperatureSetting") == 0 )
            try{
                float temperature = Float.parseFloat(value);
                return !( temperature < -20.0 || temperature > 30.0);
            }catch(Exception e){
                return false;
            }

        if( action.compareTo("OnOff") == 0 || action.compareTo("OpenClose") == 0 || action.compareTo("LockUnlock") == 0)
            return value.compareTo("0") == 0 || value.compareTo("1") == 0;

        return false;

    }



    //  TODO To be removed only for testing purpose
    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 10; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    public HashMap<String,Object> buildSmarthomeWebDevice(){
        HashMap<String,Object> result = new HashMap<>();
        if( this.name.containsKey("name"))
            result.put("name", this.name.get("name"));
        else
            result.put("name", this.id);
        switch(convertType(this.type)){
            case LIGHT:
                result.put("type" , "Light");
                break;
            case FAN:
                result.put("type" , "Fan");
                break;
            case DOOR:
                result.put("type" , "Door");
                break;
            case THERMOSTAT:
                result.put("type" , "Thermostat");
                break;
            case CONDITIONER:
                result.put("type" , "Conditioner");
                break;
            case UNKNOWN:
                result.put("type" , "Unknown");
                break;

        }
        result.put("param" , this.param);
        return result;
    }

    //  TODO To be removed only for testing purpose
    public static List<SmarthomeWebDevice> createTestingEnvironment(String location, String sub_location){

        List<SmarthomeWebDevice> devices = new ArrayList<>();
        int nDevices = random.nextInt(5)+2;

        for( int a = 0;a<nDevices; a++) {
            String name = createRandomString();
            devices.add(setParameters(new SmarthomeWebDevice(name, name, location, sub_location, DeviceType.values()[new Random().nextInt(DeviceType.values().length-1)])));
        }
        return devices;

    }

    //  TODO To be removed only for testing purpose
    public static SmarthomeWebDevice setParameters(SmarthomeWebDevice device){
        HashMap<String,String> param = new HashMap<>();
        switch(SmarthomeDevice.convertType(device.getType())){
            case LIGHT:
                param.put("action", "action.devices.traits.OnOff");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam(param);
                param.replace("action" , "action.devices.traits.Brightness");
                device.setParam(param);
                param.replace("action", "action.devices.traits.ColorSetting");
                param.replace("value" , "#ECFF00" );
                device.setParam(param);
                break;

            case FAN:
                param.put("action", "action.devices.traits.OnOff");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam(param);
                param.replace("action" , "action.devices.traits.FanSpeed");
                device.setParam(param);
                break;

            case DOOR:
                param.put("action", "action.devices.traits.LockUnlock");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam(param);
                param.replace("action" , "action.devices.traits.OpenClose");
                device.setParam(param);
                break;

            case CONDITIONER:
                param.put("action", "action.devices.traits.OnOff");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam(param);
                param.replace("action" , "action.devices.traits.FanSpeed");
                device.setParam(param);
                param.replace("action", "action.devices.traits.TemperatureSetting");
                param.replace("value" , "6.0" );
                device.setParam(param);
                break;

            case THERMOSTAT:
                param.put("action", "action.devices.traits.TemperatureSetting");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "6.0");
                break;

            default:
        }

        return device;
    }


}
