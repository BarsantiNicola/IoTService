package rest.out.beans;

import config.interfaces.ConfigurationInterface;
import iot.SmarthomeDevice;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.out.interfaces.RESTinterface;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
@SuppressWarnings( "unused" )
public class DeviceCommandSender implements RESTinterface {

    @EJB
    SenderInterface notifier;     //  TODO to be removed, only for testing purpose

    @EJB
    ConfigurationInterface configuration;   //  gives the configuration for the rest interface

    //  sends a command to the device REST server
    private boolean sendCommand(){
        return true;
    }

    @Override
    //  adds a new location to the user's smartHome
    public boolean addLocation( String username, String location, String ipAddr, int port ) {

        try {
            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates(DeviceUpdate.buildAddLocation( location, ipAddr, port ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the location name into the user's smartHome
    public boolean changeLocationName( String username, String name, String newName, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates( DeviceUpdate.buildRenameLocation( name, newName ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  removes the location from the user's smartHome
    public boolean removeLocation( String username, String name, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates( DeviceUpdate.buildRemoveLocation( name ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  adds a new subLocation to the user's smartHome
    public boolean addSubLocation( String username, String location, String sublocation, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates( DeviceUpdate.buildAddSubLocation( location, sublocation ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the subLocation name into the user's smartHome
    public boolean changeSubLocationName( String username, String location, String name, String newName, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates( DeviceUpdate.buildRenameSubLocation( location, name, newName ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  removes the subLocation from the user's smartHome
    public boolean removeSubLocation( String username, String location, String sublocation, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates(DeviceUpdate.buildRemoveSubLocation( location, sublocation ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  adds a new device to the user's smartHome
    public boolean addDevice( String username, String dID, String name, String location, String sublocation, SmarthomeDevice.DeviceType type, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates(DeviceUpdate.buildAddDevice( location, sublocation, dID, name, type ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the device's subLocation into the user's smartHome
    public boolean changeDeviceSublocation( String username, String dID, String name, String location, String subLocation, String newSubLocation, String ipAddr, int port ){

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates(DeviceUpdate.buildChangeDeviceSubLocation( location, dID, name, newSubLocation ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the device's name into the user's smartHome
    public boolean changeDeviceName( String username, String dID, String old_name, String new_name, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates(DeviceUpdate.buildRenameDevice( dID, old_name, new_name ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  removes the device from the user's smartHome
    public boolean removeDevice( String username, String dID, String name, String ipAddr, int port ) {

        try{

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates( DeviceUpdate.buildRemoveDevice( dID, name ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  executes the given command to the specified device of the user's smartHome
    public boolean execCommand( String username, String dID, String name, String action, String value, String ipAddr, int port ) {

        try{

            DeviceUpdateMessage message = new DeviceUpdateMessage( username );
            message.addUpdates( DeviceUpdate.buildDeviceUpdate( dID, name, action, value ));
            return notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }
}
