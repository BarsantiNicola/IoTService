package iot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.Date;


//  Class derived from SmarthomeDevice to add actions to devices and maintain their states
//  The class is used by the WebServer to store information about the user devices
@SuppressWarnings("unused")
public class SmarthomeWebDevice extends SmarthomeDevice {

    @Expose
    private HashMap<String, String> param = new HashMap<>();    //  set of states associated to the device
    @Expose
    private boolean connectivity;
    @Expose
    private final HashMap<Date, Operation> historical = new HashMap<>();
    private transient Logger logger;
    @Expose
    private HashMap<String, Date> expiresForMongo = new HashMap<>();
    private transient HashMap<String, Date> expires = new HashMap<>(); //  set of timestamp associated with each trait to discard old updates

    //// CONSTRUCTORS


    public SmarthomeWebDevice() {
        initializeLogger();
        this.expires = new HashMap<>();
        this.param = new HashMap<>();
    }

    public SmarthomeWebDevice(String id, String name, String location, String sub_location, DeviceType type) {
        super(id, name, location, sub_location, type);
        this.connectivity = false;
        this.initializeLogger();
    }

    ////// SETTERS

    public void setConnectivity(boolean connectivity) {
        this.connectivity = connectivity;
    }

    public void setParam(HashMap<String, String> param) {
        this.param.putAll(param);
    }

    public void setHistorical(HashMap<Date, Operation> historical) {
        this.historical.putAll(historical);
    }

    public void setExpires(HashMap<String, Date> expires) {
        this.expires = expires;
    }

    public void setExpiresForMongo(HashMap<String, Date> expiresForMongo) {
        this.expiresForMongo = expiresForMongo;
    }
    ////// GETTERS

    public HashMap<String, String> getParam() {
        return param;
    }

    public boolean getConnectivity() {
        return this.connectivity;
    }

    public HashMap<Date, Operation> getHistorical() {
        return this.historical;
    }

    public HashMap<String, Date> getExpires() {
        return this.expires;
    }

    public HashMap<String, Date> getExpiresForMongo() {
        return expiresForMongo;
    }

    //// UTILITY FUNCTIONS

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

        if( this.expires == null )
            this.expires = new HashMap<>();

        trait = trait.substring(trait.lastIndexOf(".") + 1);
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

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger() {

        if (this.logger != null)
            return;

        this.logger = LogManager.getLogger(getClass());

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
    public boolean executeAction(HashMap<String, String> param, boolean trial, boolean force) {

        this.initializeLogger();
        Gson gson = new Gson();

        System.out.println("OBJECT STATE: " + gson.toJson(this));
        System.out.println("Execute action: " + gson.toJson(param));
        //  verification that the mandatory parameters are present
        if (!trial && !param.containsKey("timestamp") && !force) {
            logger.error("Error, missing timestamp into application update");
            return false;
        }
        System.out.println("step1");
        if (!param.containsKey("action") || !param.containsKey("device_name") || !param.containsKey("value")) {
            logger.error(
                    "Invalid request to perform an action, missing parameters [device_name:" + param.containsKey("device_name") +
                            "][action:" + param.containsKey("action") + "][value:" + param.containsKey("value") + "][" + param.containsKey("timestamp") + "]");
            return false;
        }
        System.out.println("step2");
        if (!this.updateLastChange(param.get("action"), gson.fromJson(param.get("timestamp"), Date.class), trial, force)) {
            logger.warn("The requested action has an old timestamp, discarding the request");
            return false;
        }

        DeviceType typos = SmarthomeDevice.convertType(this.type);
        System.out.println("step3" + typos);
        if ((typos == DeviceType.THERMOSTAT || typos == DeviceType.CONDITIONER) && param.get("action").compareTo("action.devices.traits.Temperature") == 0) {
            if (!trial && !this.connectivity)
                this.connectivity = true;
            return true;
        }
        System.out.println("step4");
        if (param.get("action").compareTo("action.devices.traits.Connectivity") == 0) {
            if (!trial)
                this.connectivity = param.get("value").compareTo("1") == 0;
            return true;
        }
        System.out.println("step5");
        //  verification that this is the correct device
        if (param.get("device_name").compareTo(this.giveDeviceName()) != 0 || !this.getTraits().contains(param.get("action"))) {
            if (param.get("device_name").compareTo(this.giveDeviceName()) != 0)
                logger.error(
                        "Invalid request to perform an action, not the correct device. [CurrentDevice: " + this.giveDeviceName() +
                                "][RequestedDevice: " + param.get("device_name"));
            else
                logger.error("Invalid request to perform an action, invalid trait. [Action: " + param.get("action"));
            return false;
        }
        System.out.println("step6");
        // to reduce string comparison heavy we take only the variable part of the string [action.devices.traits.ACTION]
        String value = param.get("action");
        value = value.substring(value.lastIndexOf(".") + 1);
        System.out.println("step6.5");
        //  validation of the given value for the requested action
        if (!validateValue(value, param.get("value"))) {
            System.out.println("step236");
            logger.error("Invalid request to perform an action, invalid value");
            return false;

        }
        System.out.println("step6.7");
        if (trial)
            return true;
        System.out.println("step7");
        if( this.param == null )
            this.param = new HashMap<>();

        //  applying the action to the data structure
        if (this.param.containsKey(value))
            this.param.replace(value, param.get("value"));
        else
            this.param.put(value, param.get("value"));

        // TODO to be removed add value on historical list
        if (param.containsKey("timestamp")) {
            historical.put(gson.fromJson(param.get("timestamp"), Date.class), new Operation(param.get("action"),
                    param.get("value"), gson.fromJson(param.get("timestamp"), Date.class)));
            System.out.print(param.get("device_name") + "-->" + param.get("timestamp"));
        }

        System.out.println("BHOOOO");
        logger.info("Request to perform an action correctly done [DeviceName: " + param.get("device_name") + "][Action: " +
                param.get("action") + "][Value: " + param.get("value") + "]");

        if (!this.connectivity)
            this.connectivity = true;

        System.out.println("OBJECT STATE ended: " + gson.toJson(this));
        return true;

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
