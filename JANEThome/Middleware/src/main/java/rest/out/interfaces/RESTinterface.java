package rest.out.interfaces;

import iot.SmarthomeDevice;

import javax.ejb.Remote;

@Remote
public interface RESTinterface {

    boolean addLocation( String username, String from, String name, String ipAddr, int port );
    boolean changeLocationName( String username, String from, String name, String newName, String ipAddr, int port );
    boolean removeLocation( String username, String from, String name, String ipAddr, int port );
    boolean addSubLocation( String username, String from, String location, String sublocation, String ipAddr, int port );
    boolean changeSubLocationName( String username, String from, String location, String name, String newName, String ipAddr, int port );
    boolean removeSubLocation( String username, String from, String location, String sublocation, String ipAddr, int port );
    boolean addDevice( String username, String from, String dID, String name, String location, String sublocation, SmarthomeDevice.DeviceType type, String ipAddr, int port );
    boolean changeDeviceSublocation( String username, String from, String dID, String name, String location, String subLocation, String newSubLocation, String ipAddr, int port );
    boolean changeDeviceName( String username, String from, String dID, String old_name, String new_name, String ipAddr, int port );
    boolean removeDevice( String username, String from, String dID, String name, String ipAddr, int port );
    boolean execCommand( String username, String from, String dID, String action, String value, String ipAddr, int port );

}
