package iot;

import java.security.SecureRandom;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


//  Class derived from SmarthomeDevice to add actions to devices and maintain their states
//  The class is used by the WebServer to store information about the user devices
public class SmarthomeWebDevice extends SmarthomeDevice {

    private final HashMap<String,String> param;    //  set of states associated to the device
    private boolean connectivity;
    private transient Logger logger;

    //////  TODO To be removed only for testing purpose
    private final static transient Random random = new SecureRandom();
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    private static String createRandomString(){

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 10; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    public static List<SmarthomeWebDevice> createTestingEnvironment(String location, String sub_location){

        List<SmarthomeWebDevice> devices = new ArrayList<>();
        int nDevices = random.nextInt(5)+2;

        for( int a = 0;a<nDevices; a++) {
            String name = createRandomString();
            devices.add(setParameters(new SmarthomeWebDevice(name, name, location, sub_location, DeviceType.values()[new Random().nextInt(DeviceType.values().length-1)])));
        }
        return devices;

    }

    public static SmarthomeWebDevice setParameters(SmarthomeWebDevice device){
        HashMap<String,String> param = new HashMap<>();
        switch(SmarthomeDevice.convertType(device.getType())){
            case LIGHT:
                param.put("action", "action.devices.traits.OnOff");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam(param, false );
                param.replace("action" , "action.devices.traits.Brightness");
                device.setParam(param, false );
                param.replace("action", "action.devices.traits.ColorSetting");
                param.replace("value" , "#ECFF00" );
                device.setParam(param, false );
                break;

            case FAN:
                param.put("action", "action.devices.traits.OnOff");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam( param, false );
                param.replace("action" , "action.devices.traits.FanSpeed");
                device.setParam( param, false );
                break;

            case DOOR:
                param.put("action", "action.devices.traits.LockUnlock");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam( param, false );
                param.replace("action" , "action.devices.traits.OpenClose");
                device.setParam( param, false );
                break;

            case CONDITIONER:
                param.put("action", "action.devices.traits.OnOff");
                param.put("device_name" , device.giveDeviceName());
                param.put( "value" , "0");
                device.setParam( param, false );
                param.replace("action" , "action.devices.traits.FanSpeed");
                device.setParam( param, false );
                param.replace("action", "action.devices.traits.TemperatureSetting");
                param.replace("value" , "6.0" );
                device.setParam( param, false );
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

    //////

    // Constructor
    public SmarthomeWebDevice(String id, String name, String location, String sub_location, DeviceType type) {
        super(id, name, location, sub_location, type);
        this.param = new HashMap<>();
        this.connectivity = true;
        this.initializeLogger();
    }

    ////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger(){

        if( this.logger != null )
            return;

        this.logger = Logger.getLogger( getClass().getName() );

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter( new SimpleFormatter() );
            logger.addHandler( consoleHandler );

        }

    }

    ////// PUBLIC FUNCTIONS

    //  perform an action on a device. The action is describer by the hashmap which must contain:
    //   - device_name:  name assigned to the device by the user
    //   - action:  action to perform defined as a google home trait
    //   - value:  the value associated with the action to be performed
    //
    //  The function will return true if the operation is correctly executed, false otherwise. The usage of
    //  an hashmap as a parameter is made to deploy the mechanism in the most general way as possible
    public boolean setParam( HashMap<String,String> param, boolean trial ){

        this.initializeLogger();
        //  verification that the mandatory parameters are present
        if( !param.containsKey("action") || !param.containsKey("device_name") || !param.containsKey("value")) {
            logger.severe(
                    "Invalid request to perform an action, missing parameters [device_name:" + param.containsKey("device_name") +
            "][action:" + param.containsKey("action") + "][value:" + param.containsKey("value") + "]" );
            return false;
        }

        DeviceType typos = SmarthomeDevice.convertType(this.type);
        if(( typos == DeviceType.THERMOSTAT || typos == DeviceType.CONDITIONER) && param.get( "action").compareTo("action.devices.traits.Temperature") == 0 )
            return true;

        if( param.get( "action").compareTo("action.devices.traits.Connectivity") == 0 ){
            if( !trial )
                this.connectivity = param.get("value").compareTo("1")==0;
            return true;
        }

        //  verification that this is the correct device
        if( param.get("device_name").compareTo(this.giveDeviceName()) != 0 || !this.getTraits().contains(param.get("action"))){
            if(param.get("device_name").compareTo(this.giveDeviceName()) != 0)
                logger.severe(
                    "Invalid request to perform an action, not the correct device. [CurrentDevice: " + this.giveDeviceName() +
                            "][RequestedDevice: " + param.get("device_name"));
            else
                logger.severe( "Invalid request to perform an action, invalid trait. [Action: " + param.get("action"));
            return false;
        }

        // to reduce string comparison heavy we take only the variable part of the string [action.devices.traits.ACTION]
        String value = param.get("action");
        value = value.substring(value.lastIndexOf(".")+1);

        //  validation of the given value for the requested action
        if( !validateValue(value, param.get("value"))) {

            logger.severe( "Invalid request to perform an action, invalid value");
            return false;

        }

        if( trial )
            return true;

        //  applying the action to the data structure
        if( this.param.containsKey(value))
            this.param.replace(value, param.get("value"));
        else
            this.param.put(value, param.get("value"));

        logger.info( "Request to perform an action correctly done [DeviceName: " + param.get("device_name") + "][Action: " +
                param.get("action") + "][Value: " + param.get("value") + "]");
        return true;

    }

    //  returns back the hashmap of all the device's parameters
    public HashMap<String,String> getParam(){
        return param;
    }

    //  verification of a value for a given action. The function returns true if the given value can be used
    //  as a parameter for the given action, otherwise if the value is not accepted or the action isn't recognized it
    //  returns false
    private boolean validateValue(String action, String value){

        //  we search the action from the supported ones. If found we verify the value using their rules

        if( action.compareTo("ColorSetting") == 0)
            return value.charAt(0) == '#';  //  values must be defined as RGB values(#AABBCC)

        if( action.compareTo("Brightness") == 0)
            try{
                int brightness = Integer.parseInt(value);
                return !( brightness<0 || brightness>100);  //  brightness is an integer between 0 and 100

            }catch(Exception e){
                return false;
            }

        if( action.compareTo("FanSpeed") == 0 )
            try{
                int fanspeed = Integer.parseInt(value);
                return !( fanspeed<0 || fanspeed>100); //  fan speed is an integer between 0 and 100

            }catch(Exception e){
                return false;
            }

        if( action.compareTo("TemperatureSetting") == 0 )
            try{
                float temperature = Float.parseFloat(value);
                return !( temperature < -20.0 || temperature > 30.0); //  temperature setted must be between -20 and 30
            }catch(Exception e){
                return false;
            }

        //  all the remaining actions use binary values(0/1)
        if( action.compareTo("OnOff") == 0 || action.compareTo("OpenClose") == 0 || action.compareTo("LockUnlock") == 0)
            return value.compareTo("0") == 0 || value.compareTo("1") == 0;

        return false;

    }

    //  function to generate a description of the device to be used from the web clients to initialize their page.
    //  For each device the web client requires only its name, type and the sensors values
    public HashMap<String,Object> buildSmarthomeWebDevice(){

        HashMap<String,Object> result = new HashMap<>();

        result.put("connectivity" , this.connectivity?"1":"0" );
        //  getting the name
        if( this.name.containsKey("name"))  //  for compatibility reason we can use the id if the name is not found
            result.put("name", this.name.get("name"));
        else
            result.put("name", this.id);

        //  identifying the type
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

        //  putting the sensors' values
        result.put("param" , this.param);

        return result;
    }
}
