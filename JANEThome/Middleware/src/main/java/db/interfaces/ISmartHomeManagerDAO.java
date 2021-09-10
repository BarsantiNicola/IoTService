package db.interfaces;

import iot.SmarthomeManager;

public interface ISmartHomeManagerDAO extends IGenericDao<SmarthomeManager> {
    String USERNAME = "username";            //  username associated with the smarthome
    String LOCATIONS = "locations";   //  locations of the smarthome
    String DEVICES = "devices";    //  copy of all the devices for fast retrieval(optimization)

}
