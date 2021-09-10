package iot;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.*;

//  Class developed to maintain the definition of a smarthome device. The class in compatible with
//  the google home device definition and can be used to aggregate a response for the google sync requests
//  [https://developers.google.com/assistant/smarthome/reference/rest/v1/devices/sync]
//
//  The main usage of the class is to store in memory the devices information for the web server functionalities
//  and as a data definition for the mongoDb connector to store permanently the devices information

@SuppressWarnings("unused")
public class SmarthomeDevice implements Serializable {

    public enum DeviceType{
        LIGHT,            //  https://developers.google.com/assistant/smarthome/guides/light
        FAN,              //  https://developers.google.com/assistant/smarthome/guides/fan
        DOOR,             //  https://developers.google.com/assistant/smarthome/guides/door
        THERMOSTAT,       //  https://developers.google.com/assistant/smarthome/guides/thermostat
        CONDITIONER,      //  https://developers.google.com/assistant/smarthome/guides/acunit
        UNKNOWN;

        public static String typeToString(DeviceType type){

            switch( type ){
                case LIGHT:
                    return "Light";

                case FAN:
                    return "Fan";

                case DOOR:
                    return "Door";

                case THERMOSTAT:
                    return "Thermostat";

                case CONDITIONER:
                    return "Conditioner";

                default:
                    return "Unknown";
            }
        }

        public static DeviceType StringToType(String value){
            String[] values = { "Light", "Fan", "Door", "Thermostat", "Conditioner" };
            for( int a = 0; a< values.length; a++)
                if( values[a].compareToIgnoreCase(value) == 0)
                    return DeviceType.values()[a];
            return DeviceType.UNKNOWN;
        }

    }

    @Expose
    protected String id;      //  ID to uniquely identify a device
    @Expose
    protected String type;    //  type of device according to google home format
    @Expose
    protected ArrayList<String> traits;          //  set of actions available for the device according to google home format
    @Expose
    protected HashMap<String,String> name;       //  set of names usable for the device
    @Expose
    private String roomHint;                   //  name of the location in which the device is deployed
    @Expose
    private String structureHint;              //  name of the sub-location in which the device is deployed
    @Expose
    private HashMap<String,String> deviceInfo; //  set of manufacturer info(name, model, hw and sw version)
    @Expose
    private HashMap<String,String> attributes; //  set of information used by the traits
    @Expose
    private HashMap<String,String> customData; //  custom data that can be send by google in each request for the device
    @Expose
    private ArrayList<String> otherDeviceIds;  //  other ids for the devices(not used by us)
    @Expose
    private Boolean willReportState;     //  specify if the reportStateAndNotification is active
    @Expose
    protected Boolean notificationSupportedByAgent;  //  specify if the service is able to receive notification(for reportStateAndNotification)

    //// CONSTRUCTORS

    public SmarthomeDevice() {
    }

    public SmarthomeDevice(String id, String name, String location, String sub_location, DeviceType type ){

        this.traits = new ArrayList<>();
        this.name = new HashMap<>();

        this.name.put("name", name);

        this.structureHint = location;
        this.roomHint = sub_location;

        this.willReportState=false;
        this.notificationSupportedByAgent = false;

        this.deviceInfo = new HashMap<>();
        this.deviceInfo.put("manufacturer", "JanetHOME.co");
        this.deviceInfo.put("model", "BASE_MODEL");
        this.deviceInfo.put("hwVersion", "v1.0");
        this.deviceInfo.put("swVersion", "v1.0");

        this.attributes = null;

        this.customData = null;
        this.otherDeviceIds = null;

        this.id = id;
        switch(type){
            case LIGHT:
                this.name.put("defaultNames", "[\"JanetHOME Light\"]");
                this.type = "action.devices.types.LIGHT";
                this.traits.addAll(Arrays.asList(
                        "action.devices.traits.OnOff",
                        "action.devices.traits.ColorSetting",
                        "action.devices.traits.Brightness"
                ));
                break;

            case FAN:
                this.name.put("defaultNames", "[\"JanetHOME Fan\"]");
                this.type = "action.devices.types.FAN";
                this.traits.addAll(Arrays.asList(
                        "action.devices.traits.OnOff",
                        "action.devices.traits.FanSpeed"
                ));
                break;

            case DOOR:
                this.name.put("defaultNames", "[\"JanetHOME Door\"]");
                this.type = "action.devices.types.DOOR";
                this.traits.addAll(Arrays.asList(
                        "action.devices.traits.OpenClose",
                        "action.devices.traits.LockUnlock"
                ));
                break;

            case THERMOSTAT:
                this.name.put("defaultNames", "[\"JanetHOME Thermostat\"]");
                this.type = "action.devices.types.THERMOSTAT";
                this.traits.add(
                        "action.devices.traits.TemperatureSetting"
                );
                break;

            case CONDITIONER:
                this.name.put("defaultNames", "[\"JanetHOME Conditioner\"]");
                this.type = "action.devices.types.AC_UNIT";
                this.traits.addAll(Arrays.asList(
                        "action.devices.traits.OnOff",
                        "action.devices.traits.FanSpeed",
                        "action.devices.traits.TemperatureSetting"
                ));
                break;

            default:
                this.type = "UNKNOWN";
                break;
        }
    }

