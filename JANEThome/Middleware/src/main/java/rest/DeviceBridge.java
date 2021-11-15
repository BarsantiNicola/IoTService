package rest;

/**
 * Class containing bridging methods to pass from the smartHome representation of the device actions
 * to the internal service one and vice versa
 */
public class DeviceBridge {

    //  controller representation of device actions
    private static final String[] convertedTraits = {
            "onOff",
            "fanSpeed",
            "brightness",
            "color",
            "openClose",
            "lockUnlock",
            "tempTarget",
            "tempCurrent",
            "connectivity"
    };

    //  smartHome internal representation of device actions(google based)
    private static final String[] receivedTraits = {
            "action.devices.traits.OnOff",
            "action.devices.traits.FanSpeed",
            "action.devices.traits.Brightness",
            "action.devices.traits.ColorSetting",
            "action.devices.traits.OpenClose",
            "action.devices.traits.LockUnlock",
            "action.devices.traits.TemperatureSetting",
            "action.devices.traits.Temperature",
            "action.devices.traits.Connectivity"
    };


    ////////--  CONTROLLER -> SERVICE --////////


    /**
     * Convert a trait to the controller format to the internal service format(controller->service)
     * @param trait A device action expressed into the controller format
     * @return The corresponding internal representation or an empty string
     */
    public static String controllerToServiceTrait( String trait ){

        //  searching a matching value from the controller defined actions
        int index = DeviceBridge.controllerToServiceIndex( trait );
        if( index == -1 )
            return "";
        else
            return DeviceBridge.receivedTraits[ index ];

    }

    /**
     * Convert an action value to the controller format to the internal service format(controller->service)
     * @param value A device action value expressed into the controller format
     * @return The corresponding internal representation or the same value(not to be converted)
     */
    public static String controllerToServiceValue( String value ){

        //  on/open/lock corresponds to 1 into the service internal representation
        if( value.compareTo( "on" )== 0 || value.compareTo( "open" ) == 0 || value.compareTo( "lock" ) == 0 )
            return "1";

        //  off/close/unlock corresponds to 0 into the service internal representation
        if( value.compareTo( "off" ) == 0 || value.compareTo( "close" ) == 0 || value.compareTo( "unlock" ) == 0 )
            return "0";

        return value;

    }

    /**
     * Searches a match into the internal possible actions
     * @param trait A device action value expressed into the controller format
     * @return A value corresponding to the index into the actions array or -1 in case of no match
     */
    public static int controllerToServiceIndex( String trait ){

        for( int a = 0; a<DeviceBridge.convertedTraits.length; a++ )
            if( DeviceBridge.convertedTraits[ a ].compareTo( trait ) == 0 )
                return a;

        return -1;

    }


    ////////--  SERVICE -> CONTROLLER --////////


    /**
     * Convert a trait from the internal service format to the controller format(service->controller)
     * @param trait A device action expressed into the internal service format
     * @return The corresponding controller representation or an empty string
     */
    public static String serviceToControllerTrait( String trait ){

        //  searching a matching value from the internal defined actions
        int index = DeviceBridge.serviceToControllerIndex( trait );
        if( index == -1 )
            return "";
        else
            return DeviceBridge.convertedTraits[ index ];

    }

    /**
     * Convert an action value from the internal service format to the controller format(service->controller)
     * @param value A device action value expressed into the internal format
     * @return The corresponding controller representation or the same value(not to be converted)
     */
    public static Object serviceToControllerValue( String action, String value ){

        switch( DeviceBridge.serviceToControllerIndex( action )){

            case 0: // action.devices.traits.OnOff
                if( value.compareTo( "1" ) == 0 )
                    return "on";
                else
                    return "off";

            case 4: // action.devices.traits.OpenClose
                if( value.compareTo( "1" ) == 0 )
                    return "open";
                else
                    return "close";

            case 5: // action.devices.traits.LockUnlock
                if( value.compareTo( "1" ) == 0 )
                    return "lock";
                else
                    return "unlock";

            default: // value needs to be converted into the integer or float representation
                try{
                    //  we try first integer conversion
                    return Integer.parseInt( value );

                }catch( Exception e ){

                    try {
                        //  if fail we try the the float conversion
                        return Math.round(Float.parseFloat(value));

                    }catch( Exception e2 ){

                        //  if fail we leave the value as it is
                        return value;
                    }

                }
        }
    }

    /**
     * Searches a match into the controller possible actions
     * @param trait A device action value expressed into the internal format
     * @return A value corresponding to the index into the actions array or -1 in case of no match
     */
    public static int serviceToControllerIndex( String trait ){

        for( int a = 0; a<DeviceBridge.receivedTraits.length; a++ )
            if( DeviceBridge.receivedTraits[ a ].compareTo( trait ) == 0 )
                return a;

        return -1;

    }


}
