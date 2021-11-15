package iot;

public enum DeviceType{
    LIGHT,            //  https://developers.google.com/assistant/smarthome/guides/light
    FAN,              //  https://developers.google.com/assistant/smarthome/guides/fan
    DOOR,             //  https://developers.google.com/assistant/smarthome/guides/door
    THERMOSTAT,       //  https://developers.google.com/assistant/smarthome/guides/thermostat
    CONDITIONER,      //  https://developers.google.com/assistant/smarthome/guides/acunit
    UNKNOWN;

    private static final String[] types = {
            "Light",
            "Fan",
            "Door",
            "Thermostat",
            "Conditioner" };

    /**
     * Method to pass from the stringed representation of the device type to the enumerator
     * @param value A stringed value of the device type
     * @return {@link DeviceType} The enumerator representation of the type or DeviceType.UNKNOWN
     */
    public static DeviceType StringToType( String value ){

        value = value.indexOf(".") > 0 ? value.substring( value.lastIndexOf( "." ) + 1 ) : value;
        if( value.compareToIgnoreCase( "AC_UNIT") == 0 )
            return DeviceType.CONDITIONER;

        for( int a = 0; a< DeviceType.types.length; a++ )
            if( DeviceType.types[ a ].compareToIgnoreCase( value ) == 0 )
                return DeviceType.values()[ a ];
        return DeviceType.UNKNOWN;
    }

}