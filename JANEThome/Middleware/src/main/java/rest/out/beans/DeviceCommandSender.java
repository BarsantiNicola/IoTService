package rest.out.beans;

import config.interfaces.ConfigurationInterface;
import config.interfaces.GeneratorInterface;
import iot.SmarthomeDevice;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.out.interfaces.RESTinterface;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
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
public class DeviceCommandSender implements RESTinterface {


    @EJB
    private SenderInterface notifier;           //  component to update all the elements of the service

    private Logger logger;
    private HashMap<String,String> conf;        //  configuration of the service

    @EJB
    private ConfigurationInterface configuration;   //  gives the configuration for the rest interface

    @EJB
    GeneratorInterface idGenerator;             //  generates unique identifiers for the resource of the service

    private final static ExecutorService executors = Executors.newCachedThreadPool(); //  shared pool of executors to send rest messages

    public DeviceCommandSender(){
        this.initializeLogger();
    }

    @PostConstruct
    private void initialization(){
        this.conf = configuration.getConfiguration( "rest" );
    }

    private void initializeLogger(){

        this.logger = Logger.getLogger( getClass().getName() );

        //  verification of the number of instantiated handlers
        if( logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }
    }


    //  sends a command to the device REST server
    private Future<Response> sendCommand(String address, int port, String path, RESTsender.REQ_TYPE reqType, HashMap<String, String> request ){

        return executors.submit(new RESTsender(address, port, path, reqType, request ));

    }

    //  adds a new location to the user's smartHome. In case of success it automatically forwards the update to all the involved components.
    //  The locID information will be automatically generated and linked with the update forwarded to all the components
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: name of the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //      - port: port used by the location controller
    //
    //  MESSAGE   [path: /location/{locID} PUT]
    //  {
    //      "name" : "....",
    //      "user" : "....",
    //      "port" : "...."
    //  }
    @Override
    public boolean addLocation( String username, String from, String location, String ipAddr, int port ) {

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        int tentative = 0;

        String locID = idGenerator.generateLID();

        data.put( "name" , location );
        data.put( "user", username );
        data.put( "port", String.valueOf( port ));

        try {

            while( true ){

                Response result = this.sendCommand( ipAddr, Integer.parseInt(this.conf.get("control_port")), "/location/"+locID, RESTsender.REQ_TYPE.PUT, data ).get();
                if( result != null ) {

                    switch (result.getStatus()) {

                        case 200:  //  if command correctly done, we notify it to all the involved components
                            DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                            message.addUpdates( DeviceUpdate.buildAddLocation( new Date(System.currentTimeMillis()), location, locID, ipAddr, port ));
                            return this.notifier.sendMessage( message ) > 0;

                        case 400:  //  in stable version cannot happen
                            logger.severe("Error, bad ADD_LOCATION request");
                            break;

                        case 409:  //  TODO to be splitted into future updates into two errors: duplicate port / duplicate locID
                            logger.severe("Error, duplicated port or locID");
                            if( tentative++ == 3 )  //  TODO to be removed when duplicate locID response well defined
                                return false;
                            locID = idGenerator.generateLID();
                            continue;

                        case 500:  //  an error has occurred inside the erlang network
                            logger.warning( "Error, internal server error of erlang network");
                            break;

                        default:
                    }
                }
                return false;
            }

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }

    }


