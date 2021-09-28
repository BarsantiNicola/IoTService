package iot;

public interface Action {
    String ONOFF = "action.devices.traits.OnOff";
    String BRIGHNESS = "action.devices.traits.Brightness";
    String COLORSET = "action.devices.traits.ColorSetting";
    String FANSPEED = "action.devices.traits.FanSpeed";
    String LOCKUNLOCK = "action.devices.traits.LockUnlock";
    String OPENCLOSE = "action.devices.traits.OpenClose";
    String TEMPSET = "action.devices.traits.TemperatureSetting";
    String TEMP = "action.devices.traits.Temperature";
    String CONNECT = "action.devices.traits.Connectivity";

    String LIGHT_ACTION ="action.devices.types.LIGHT";
    String FAN_ACTION = "action.devices.types.FAN";
    String DOOR_ACTION = "action.devices.types.DOOR";
    String THERM_ACTION = "action.devices.types.THERMOSTAT";
    String AC_ACTION = "action.devices.types.AC_UNIT";

}
