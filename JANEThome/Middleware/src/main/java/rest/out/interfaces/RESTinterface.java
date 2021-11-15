package rest.out.interfaces;

import iot.DeviceType;
import javax.ejb.Remote;

@Remote
public interface RESTinterface {

    boolean addLocation( String username, String from, String name, String ipAddr, int port );
    boolean changeLocationName( String username, String from, String locID, String oldName, String newName );
    boolean removeLocation( String username, String from, String name, String locID );
    boolean addSubLocation( String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port );
    boolean changeSubLocationName( String username, String from, String location, String sublocation, String locID, String sublocID, String newName );
    boolean removeSubLocation( String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port );
    boolean addDevice(String username, String from, String name, String location, String sublocation, String sublocID, DeviceType type, String ipAddr, int port );
    boolean changeDeviceSublocation( String username, String from, String dID, String name, String location, String subLocation, String newSubLocation, String sublocID, String ipAddr, int port );
    boolean changeDeviceName( String username, String from, String dID, String oldName, String newName );
    boolean removeDevice( String username, String from, String dID, String name, String ipAddr, int port );
    boolean execCommand( String username, String from, String dID, String action, String value, String ipAddr, int port );

}
