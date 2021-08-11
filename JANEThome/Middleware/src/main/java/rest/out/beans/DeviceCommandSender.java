package rest.out.beans;

import rest.out.interfaces.RESTinterface;
import javax.ejb.Stateless;

@Stateless
public class DeviceCommandSender implements RESTinterface {

    public boolean sendCommand(){
        return true;
    }

    @Override
    public boolean addLocation(String name, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean changeLocationName(String name, String newName, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean removeLocation(String name, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean addSubLocation(String location, String sublocation, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean changeSubLocationName(String location, String name, String newName, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean removeSubLocation(String location, String sublocation, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean addDevice(String dID, String location, String sublocation, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean changeDeviceSublocation(String dID, String location, String subLocation, String newSubLocation, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean removeDevice(String dID, String location, String subLocation, String ipAddr, int port) {
        return false;
    }

    @Override
    public boolean execCommand(String dID, String location, String subLocation, String action, String value, int port) {
        return false;
    }
}
