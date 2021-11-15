package iot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.Date;


/**
 * Class derived from SmarthomeDevice to add actions to devices and maintain their states
 * The class is used by the WebServer to store information about the user devices but at the end is used also as core of
 * the db even if it isn't designed for
 */
public class SmarthomeWebDevice extends SmarthomeDevice {

    @Expose
    private HashMap<String, String> param = new HashMap<>();  //  set of states associated to the device

    @Expose
    private boolean connectivity;  //  defines if the device is reachable or not

    @Expose
    private HashMap<String, Date> expires = new HashMap<>(); //  set of timestamp associated with each trait to discard old updates

    private transient Logger logger;

    @SuppressWarnings("unused")
    public SmarthomeWebDevice(){

        this.logger = LogManager.getLogger( getClass().getName() );

    }

    public SmarthomeWebDevice( String id, String name, String location, String sub_location, DeviceType type ) {

        super( id, name, location, sub_location, type );
        this.logger = LogManager.getLogger( getClass().getName() );
        this.connectivity = false;


    }


    ////////--  SETTERS  --////////


    public void setParam( HashMap<String, String> param ){

        this.param.putAll( param );

    }

    public void setExpires( HashMap<String, Date> expires ){

        this.expires = expires;

    }


    ////////--  GETTERS  --////////


    public HashMap<String, String> getParam(){

        return param;

    }

    public HashMap<String, Date> getExpires(){

        return this.expires;

    }


    ////////--  UTILITIES  --////////


    /**
     * Verifies if an update can be applied or not(a device can receive old updates, to guarantee sequential consistence
     * the updates must be checked if timestamp >= last operation timestamp, the equality is due to the fact that the same
     * update can be redundanty applied on the smarthome, in this case to prevent that some updates will not be seen by all
     * the webclients we have to accept them). The function is in charge also to maintain the state of the last update timestamp
     * when an action is performed the method automatically refresh the associated action timestamp
     * @param trait Name of the action to test on the device
     * @param time  Timestamp of the action
     * @param trial Verification without update of the last action timestamp(Method returns always true)
     * @param force No verification, force the update to be applied
     * @return True if the action is fresh false otherwise
     */
    private boolean updateLastChange( String trait, Date time, boolean trial, boolean force ) {

        //  during a trial we aren't interested into the timestamp comparison
        if( trial )
            return true;

        //  if the device doesn't contain the action we reject it
        if( !super.traits.contains( trait ) &&
                trait.compareTo( "action.devices.traits.Connectivity" ) != 0 &&  //  not google standard
                trait.compareTo( "action.devices.traits.Temperature" ) != 0 )    //  not google standard
            return false;

        if( this.expires == null )  //  patch for mongoDB
            this.expires = new HashMap<>();

        trait = trait.substring( trait.lastIndexOf( "." ) + 1 );
        //  if there isn't a timestamp already setted every action is good
        if( !this.expires.containsKey( trait )){

            this.expires.put( trait, time );
            return true;

        }

        //  if there is a setted timestamp and force is setted we change the value even if the timestamp is old
        if( force ) {

            // only if the timestamp is old we update
            if ( this.expires.get( trait ).before( time ))
                this.expires.replace( trait, time );

            return true;

        }

        //  we verify if these is a duplicate message, in the case return true for updating eventually pending client
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" ).create();
        if( this.expires.containsKey( trait ) && gson.toJson( time ).compareTo( gson.toJson( this.expires.get( trait ))) == 0)
            return true;

        //  we verify the expire time and in case is older we update it
        if( this.expires.get( trait ).before( time )) {

            this.expires.replace( trait, time );
            return true;

        }

        System.out.println( "E' IL TIMESTAMP IL PROBLEMA!!" );
        //  update is old we can discard it
        return false;

    }

