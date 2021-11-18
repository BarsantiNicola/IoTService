package iot;

//  internal services
import static iot.Action.*;

//  utils
import java.io.Serializable;
import java.util.*;

//  database management
import com.google.gson.annotations.Expose;


/**
 *  Class developed to maintain the definition of a smarthome device. The class in compatible with
 *  the google home device definition and can be used to aggregate a response for the google sync requests
 *    [https://developers.google.com/assistant/smarthome/reference/rest/v1/devices/sync]
 *
 *  The main usage of the class is to store in memory the devices information for the web server functionalities
 *  and as a data definition for the mongoDb connector to store permanently the devices information
 */
@SuppressWarnings("unused")
public class SmarthomeDevice implements Serializable {


    protected String id;      //  ID to uniquely identify a device

    protected String type;    //  type of device according to google home format

    protected ArrayList<String> traits;          //  set of actions available for the device according to google home format

    protected HashMap<String,String> name;       //  set of names usable for the device

    private String roomHint;                   //  name of the location in which the device is deployed

    private String structureHint;              //  name of the sub-location in which the device is deployed

    private HashMap<String,String> deviceInfo; //  set of manufacturer info(name, model, hw and sw version)

    private HashMap<String,String> attributes; //  set of information used by the traits

    private HashMap<String,String> customData; //  custom data that can be send by google in each request for the device

    private ArrayList<String> otherDeviceIds;  //  other ids for the devices(not used by us)

    private Boolean willReportState;     //  specify if the reportStateAndNotification is active

    protected Boolean notificationSupportedByAgent;  //  specify if the service is able to receive notification(for reportStateAndNotification)

    public SmarthomeDevice() {}

    public SmarthomeDevice(String id, String name, String location, String sub_location, DeviceType type ){

        this.traits = new ArrayList<>();
        this.name = new HashMap<>();

        this.name.put("name", name);

        this.structureHint = location;
        this.roomHint = sub_location;

        this.willReportState=false;
        this.notificationSupportedByAgent = false;

        this.deviceInfo = new HashMap<>();
        this.deviceInfo.put( "manufacturer", "JanetHOME.co" );
        this.deviceInfo.put( "model", "BASE_MODEL" );
        this.deviceInfo.put( "hwVersion", "v1.0" );
        this.deviceInfo.put( "swVersion", "v1.0" );

        this.attributes = null;

        this.customData = null;
        this.otherDeviceIds = null;

        this.id = id;
        switch(type){
            case LIGHT:
                this.name.put( "defaultNames", "[\"JanetHOME Light\"]" );
                this.type = LIGHT_ACTION;
                this.traits.addAll( Arrays.asList(
                        Action.ONOFF,
                        Action.COLORSET,
                        Action.BRIGHNESS
                ));
                break;

            case FAN:
                this.name.put( "defaultNames", "[\"JanetHOME Fan\"]" );
                this.type = Action.FAN_ACTION;
                this.traits.addAll( Arrays.asList(
                        Action.ONOFF,
                        Action.FANSPEED
                ));
                break;

            case DOOR:
                this.name.put( "defaultNames", "[\"JanetHOME Door\"]" );
                this.type = Action.DOOR_ACTION;
                this.traits.addAll( Arrays.asList(
                        Action.OPENCLOSE,
                        Action.LOCKUNLOCK
                ));
                break;

            case THERMOSTAT:
                this.name.put( "defaultNames", "[\"JanetHOME Thermostat\"]" );
                this.type = Action.THERM_ACTION;
                this.traits.add(
                        Action.TEMPSET
                );
                break;

            case CONDITIONER:
                this.name.put( "defaultNames", "[\"JanetHOME Conditioner\"]" );
                this.type = Action.AC_ACTION;
                this.traits.addAll( Arrays.asList(
                        Action.ONOFF,
                        Action.FANSPEED,
                        Action.TEMPSET
                ));
                break;

            default:
                this.type = "UNKNOWN";
                break;
        }
    }

    SmarthomeDevice( SmarthomeDevice device ){

        this.id = device.id;
        this.type = device.type;
        this.willReportState=device.willReportState;
        this.roomHint = device.roomHint;
        this.structureHint = device.structureHint;
        this.notificationSupportedByAgent = device.notificationSupportedByAgent;

        if( device.traits != null ){
            this.traits = new ArrayList<>();
            this.traits.addAll( device.traits );
        }

        if( device.name != null ){
            this.name = new HashMap<>();
            this.name.putAll( device.name );
        }

        if( device.deviceInfo != null ){

            this.deviceInfo = new HashMap<>();
            this.deviceInfo.putAll( device.deviceInfo );

        }

        if( device.attributes != null ) {
            this.attributes = new HashMap<>();
            this.attributes.putAll( device.attributes );
        }

        if( device.customData != null ){
            this.customData = new HashMap<>();
            this.customData.putAll( device.customData );
        }

        if( device.otherDeviceIds != null ){
            this.otherDeviceIds = new ArrayList<>();
            this.otherDeviceIds.addAll( device.otherDeviceIds );
        }
    }


    ////////--  SETTERS  --////////


    public void setStructureHint( String location ){ this.structureHint = location; }

    public void setRoomHint( String sub_location ){ this.roomHint = sub_location; }

    public void setId( String id ){ this.id = id; }

    public void setType( String type ){ this.type = type; }

    public void setName( HashMap<String,String> names ){ this.name = names; }

    public void setTraits( ArrayList<String> traits ){ this.traits = traits; }

    public void setWillReportState( boolean state ){ this.willReportState = state; }

    public void setNotificationSupportedByAgent( boolean state ){ this.notificationSupportedByAgent = state; }

    public void setDeviceInfo( HashMap<String,String> info ){ this.deviceInfo = info; }

    public void setAttributes( HashMap<String,String> attributes ){ this.attributes = attributes; }

    public void setCustomData( HashMap<String,String> data ){ this.customData = data; }

    public void setOtherDeviceIds( ArrayList<String> ids ){ this.otherDeviceIds = ids; }


    ////////--  SETTERS  --////////


    public String getId(){ return this.id; }

    public String getType(){ return this.type; }

    public HashMap<String,String> getName(){ return this.name; }

    public ArrayList<String>  getTraits(){ return this.traits; }

    public Boolean getWillReportState(){ return this.willReportState; }

    public Boolean getNotificationSupportedByAgent(){ return this.notificationSupportedByAgent; }

    public HashMap<String,String> getDeviceInfo(){ return this.deviceInfo; }

    public HashMap<String,String> getAttributes(){ return this.attributes; }

    public HashMap<String,String> getCustomData(){ return this.customData; }

    public ArrayList<String> getOtherDeviceIds(){ return this.otherDeviceIds; }

    public String getRoomHint(){ return this.roomHint; }

    public String getStructureHint(){ return this.structureHint; }


    ////////--  UTILITIES  --////////


    /**
     * Changes the user assigned device's name
     * @param name Name of the device
     */
    public void changeDeviceName( String name ){

        if( this.name.containsKey( "name" ))
            this.name.replace( "name", name );
        else
            this.name.put( "name",name );

    }

    /**
     * Get the user assigned device's name
     * @return The device name assigned by the user
     */
    public String giveDeviceName(){
        return this.name.get( "name" );
    }

}
