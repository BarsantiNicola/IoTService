package rest.out.beans;

import config.interfaces.ConfigurationInterface;
import config.interfaces.GeneratorInterface;
import iot.SmarthomeDevice;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.out.interfaces.RESTinterface;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Stateless
@SuppressWarnings( "unused" )
public class DeviceCommandSender implements RESTinterface {

    @EJB
    private SenderInterface notifier;

    @EJB
    private ConfigurationInterface configuration;   //  gives the configuration for the rest interface

    @EJB
    GeneratorInterface idGenerator;

    private final ExecutorService executors = Executors.newCachedThreadPool();

    private Logger initializeLogger(){

        Logger logger = Logger.getLogger( getClass().getName() );

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

        return logger;
    }

    //  sends a command to the device REST server
    private Future<Boolean> sendCommand( String address, int port, String path, HashMap<String,String> request ){

        return executors.submit(new RESTsender(address, port, path, request ));

    }

    @Override
    //  adds a new location to the user's smartHome and in case of success automatically forward the message to
    //  all the service components
    public boolean addLocation( String username, String from, String location, String ipAddr, int port ) {

        Logger logger = this.initializeLogger();
        HashMap<String,String> conf = configuration.getConfiguration( "rest" );
        HashMap<String,String> data = new HashMap<>();
        String locID = idGenerator.generateLID();
        data.put( "location" , location );
        data.put( "locID", locID );
        data.put( "address", ipAddr );
        data.put( "port", String.valueOf( port ));
        try {
            Future<Boolean> result = this.sendCommand( conf.get("control_address"), Integer.parseInt(conf.get("control_port")), conf.get("control_path"), data );
            if( result.get() ){

                DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                message.addUpdates(DeviceUpdate.buildAddLocation( new Date(System.currentTimeMillis()), location, locID, ipAddr, port ));
                return this.notifier.sendMessage( message ) > 0;

            }
            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    //  changes the location name into the user's smartHome
    public boolean changeLocationName( String username, String from, String name, String newName, String ipAddr, int port ) {


        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates( DeviceUpdate.buildRenameLocation( new Date(System.currentTimeMillis()), name, newName ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  removes the location from the user's smartHome
    public boolean removeLocation( String username, String from, String name, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates( DeviceUpdate.buildRemoveLocation( new Date(System.currentTimeMillis()), name ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  adds a new subLocation to the user's smartHome
    public boolean addSubLocation( String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates( DeviceUpdate.buildAddSubLocation( new Date(System.currentTimeMillis()), location, sublocation, sublocID ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the subLocation name into the user's smartHome
    public boolean changeSubLocationName( String username, String from, String location, String name, String newName, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates( DeviceUpdate.buildRenameSubLocation( new Date(System.currentTimeMillis()), location, name, newName ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  removes the subLocation from the user's smartHome
    public boolean removeSubLocation( String username, String from, String location, String sublocation, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates(DeviceUpdate.buildRemoveSubLocation( new Date(System.currentTimeMillis()), location, sublocation ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  adds a new device to the user's smartHome
    public boolean addDevice( String username, String from, String name, String location, String sublocation, SmarthomeDevice.DeviceType type, String ipAddr, int port ) {

        try {
            String dID = idGenerator.generateDID();
            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates(DeviceUpdate.buildAddDevice( new Date(System.currentTimeMillis()), location, sublocation, dID, name, type ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the device's subLocation into the user's smartHome
    public boolean changeDeviceSublocation( String username, String from, String dID, String name, String location, String subLocation, String newSubLocation, String ipAddr, int port ){

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates(DeviceUpdate.buildChangeDeviceSubLocation( new Date(System.currentTimeMillis()), location, dID, name, newSubLocation ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  changes the device's name into the user's smartHome
    public boolean changeDeviceName( String username, String from, String dID, String old_name, String new_name, String ipAddr, int port ) {

        try {

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates(DeviceUpdate.buildRenameDevice( new Date(System.currentTimeMillis()), dID, old_name, new_name ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  removes the device from the user's smartHome
    public boolean removeDevice( String username, String from, String dID, String name, String ipAddr, int port ) {

        try{

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates( DeviceUpdate.buildRemoveDevice( new Date(System.currentTimeMillis()), dID, name ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }

    @Override
    //  executes the given command to the specified device of the user's smartHome
    public boolean execCommand( String username, String from, String dID, String action, String value, String ipAddr, int port ) {

        try{

            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
            message.addUpdates( DeviceUpdate.buildDeviceUpdate( new Date(System.currentTimeMillis()), dID, action, value ));
            return this.notifier.sendMessage( message ) > 0;

        }catch( InvalidMessageException e ){

            e.printStackTrace();
            return false;

        }
    }
}
