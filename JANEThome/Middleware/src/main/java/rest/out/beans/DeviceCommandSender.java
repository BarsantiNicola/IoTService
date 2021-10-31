package rest.out.beans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.interfaces.ConfigurationInterface;
import config.interfaces.GeneratorInterface;
import iot.SmarthomeDevice;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.msg.RESTMessage;
import rest.msg.out.req.*;
import rest.msg.out.resp.AddDeviceResp;
import rest.out.interfaces.RESTinterface;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import java.util.*;
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
    private Properties conf;        //  configuration of the service

    @EJB
    private ConfigurationInterface configuration;   //  gives the configuration for the rest interface

    @EJB
    GeneratorInterface idGenerator;             //  generates unique identifiers for the resource of the service

    private final static ExecutorService executors = Executors.newCachedThreadPool(); //  shared pool of executors to send rest messages


    private static final String[] convertedTraits = {
            "onOff",
            "fanSpeed",
            "brightness",
            "color",
            "openClose",
            "lockUnlock",
            "tempTarget",
            "tempCurrent",
            "connectivity"
    };

    private static final String[] receivedTraits = {
            "action.devices.traits.OnOff",
            "action.devices.traits.FanSpeed",
            "action.devices.traits.Brightness",
            "action.devices.traits.ColorSetting",
            "action.devices.traits.OpenClose",
            "action.devices.traits.LockUnlock",
            "action.devices.traits.TemperatureSetting",
            "action.devices.traits.Temperature",
            "action.devices.traits.Connectivity"
    };

    private String controllerToServiceTrait( String trait ){

        int index = this.controllerToServiceIndex( trait );
        if( index == -1 )
            return "";
        else
            return DeviceCommandSender.receivedTraits[index];

    }

    private String controllerToServiceValue( String value ){
        if( value.compareTo("on")== 0 || value.compareTo("open") == 0 || value.compareTo("lock") == 0 )
            return "1";
        if( value.compareTo("off") == 0 || value.compareTo("close") == 0 || value.compareTo("unlock") == 0 )
            return "0";
        return value;
    }

    private Object serviceToControllerValue( String action, String value ){
        switch( this.serviceToControllerIndex(action)){
            case 0:
                if( value.compareTo("1") == 0 )
                    return "on";
                else
                    return "off";
            case 4:
                if( value.compareTo("1") == 0 )
                    return "open";
                else
                    return "close";
            case 5:
                if( value.compareTo("1") == 0 )
                    return "lock";
                else
                    return "unlock";
            default:
                try{
                    return Integer.parseInt(value);
                }catch(Exception e){
                    return Math.round(Float.parseFloat(value));
                }

        }
    }

    private String serviceToControllerTrait( String trait ){
        int index = this.serviceToControllerIndex( trait );
        if( index == -1 )
            return "";
        else
            return DeviceCommandSender.convertedTraits[index];
    }

    private int controllerToServiceIndex( String trait ){
        for( int a = 0; a<DeviceCommandSender.convertedTraits.length; a++ )
            if( DeviceCommandSender.convertedTraits[a].compareTo(trait) == 0)
                return a;
        return -1;
    }

    private int serviceToControllerIndex( String trait ){

        for( int a = 0; a<DeviceCommandSender.receivedTraits.length; a++ )
            if( DeviceCommandSender.receivedTraits[a].compareTo(trait) == 0)
                return a;
        return -1;
    }

    public DeviceCommandSender() {
        this.initializeLogger();
    }

    @PostConstruct
    private void initialization() {
        this.conf = configuration.getConfiguration("rest");
    }

    private void initializeLogger() {

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if (logger.getHandlers().length == 0) { //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }
    }


    //  sends a command to the device REST server
    private Future<Response> sendCommand(String address, int port, String path, RESTsender.REQ_TYPE reqType, RESTMessage request) {

        return executors.submit(new RESTsender(address, port, path, reqType, request));

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
    //      "port" : "....",
    //      "hostname" : "...."
    //  }
    @Override
    public boolean addLocation( String username, String from, String location, String ipAddr, int port ) {

        String locID = idGenerator.generateLID();

        try {

            while (true) {

                Response result = this.sendCommand(
                        this.conf.getProperty("control_address"),
                        Integer.parseInt(this.conf.getProperty("control_port")),
                        "/location/" + locID,
                        RESTsender.REQ_TYPE.PUT,
                        new AddLocationReq( location, username, port, ipAddr ))
                        .get();

                if (result != null) {

                    switch (result.getStatus()) {

                        case 201:  //  if command correctly done, we notify it to all the involved components
                            DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                            message.addUpdates(DeviceUpdate.buildAddLocation(new Date(System.currentTimeMillis()), location, locID, ipAddr, port));
                            return this.notifier.sendMessage(message) > 0;

                        case 400:  //  in stable version cannot happen
                            logger.severe("Error, bad ADD_LOCATION request");
                            return false;

                        case 405: // in stable version cannot happen
                            logger.severe( "Error, unsupported HTTP method on ADD_LOCATION");
                            return false;

                        case 406:
                            logger.severe( "Error, hostname not valid into ADD_LOCATION");
                            return false;

                        case 409:  //  TODO to be splitted into future updates into two errors: duplicate port / duplicate locID
                            logger.severe("Error, duplicated locID");
                            locID = idGenerator.generateLID();
                            break;

                        case 412:
                            logger.severe( "Error into ADD_LOCATION, port already assigned");
                            return false;

                        case 415:
                            logger.severe( "Error into ADD_LOCATION, unsupported media type");
                            return false;

                        case 500:  //  an error has occurred inside the erlang network
                            logger.warning("Error, internal server error of erlang network");
                            return false;

                        default:
                    }
                }
            }

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean changeLocationName(String username, String from, String locID, String oldName, String newName, String ipAddr) {

        try {

            Response result = this.sendCommand(
                    this.conf.getProperty("control_address"),
                    Integer.parseInt(conf.getProperty("control_port")),
                    "/location/" + locID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateLocationReq( newName )).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRenameLocation(new Date(System.currentTimeMillis()), oldName, newName));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad UPDATE_LOCATION request");
                        break;

                    case 404:
                        logger.severe( "Error, locID not found");
                        break;

                    case 405: // in stable version cannot happen
                        logger.severe( "Error, unsupported HTTP method on ADD_LOCATION");
                        break;

                    case 415:
                        logger.severe( "Error into ADD_LOCATION, unsupported media type");
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean removeLocation(String username, String from, String name, String locID, String ipAddr) {

        try {

            Response result = this.sendCommand(
                    this.conf.getProperty("control_address"),
                    Integer.parseInt(conf.getProperty("control_port")),
                    "/location/" + locID,
                    RESTsender.REQ_TYPE.DELETE,
                    null ).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRemoveLocation(new Date(System.currentTimeMillis()), name));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad REMOVE_LOCATION request");
                        break;

                    case 404:
                        logger.severe("Error, locID not found");
                        break;

                    case 405:
                        logger.severe( "Error, unsupported HTTP method on REMOVE_LOCATION");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean addSubLocation(String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port) {

        AddSubLocationReq request = new AddSubLocationReq( sublocation );

        try {

            Response result = this.sendCommand(
                    ipAddr,
                    port,
                    "/sublocation/" + sublocID,
                    RESTsender.REQ_TYPE.PUT,
                    request).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 201:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildAddSubLocation(new Date(System.currentTimeMillis()), location, sublocation, sublocID));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad ADD_SUBLOC request");
                        break;

                    case 404:
                        logger.severe( "Error, locID not found");
                        break;

                    case 405: // in stable version cannot happen
                        logger.severe( "Error, unsupported HTTP method on ADD_SUBLOC");
                        break;

                    case 415:
                        logger.severe( "Error into ADD_SUBLOC, unsupported media type");
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean changeSubLocationName(String username, String from, String location, String sublocation, String locID, String sublocID, String newName, String ipAddr) {

        try {

            Response result = this.sendCommand(
                    this.conf.getProperty("control_address"),
                    Integer.parseInt(conf.getProperty("control_port")),
                    "/location/" + locID + "/sublocation/" + sublocID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateSubLocationReq( newName )).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRenameSubLocation(new Date(System.currentTimeMillis()), location, sublocation, newName));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad UPDATE_SUBLOCATION request");
                        break;

                    case 404:
                        logger.severe( "Error, {locID,sublID} not found");
                        break;

                    case 405: // in stable version cannot happen
                        logger.severe( "Error, unsupported HTTP method on UPDATE_SUBLOCATION");
                        break;

                    case 415:
                        logger.severe( "Error into UPDATE_SUBLOCATION, unsupported media type");
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warning("Error, internal server error of erlang network");
                        break;


                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean removeSubLocation(String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port) {

        try {

            Response result = this.sendCommand(
                    ipAddr,
                    port,
                    "/sublocation/" + sublocID,
                    RESTsender.REQ_TYPE.DELETE,
                    null ).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRemoveSubLocation(new Date(System.currentTimeMillis()), location, sublocation));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad REMOVE_SUB_LOCATION request");
                        break;

                    case 404:
                        logger.severe("Error, locID not found");
                        break;

                    case 405:
                        logger.severe( "Error, unsupported HTTP method on REMOVE_SUB_LOCATION");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean addDevice(String username, String from, String name, String location, String sublocation, String sublocID, SmarthomeDevice.DeviceType type, String ipAddr, int port) {

        String dID = idGenerator.generateDID();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            while (true) {

                Response result = this.sendCommand(
                        ipAddr,
                        port,
                        "/device/" + dID,
                        RESTsender.REQ_TYPE.PUT,
                        new AddDeviceReq(
                                Integer.parseInt(sublocID),
                                name,
                                type.toString().replace("action.devices.types.", ""),
                                ipAddr)).get();

                if (result != null) {

                    switch (result.getStatus()) {
                        case 200:  //  if command is correctly done, we notify it to all the involved components
                            AddDeviceResp response = result.readEntity(AddDeviceResp.class);
                            System.out.println("GSON: " + gson.toJson(response));
                            DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                            message.addUpdates(DeviceUpdate.buildAddDevice(new Date(System.currentTimeMillis()), location, sublocation, response.getDev_id(), name, type));
                            response.getState().forEach( (key,value) -> System.out.println("DATA: " + key + " value: " + value + " converted: " + this.controllerToServiceTrait( key ) + ":" + this.controllerToServiceValue(value)));
                            response.getState().forEach( (key,value) -> message.addUpdates( DeviceUpdate.buildDeviceUpdate(new Date(System.currentTimeMillis()), response.getDev_id(), this.controllerToServiceTrait( key ), this.controllerToServiceValue(value))));
                            return this.notifier.sendMessage(message) > 0;

                        case 400:  //  in stable version cannot happen
                            logger.severe("Error, bad ADD_DEVICE request");
                            return false;

                        case 404:
                            logger.severe("Error, {locID,sublocID} not found");
                            return false;

                        case 405:
                            logger.severe( "Error, unsupported HTTP method on ADD_DEVICE");
                            return false;

                        case 406:
                            logger.severe( "Error, hostname not allowed");
                            return false;

                        case 409:
                            dID = idGenerator.generateDID();
                            break;

                        case 500:
                            logger.warning("Error, internal server error of erlang network");
                            return false;

                        default:
                    }
                }
            }

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean changeDeviceSublocation(String username, String from, String dID, String name, String location, String subLocation, String newSubLocation, String sublocID, String ipAddr, int port) {

        try {

            Response result = this.sendCommand(
                    ipAddr,
                    port,
                    "/device/" + dID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateDeviceSubLocReq( sublocID )).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204: //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildChangeDeviceSubLocation(new Date(System.currentTimeMillis()), location, dID, name, newSubLocation));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad UPDATE_DEVICE_SUB_LOC request");
                        break;

                    case 404:
                        logger.severe( "Error, dID not found");
                        break;

                    case 405: // in stable version cannot happen
                        logger.severe( "Error, unsupported HTTP method on UPDATE_DEVICE_SUB_LOC");
                        break;

                    case 415:
                        logger.severe( "Error into UPDATE_DEVICE_SUB_LOC, unsupported media type");
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean changeDeviceName(String username, String from, String dID, String oldName, String newName, String ipAddr) {

        try {

            Response result = this.sendCommand(
                    conf.getProperty("control_address"),
                    Integer.parseInt(conf.getProperty("control_port")),
                    "/device/" + dID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateDeviceNameReq( newName )).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204: //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRenameDevice(new Date(System.currentTimeMillis()), dID, oldName, newName));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad UPDATE_DEVICE_NAME request");
                        break;

                    case 404:
                        logger.severe( "Error, dID not found");
                        break;

                    case 405: // in stable version cannot happen
                        logger.severe( "Error, unsupported HTTP method on UPDATE_DEVICE_NAME");
                        break;

                    case 415:
                        logger.severe( "Error into UPDATE_DEVICE_NAME, unsupported media type");
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warning("Error, internal server error of erlang network");
                        break;
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
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
    public boolean removeDevice(String username, String from, String dID, String name, String ipAddr, int port) {

        try {

            Response result = this.sendCommand(
                    ipAddr,
                    port,
                    "/device/" + dID,
                    RESTsender.REQ_TYPE.DELETE,
                    null ).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildRemoveDevice(new Date(System.currentTimeMillis()), dID, name));
                        return this.notifier.sendMessage(message) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad REMOVE_DEVICE request");
                        break;

                    case 404:
                        logger.severe("Error, dID not found");
                        break;

                    case 405:
                        logger.severe( "Error, unsupported HTTP method on REMOVE_DEVICE");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    //  executes the given command to the specified device of the user's smartHome
    public boolean execCommand(String username, String from, String dID, String action, String value, String ipAddr, int port) {

        HashMap<String,Object> req = new HashMap<>();
        req.put( this.serviceToControllerTrait(action), this.serviceToControllerValue(action, value));
        try{
            Response result = this.sendCommand(
                    ipAddr,
                    port,
                    "/devcommands",
                    RESTsender.REQ_TYPE.PATCH,
                    new ExecCommandsReq(Collections.singletonList(new ExecCommandReq(Integer.parseInt(dID), req)))).get();

            if (result != null) {

                switch (result.getStatus()) {
                    case 200:
                        DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                        message.addUpdates(DeviceUpdate.buildDeviceUpdate(new Date(System.currentTimeMillis()), dID, action, value));
                        return this.notifier.sendMessage(message) > 0;


                    case 400:  //  in stable version cannot happen
                        logger.severe("Error, bad EXEC_COMMAND request");
                        break;

                    case 405:
                        logger.severe( "Error, method not allowed into EXEC_COMMAND");
                        break;

                    case 415:
                        logger.severe( "Error, unsupported mediaType on EXEC_COMMAND");
                        break;

                    case 500:
                        logger.warning("Error, internal server error of erlang network");
                        break;

                    default:
                }
            }

            return false;

        } catch (InvalidMessageException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }

    }
}