    //  changes the location name of a location. In case of success it automatically forwards the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - locID: the identifier of the location
    //      - oldName: the current name of the location
    //      - newName: the new name to be associated with the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //
    //  MESSAGE   [path: /location/{locID} POST ]
    //  {
    //      "name" : "...."
    //  }
    @Override
    public boolean changeLocationName( String username, String from, String locID, String oldName, String newName, String ipAddr ) {

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        data.put( "name" , newName );

        try {

            Response result = this.sendCommand(ipAddr, Integer.parseInt(conf.get("control_port")), "/location/" + locID, RESTsender.REQ_TYPE.POST, data).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRenameLocation(new Date(System.currentTimeMillis()), newName, newName));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad CHANGE_LOCATION_NAME request");
                        break;

                    case 404:
                        logger.severe("Error, locID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }
    }

    //  removes the location identified by its locID. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - name: the current name of the location
    //      - locID: identifier of the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //
    //  MESSAGE   [path: /location/{locID} DELETE ]
    @Override
    public boolean removeLocation( String username, String from, String name, String locID, String ipAddr ){

        try {

            Response result = this.sendCommand(ipAddr, Integer.parseInt(conf.get("control_port")), "/location/" + locID, RESTsender.REQ_TYPE.DELETE, null).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates( DeviceUpdate.buildRemoveLocation( new Date(System.currentTimeMillis()), name ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad REMOVE_LOCATION request");
                        break;

                    case 404:
                        logger.severe("Error, locID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }

    }

    //  adds a new sublocation to the given location. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: the current name of the location
    //      - sublocation: the name of the sublocation to add
    //      - sublocID: the identifier of the subLocation
    //      - locID: identifier of the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //      - port: port from which the controller is reachable(defined into addLocation request)
    //
    //  MESSAGE   [path: /sublocation/{subLocID} PUT ]
    //  {
    //     "name" : "...."
    //  }
    @Override
    public boolean addSubLocation( String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port ) {

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        data.put( "name" , sublocation );

        try {

            Response result = this.sendCommand( ipAddr, port, "/sublocation/" + sublocID, RESTsender.REQ_TYPE.PUT, data ).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates( DeviceUpdate.buildAddSubLocation( new Date(System.currentTimeMillis()), location, sublocation, sublocID ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad ADD_SUBLOCATION request");
                        break;

                    case 409:
                        logger.severe("Error, locID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }
    }

    //  change a sublocation name identified by locID/sublocID. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: the current name of the location
    //      - sublocation: the current name of the subLocation
    //      - newName: the new name for the subLocation
    //      - locID: the identifier of the location
    //      - sublocID: the identifier of the subLocation
    //      - locID: identifier of the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //
    //  MESSAGE   [path: /location/{locID}/{subLocID} POST ]
    //  {
    //     "name" : "...."
    //  }
    @Override
    public boolean changeSubLocationName( String username, String from, String location, String sublocation, String locID, String sublocID, String newName, String ipAddr ) {

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        data.put( "name" , newName );

        try {

            Response result = this.sendCommand(ipAddr, Integer.parseInt(conf.get("control_port")), "/location/" + locID + "/" + sublocID, RESTsender.REQ_TYPE.POST, data ).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates( DeviceUpdate.buildRenameSubLocation( new Date(System.currentTimeMillis()), location, sublocation, newName ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad CHANGE_SUBLOCATION_NAME request");
                        break;

                    case 404:
                        logger.severe("Error, locID/sublocID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }

    }

    //  removes a sublocation from the given location. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: the current name of the location
    //      - sublocation: the current name of the subLocation
    //      - sublocID: the identifier of the subLocation
    //      - locID: identifier of the location
    //      - ipAddr: Ipv4 address of the location controller
    //      - port: port from which the controller is reachable(defined into addLocation request)
    //
    //  MESSAGE   [path: /sublocation/{subLocID} DELETE ]
    @Override
    public boolean removeSubLocation( String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port ) {

        try {

            Response result = this.sendCommand( ipAddr, port, "/sublocation/" + sublocID, RESTsender.REQ_TYPE.DELETE, null ).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRemoveSubLocation( new Date(System.currentTimeMillis()), location, sublocation ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad REMOVE_SUBLOCATION request");
                        break;

                    case 409:
                        logger.severe("Error, locID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }
    }

    //  adds a new device into a given location/sublocation. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: the name of the location in which deploy the device
    //      - sublocation: the name of the sublocation in which deploy the device
    //      - sublocID: the identifier of the subLocation in which deploy the device
    //      - type: the type of the device according to Google Smarthome(action.devices.types.LIGHT/FAN/CONDITIONER/THERMOSTAT)
    //      - ipAddr: Ipv4 address of the location controller
    //      - port: port from which the controller is reachable(defined into addLocation request)
    //
    //  MESSAGE   [path: /device/{dID} PUT ]
    //  {
    //     "subloc_id" : "...",
    //     "name" : "...",
    //     "type" : "..."
    //  }
    @Override
    public boolean addDevice( String username, String from, String name, String location, String sublocation, String sublocID, SmarthomeDevice.DeviceType type, String ipAddr, int port ) {

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        int tentative = 0;

        data.put( "subloc_id" , sublocID );
        data.put( "name", name );
        data.put( "type", type.toString() );
        String dID = idGenerator.generateDID();

        try {

            while( true ){
                Response result = this.sendCommand( ipAddr, port, "/device/"+dID, RESTsender.REQ_TYPE.PUT, data ).get();
                if( result != null ) {

                    switch (result.getStatus()) {
                        case 200:  //  if command is correctly done, we notify it to all the involved components
                            DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                            message.addUpdates(DeviceUpdate.buildAddDevice( new Date(System.currentTimeMillis()), location, sublocation, dID, name, type ));
                            return this.notifier.sendMessage(message) > 0;

                        case 400:  //  in stable version cannot happen
                            logger.severe("Error, bad ADD_DEVICE request");
                            break;

                        case 409:
                            if( tentative++ == 10 )
                                return false;
                            dID = idGenerator.generateDID();
                            continue;



                        case 500:
                            logger.warning( "Error, internal server error of erlang network");
                            break;

                        default:
                    }
                }
                return false;
            }

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }
    }

    //  changes the device's subLocation into the user's smartHome. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - dID: the identifier of the device
    //      - name: the name of the device
    //      - location: the name of the current location in which the device is deployed
    //      - sublocation: the name of the current sublocation in which the device is deployed
    //      - newSublocation: the name of the sublocation in which the device has to be deployed
    //      - sublocID: the identifier of the sublocation in which the device has to be deployed
    //      - ipAddr: Ipv4 address of the location controller
    //      - port: port from which the controller is reachable(defined into addLocation request)
    //
    //  MESSAGE   [path: /device/{dID} POST ]
    //  {
    //     "subloc_id" : "..."
    //  }
    @Override
    public boolean changeDeviceSublocation( String username, String from, String dID, String name, String location, String subLocation, String newSubLocation, String sublocID, String ipAddr, int port ){

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        data.put( "subloc_id" , sublocID );


        try {

            Response result = this.sendCommand( ipAddr, port, "/device/" + dID, RESTsender.REQ_TYPE.POST, data ).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200: //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildChangeDeviceSubLocation( new Date(System.currentTimeMillis()), location, dID, name, newSubLocation ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad CHANGE_DEVICE_SUBLOCATION request");
                        break;

                    case 404:
                        logger.severe("Error, locID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }
    }

    //  changes the device's name identified by a dID. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: the current name of the location
    //      - sublocation: the current name of the subLocation
    //      - newName: the new name for the subLocation
    //      - locID: the identifier of the location
    //      - sublocID: the identifier of the subLocation
    //      - locID: identifier of the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //
    //  MESSAGE   [path: /device/{dID} POST ]
    //  {
    //     "name" : "...."
    //  }
    @Override
    public boolean changeDeviceName( String username, String from, String dID, String oldName, String newName, String ipAddr ) {

        HashMap<String,String> data = new HashMap<>();  //  message to be forwarded
        data.put( "name" , newName );

        try {

            Response result = this.sendCommand(ipAddr, Integer.parseInt(conf.get("control_port")), "/device/"+dID, RESTsender.REQ_TYPE.POST, data ).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200: //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRenameDevice( new Date(System.currentTimeMillis()), dID, oldName, newName ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad CHANGE_DEVICE_NAME request");
                        break;

                    case 404:
                        logger.severe("Error, locID/sublocID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
            e.printStackTrace();
            return false;
        }

    }

    //  removes the device from the user's smartHome. In case of success it automatically forward the update to all the involved components.
    //  Parameters:
    //      - username: email of the user
    //      - from: an identifier of the component which made the request. Prevent duplicated updates
    //      - location: the current name of the location
    //      - sublocation: the current name of the subLocation
    //      - newName: the new name for the subLocation
    //      - locID: the identifier of the location
    //      - sublocID: the identifier of the subLocation
    //      - locID: identifier of the location
    //      - ipAddr: Ipv4 address of the location controller/simulator(controller will be deployed in the same network of the simulator)
    //
    //  MESSAGE   [path: /device/{dID} REMOVE ]
    @Override
    public boolean removeDevice( String username, String from, String dID, String name, String ipAddr, int port ) {

        try {

            Response result = this.sendCommand(ipAddr, port, "/device/"+dID, RESTsender.REQ_TYPE.DELETE, null ).get();
            if (result != null) {

                switch (result.getStatus()) {
                    case 200:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates( DeviceUpdate.buildRemoveDevice( new Date(System.currentTimeMillis()), dID, name ));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad REMOVE_DEVICE request");
                        break;

                    case 404:
                        logger.severe("Error, locID/sublocID not found");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){
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
