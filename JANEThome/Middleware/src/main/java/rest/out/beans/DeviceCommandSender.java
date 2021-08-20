package rest.out.beans;

import iot.SmarthomeDevice;
import rabbit.out.DeviceUpdate;
import rabbit.out.DeviceUpdateMessage;
import rabbit.out.interfaces.SenderInterface;
import rest.out.interfaces.RESTinterface;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class DeviceCommandSender implements RESTinterface {

    @EJB
    SenderInterface notifier;

    public boolean sendCommand(){
        return true;
    }

    @Override
    public boolean addLocation( String username, String location, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildAddLocation( location, ipAddr, port));
        notifier.sendMessage(message);
        return true;
    }

    @Override
    public boolean changeLocationName( String username, String name, String newName, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildRenameLocation( name, newName ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean removeLocation( String username, String name, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildRemoveLocation( name ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean addSubLocation( String username, String location, String sublocation, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildAddSubLocation( location, sublocation ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean changeSubLocationName( String username, String location, String name, String newName, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildRenameSubLocation( location, name, newName ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean removeSubLocation( String username, String location, String sublocation, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildRemoveSubLocation( location, sublocation ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean addDevice(String username, String dID, String name, String location, String sublocation, SmarthomeDevice.DeviceType type, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildAddDevice( location, sublocation, dID, name, type ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean changeDeviceSublocation( String username, String dID, String name, String location, String subLocation, String newSubLocation, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildChangeDeviceSubLocation( location, dID, name, newSubLocation ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean changeDeviceName(String username, String dID, String old_name, String new_name, String ipAddr, int port) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildRenameDevice( dID, old_name, new_name ));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean removeDevice( String username, String dID, String name, String ipAddr, int port ) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildRemoveDevice( dID, name));
        notifier.sendMessage(message);
        return true;

    }

    @Override
    public boolean execCommand( String username, String dID, String name, String action, String value, String ipAddr, int port ) {

        DeviceUpdateMessage message = new DeviceUpdateMessage( username );
        message.addUpdates( DeviceUpdate.buildDeviceUpdate( dID, name, action, value ));
        notifier.sendMessage(message);
        return true;

    }
}
