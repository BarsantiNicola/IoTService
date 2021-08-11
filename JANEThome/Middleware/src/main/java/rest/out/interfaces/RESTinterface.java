package rest.out.interfaces;

import javax.ejb.Remote;

@Remote
public interface RESTinterface {

    boolean addLocation(String name, String ipAddr, int port);
    boolean changeLocationName(String name, String newName, String ipAddr, int port);
    boolean removeLocation(String name, String ipAddr, int port);
    boolean addSubLocation(String location, String sublocation, String ipAddr, int port);
    boolean changeSubLocationName(String location, String name, String newName, String ipAddr, int port);
    boolean removeSubLocation(String location, String sublocation, String ipAddr, int port);
    boolean addDevice(String dID, String location, String sublocation, String ipAddr, int port);
    boolean changeDeviceSublocation(String dID, String location, String subLocation, String newSubLocation, String ipAddr, int port);
    boolean removeDevice(String dID, String location, String subLocation, String ipAddr, int port);
    boolean execCommand(String dID, String location, String subLocation, String action, String value, int port);

}