    SmarthomeDevice(SmarthomeDevice device){

        this.id = device.id;
        this.type = device.type;
        this.willReportState=device.willReportState;
        this.roomHint = device.roomHint;
        this.structureHint = device.structureHint;
        this.notificationSupportedByAgent = device.notificationSupportedByAgent;

        if( device.traits != null){
            this.traits = new ArrayList<>();
            this.traits.addAll(device.traits);
        }

        if( device.name != null){
            this.name = new HashMap<>();
            this.name.putAll(device.name);
        }

        if( device.deviceInfo != null){

            this.deviceInfo = new HashMap<>();
            this.deviceInfo.putAll(device.deviceInfo);

        }

        if( device.attributes != null ) {
            this.attributes = new HashMap<>();
            this.attributes.putAll(device.attributes);
        }

        if( device.customData != null ){
            this.customData = new HashMap<>();
            this.customData.putAll(device.customData);
        }

        if( device.otherDeviceIds != null ){
            this.otherDeviceIds = new ArrayList<>();
            this.otherDeviceIds.addAll(device.otherDeviceIds);
        }
    }

    //  used to quickly make decision with switch case
    public static DeviceType convertType(String type){

        type = type.substring(type.lastIndexOf(".") +1 );
        String[] types = {"LIGHT" , "FAN", "DOOR" , "THERMOSTAT" , "AC_UNIT"};

        for(int a = 0; a<types.length; a++)
            if( types[a].compareTo(type) == 0 )
                return DeviceType.values()[a];
        return DeviceType.UNKNOWN;

    }

    //  changes the user assigned device's name
    public void changeDeviceName(String name){

        if( this.name.containsKey("name"))
            this.name.replace("name", name);
        else
            this.name.put("name",name);

    }

    //  gives the user assigned device's name
    public String giveDeviceName(){
        return this.name.get("name");
    }

    //  SETTERS

    //  changes the location assigned to the device
    public void setStructureHint(String location){
        this.structureHint = location;
    }

    //  changes the sub-location assigned to the device
    public void setRoomHint(String sub_location){
        this.roomHint = sub_location;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setId(String id){
        this.id = id;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setType(String type){
        this.type = type;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    //  [maybe you are looking for SmarthomeDevice.changeDeviceName]
    public void setName(HashMap<String,String> names){
        this.name = names;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setTraits(ArrayList<String> traits){
        this.traits = traits;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setWillReportState(boolean state){
        this.willReportState = state;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setNotificationSupportedByAgent(boolean state){
        this.notificationSupportedByAgent = state;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setDeviceInfo(HashMap<String,String> info){
        this.deviceInfo = info;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setAttributes(HashMap<String,String> attributes){
        this.attributes = attributes;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setCustomData(HashMap<String,String> data){
        this.customData = data;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public void setOtherDeviceIds(ArrayList<String> ids){
        this.otherDeviceIds = ids;
    }

    //  GETTERS

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public String getId(){
        return this.id;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public String getType(){
        return this.type;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    //  [maybe you are looking for SmarthomeDevice.giveDeviceName]
    public HashMap<String,String> getName(){
        return this.name;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public ArrayList<String>  getTraits(){
        return this.traits;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public Boolean getWillReportState(){
        return this.willReportState;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public Boolean getNotificationSupportedByAgent(){
        return this.notificationSupportedByAgent;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public HashMap<String,String> getDeviceInfo(){
        return this.deviceInfo;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public HashMap<String,String> getAttributes(){
        return this.attributes;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public HashMap<String,String> getCustomData(){
        return this.customData;
    }

    //  only for beans functionalities. Not recommended not be used, ask if you have to use it to Nicola
    public ArrayList<String> getOtherDeviceIds(){
        return this.otherDeviceIds;
    }

    //  gives the subLocation where the device is deployed
    public String getRoomHint(){
        return this.roomHint;
    }

    //  gives the location where the device is deployed
    public String getStructureHint(){
        return this.structureHint;
    }
}