    /**
     * Performs an action on a device
     * @param action Name of the action to be performed in google home format
     * @param value  Value associated with the action
     * @param timestamp  Timestamp of the action execution
     * @param trial  If true make a verification of the action without changing the information
     * @param force  Disable the timestamp verification
     * @return True in case of success false otherwise
     */
    public boolean executeAction( String action, String value, Date timestamp, boolean trial, boolean force ) {

        this.logger = LogManager.getLogger( getClass().getName() );

        //  verification that the mandatory parameters are present
        if( !trial && timestamp == null && !force){

            logger.error("Error, missing timestamp into application update");
            return false;

        }

        if( action == null || action.length() == 0 || value == null || value.length() == 0 ){

            logger.error( "Invalid request to perform an action, missing parameters [action:" + action + "][value:" + value + "][" + timestamp + "]");
            return false;

        }

        //  verification of applicability of the update
        if( !this.updateLastChange( action, timestamp, trial, force )) {

            logger.warn("The requested action has an old timestamp, discarding the request");
            return false;

        }

        DeviceType dType = DeviceType.StringToType( this.type );

        ////  specific verification that the action can be performed on the device(for not google compliant actions)

        //  if it is an environment temperature update we always accept it(it isn't a state of the device, we don't care)
        if(( dType == DeviceType.THERMOSTAT || dType == DeviceType.CONDITIONER) &&
                action.compareToIgnoreCase( "action.devices.traits.Temperature" ) == 0){

            //  update only if it isn't a trial
            this.connectivity = !trial || this.connectivity;
            return true;

        }

        if( action.compareToIgnoreCase( "action.devices.traits.Connectivity" ) == 0 ){

            this.connectivity = !trial || value.compareTo( "1" ) == 0;
            return true;

        }

        ////  general verification that the action can be performed on the device(google compliant actions)

        if( !this.getTraits().contains( action )) {

            logger.error( "Invalid request to perform an action, invalid trait. [Action: " + action );
            return false;

        }

        // to reduce string comparison heavy we take only the variable part of the string [action.devices.traits.ACTION]
        //  [Also patch for mongoDB to work]
        action = action.substring( action.lastIndexOf( "." ) + 1 );

        //  validation of the given value for the requested action
        if( !validateValue( action, value )){

            logger.error("Invalid request to perform an action, invalid value");
            return false;

        }

        if( trial )  //  If it's a verification, we have done
            return true;

        //  application of the update

        if( this.param == null )  //  patch for mongoDB
            this.param = new HashMap<>();

        //  applying the action to the data structure
        if( this.param.containsKey( value ))
            this.param.replace( action, value );
        else
            this.param.put( action, value);

        logger.info( "Request to perform an action correctly done [DeviceName: " + param.get("device_name") + "][Action: " +
                action + "][Value: " + value + "]" );

        this.connectivity = true;
        return true;

    }

    /**
     * Verifies if the given value is usable for the corresponding action
     * @param action Name of the action
     * @param value  Value used with the action
     * @return Returns true in case of success false otherwise
     */
    private boolean validateValue( String action, String value ) {

        //  we search the action from the supported ones. If found we verify the value using their rules
        if( action.compareToIgnoreCase( "ColorSetting" ) == 0 )
            return value.charAt( 0 ) == '#';  //  values must be defined as RGB values(#AABBCC)

        if( action.compareToIgnoreCase( "Brightness" ) == 0 )
            try {

                int brightness = Integer.parseInt( value );    //  verification is a number
                return !( brightness < 0 || brightness > 100 );  //  brightness is an integer between 0 and 100

            }catch( Exception e ){

                return false;

            }

        if( action.compareToIgnoreCase( "FanSpeed" ) == 0 )
            try{

                int fanspeed = Integer.parseInt(value);   //  verification is a number
                return !( fanspeed < 0 || fanspeed > 100 ); //  fan speed is an integer between 0 and 100

            }catch( Exception e ){

                return false;

            }

        if( action.compareToIgnoreCase( "TemperatureSetting" ) == 0 )
            try{

                float temperature = Float.parseFloat( value );       //  verification is a number
                return !(temperature < -20.0 || temperature > 30.0); //  temperature setted must be between -20 and 30

            }catch( Exception e ){

                return false;

            }

        //  all the remaining actions use binary values(0/1)
        boolean comparison = value.compareTo( "0" ) == 0 || value.compareTo( "1" ) == 0;
        if ( action.compareToIgnoreCase( "OnOff" ) == 0 )
            return comparison;

        //  for door operations we have to verify the consistence of the operation
        if( action.compareToIgnoreCase( "OpenClose" ) == 0 )
            if( this.param.containsKey( "LockUnlock" ))
                return comparison && this.param.get( "LockUnlock" ).compareTo( "0" ) == 0;
            else
                return comparison;

        if( action.compareTo( "LockUnlock" ) == 0)
            if (this.param.containsKey( "OpenClose" ))
                return comparison && this.param.get( "OpenClose" ).compareTo( "0" ) == 0;
            else
                return comparison;
        return false;

    }

    /**
     * Generates a representation of the device for the smartHome builder
     * @return A stringed description of the device
     */
    public HashMap<String, Object> buildSmarthomeWebDevice() {

        HashMap<String, Object> result = new HashMap<>();

        result.put("connectivity", this.connectivity ? "1" : "0");

        //  getting the name
        if( this.name.containsKey( "name" ))  //  for compatibility reason we can use the id if the name is not found
            result.put( "name", this.name.get( "name" ));
        else
            result.put( "name", this.id );

        //  identifying the type
        switch( DeviceType.StringToType( this.type )){

            case LIGHT:
                result.put( "type", "Light" );
                break;

            case FAN:
                result.put( "type", "Fan" );
                break;

            case DOOR:
                result.put( "type", "Door" );
                break;

            case THERMOSTAT:
                result.put( "type", "Thermostat" );
                break;

            case CONDITIONER:
                result.put( "type", "Conditioner" );
                break;

            case UNKNOWN:
                result.put( "type", "Unknown" );
                break;
        }

        //  putting the sensors' values
        result.put( "param", this.param );

        return result;
    }

}
