package iot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.security.SecureRandom;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Date;


//  Class derived from SmarthomeDevice to add actions to devices and maintain their states
//  The class is used by the WebServer to store information about the user devices
public class SmarthomeWebDevice extends SmarthomeDevice {

    @Expose
    private final HashMap<String, String> param = new HashMap<>();    //  set of states associated to the device
    @Expose
    private boolean connectivity;
    @Expose
    private HashMap<Date, Operation> historical = new HashMap<>();
    private transient Logger logger;
    private transient HashMap<String, Date> expires; //  set of timestamp associated with each trait to discard old updates

    //////  TODO To be removed only for testing purpose
    private final static transient Random random = new SecureRandom();
    protected transient static final char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();

    private static String createRandomString() {

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 10; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    public static List<SmarthomeWebDevice> createTestingEnvironment(String location, String sub_location) {

        List<SmarthomeWebDevice> devices = new ArrayList<>();
        int nDevices = random.nextInt(5) + 2;

        for (int a = 0; a < nDevices; a++) {
            String name = createRandomString();
            devices.add(setParameters(new SmarthomeWebDevice(name, name, location, sub_location, DeviceType.values()[new Random().nextInt(DeviceType.values().length - 1)])));
        }
        return devices;

    }

    public static SmarthomeWebDevice setParameters(SmarthomeWebDevice device) {
        HashMap<String, String> param = new HashMap<>();
        HashMap<String, Date> exp = new HashMap<>();
        Date last = new Date(System.currentTimeMillis());
        switch (SmarthomeDevice.convertType(device.getType())) {
            case LIGHT:
                param.put("action", Action.ONOFF);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param, false, true);
                param.replace("action", Action.BRIGHNESS);
                device.setParam(param, false, true);
                param.replace("action", Action.COLORSET);
                param.replace("value", "#ECFF00");
                device.setParam(param, false, true);
                exp.put(Action.ONOFF, last);
                exp.put(Action.BRIGHNESS, last);
                exp.put(Action.COLORSET, last);
                break;

            case FAN:
                param.put("action", Action.ONOFF);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param, false, true);
                param.replace("action", Action.FANSPEED);
                device.setParam(param, false, true);
                exp.put(Action.ONOFF, last);
                exp.put(Action.FANSPEED, last);
                break;

            case DOOR:
                param.put("action", Action.LOCKUNLOCK);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param, false, true);
                param.replace("action", Action.OPENCLOSE);
                device.setParam(param, false, true);
                exp.put(Action.LOCKUNLOCK, last);
                exp.put(Action.OPENCLOSE, last);
                break;

            case CONDITIONER:
                param.put("action", Action.ONOFF);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param, false, true);
                param.replace("action", Action.FANSPEED);
                device.setParam(param, false, true);
                param.replace("action", Action.TEMPSET);
                param.replace("value", "6.0");
                device.setParam(param, false, true);
                exp.put(Action.ONOFF, last);
                exp.put(Action.FANSPEED, last);
                exp.put(Action.TEMPSET, last);
                break;

            case THERMOSTAT:
                param.put("action", Action.TEMPSET);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "6.0");
                device.setParam(param, false, true);
                param.put("action", Action.TEMP);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "7.0");
                device.setParam(param, false, true);
                exp.put(Action.TEMP, last);
                exp.put(Action.TEMPSET, last);
                break;

            default:
        }

        exp.put(Action.CONNECT, last);
        device.setExpires(exp);
        return device;
    }

    ///////

    //// UTILITY FUNCTIONS

    //  updates the last change happened to the device to prevent to previous late updates to be applied
    private void setExpires(HashMap<String, Date> expires) {

        this.expires = expires;

    }

    //  verifies if the update can be applied or is. With force enabled the system will be always updated but
    //  no expire will updated
    private boolean updateLastChange(String trait, Date time, boolean trial, boolean force) {

        //  during a trial we aren't interested into the timestamp comparison
        if (trial)
            return true;

        //  if the device doesn't contain the action we reject it
        if (!super.traits.contains(trait) &&
                trait.compareTo("action.devices.traits.Connectivity") != 0 &&
                trait.compareTo("action.devices.traits.Temperature") != 0)
            return false;

        //  if there isn't a timestamp already setted every action is good
        if (!this.expires.containsKey(trait)) {

            this.expires.put(trait, time);
            return true;

        }

        //  if there is a setted timestamp and force is setted we change the value even if the timestamp is old
        if (force) {

            // only if the timestamp is old we update
            if (this.expires.get(trait).before(time))
                this.expires.replace(trait, time);

            return true;

        }

        //  we verify if these is a duplicate message, in the case return true for updating eventually pending client
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
        if (this.expires.containsKey(trait) && gson.toJson(time).compareTo(gson.toJson(this.expires.get(trait))) == 0)
            return true;

        //  we verify the expire time and in case is older we update it
        if (this.expires.get(trait).before(time)) {

            this.expires.replace(trait, time);
            return true;

        }

        //  update is old we can discard it
        return false;

    }

    //////

    // Constructor


    public SmarthomeWebDevice() {
    }

    public SmarthomeWebDevice(String id, String name, String location, String sub_location, DeviceType type) {
        super(id, name, location, sub_location, type);
        this.expires = new HashMap<>();
        this.connectivity = true;
        this.initializeLogger();
    }

    ////// UTILITY FUNCTIONS

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger() {

        if (this.logger != null)
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if (logger.getHandlers().length == 0) { //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

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
    //  Parameters:
    //       - param: contains the param to make the action
    //       - trial: useful for operation testing, not apply any modification just verify if it is possible
    //       - force: useful for device initialization, force the system to update its status not considering the expire
    public boolean setParam(HashMap<String, String> param, boolean trial, boolean force) {

        this.initializeLogger();
        Gson gson = new Gson();

        //  verification that the mandatory parameters are present
        if (!trial && !param.containsKey("timestamp") && !force) {
            logger.severe("Error, missing timestamp into application update");
            return false;
        }

        if (!param.containsKey("action") || !param.containsKey("device_name") || !param.containsKey("value")) {
            logger.severe(
                    "Invalid request to perform an action, missing parameters [device_name:" + param.containsKey("device_name") +
                            "][action:" + param.containsKey("action") + "][value:" + param.containsKey("value") + "][" + param.containsKey("timestamp") + "]");
            return false;
        }

        if (!this.updateLastChange(param.get("action"), gson.fromJson(param.get("timestamp"), Date.class), trial, force)) {
            logger.warning("The requested action has an old timestamp, discarding the request");
            return false;
        }

        DeviceType typos = SmarthomeDevice.convertType(this.type);
        if ((typos == DeviceType.THERMOSTAT || typos == DeviceType.CONDITIONER) && param.get("action").compareTo("action.devices.traits.Temperature") == 0) {
            if (!trial && !this.connectivity)
                this.connectivity = true;
            return true;
        }

        if (param.get("action").compareTo("action.devices.traits.Connectivity") == 0) {
            if (!trial)
                this.connectivity = param.get("value").compareTo("1") == 0;
            return true;
        }

        //  verification that this is the correct device
        if (param.get("device_name").compareTo(this.giveDeviceName()) != 0 || !this.getTraits().contains(param.get("action"))) {
            if (param.get("device_name").compareTo(this.giveDeviceName()) != 0)
                logger.severe(
                        "Invalid request to perform an action, not the correct device. [CurrentDevice: " + this.giveDeviceName() +
                                "][RequestedDevice: " + param.get("device_name"));
            else
                logger.severe("Invalid request to perform an action, invalid trait. [Action: " + param.get("action"));
            return false;
        }

        // to reduce string comparison heavy we take only the variable part of the string [action.devices.traits.ACTION]
        String value = param.get("action");
        value = value.substring(value.lastIndexOf(".") + 1);

        //  validation of the given value for the requested action
        if (!validateValue(value, param.get("value"))) {

            logger.severe("Invalid request to perform an action, invalid value");
            return false;

        }

        if (trial)
            return true;

        //  applying the action to the data structure
        if (this.param.containsKey(value))
            this.param.replace(value, param.get("value"));
        else
            this.param.put(value, param.get("value"));

        //add value on historical list
        historical.put(new Date(), new Operation(param.get("action"), param.get("value")));

        logger.info("Request to perform an action correctly done [DeviceName: " + param.get("device_name") + "][Action: " +
                param.get("action") + "][Value: " + param.get("value") + "]");
        if (!this.connectivity)
            this.connectivity = true;

        return true;

    }

    //  returns back the hashmap of all the device's parameters
    public HashMap<String, String> getParam() {
        return param;
    }

    //  verification of a value for a given action. The function returns true if the given value can be used
    //  as a parameter for the given action, otherwise if the value is not accepted or the action isn't recognized it
    //  returns false
    private boolean validateValue(String action, String value) {

        //  we search the action from the supported ones. If found we verify the value using their rules

        if (action.compareTo("ColorSetting") == 0)
            return value.charAt(0) == '#';  //  values must be defined as RGB values(#AABBCC)

        if (action.compareTo("Brightness") == 0)
            try {
                int brightness = Integer.parseInt(value);
                return !(brightness < 0 || brightness > 100);  //  brightness is an integer between 0 and 100

            } catch (Exception e) {
                return false;
            }

        if (action.compareTo("FanSpeed") == 0)
            try {
                int fanspeed = Integer.parseInt(value);
                return !(fanspeed < 0 || fanspeed > 100); //  fan speed is an integer between 0 and 100

            } catch (Exception e) {
                return false;
            }

        if (action.compareTo("TemperatureSetting") == 0)
            try {
                float temperature = Float.parseFloat(value);
                return !(temperature < -20.0 || temperature > 30.0); //  temperature setted must be between -20 and 30
            } catch (Exception e) {
                return false;
            }

        //  all the remaining actions use binary values(0/1)
        boolean comparison = value.compareTo("0") == 0 || value.compareTo("1") == 0;
        if (action.compareTo("OnOff") == 0)
            return comparison;

        //  for door operations we have to verify the consistence of the operation
        if (action.compareTo("OpenClose") == 0)
            if (this.param.containsKey("LockUnlock"))
                return comparison && this.param.get("LockUnlock").compareTo("0") == 0;
            else
                return comparison;

        if (action.compareTo("LockUnlock") == 0)
            if (this.param.containsKey("OpenClose"))
                return comparison && this.param.get("OpenClose").compareTo("0") == 0;
            else
                return comparison;
        return false;

    }

    //  function to generate a description of the device to be used from the web clients to initialize their page.
    //  For each device the web client requires only its name, type and the sensors values
    public HashMap<String, Object> buildSmarthomeWebDevice() {

        HashMap<String, Object> result = new HashMap<>();

        result.put("connectivity", this.connectivity ? "1" : "0");
        //  getting the name
        if (this.name.containsKey("name"))  //  for compatibility reason we can use the id if the name is not found
            result.put("name", this.name.get("name"));
        else
            result.put("name", this.id);

        //  identifying the type
        switch (convertType(this.type)) {
            case LIGHT:
                result.put("type", "Light");
                break;
            case FAN:
                result.put("type", "Fan");
                break;
            case DOOR:
                result.put("type", "Door");
                break;
            case THERMOSTAT:
                result.put("type", "Thermostat");
                break;
            case CONDITIONER:
                result.put("type", "Conditioner");
                break;
            case UNKNOWN:
                result.put("type", "Unknown");
                break;
        }

        //  putting the sensors' values
        result.put("param", this.param);

        return result;
    }
}
