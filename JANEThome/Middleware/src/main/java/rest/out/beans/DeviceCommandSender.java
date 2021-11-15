package rest.out.beans;

//  internal services
import config.interfaces.IConfiguration;
import config.interfaces.GeneratorInterface;
import iot.DeviceType;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rest.DeviceBridge;
import rest.msg.RESTMessage;
import rabbit.out.interfaces.IRabbitSender;

//  exceptions
import rabbit.msg.InvalidMessageException;

//  jersey REST management
import rest.msg.out.req.*;
import rest.msg.out.resp.AddDeviceResp;
import rest.out.interfaces.RESTinterface;
import javax.ws.rs.core.Response;

//  ejb3.0
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

//  ExecutorServices
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service for sending messages to the smarthome controller/simulator via REST communications
 */
@Stateless
public class DeviceCommandSender implements RESTinterface {
    
    //  rabbitMQ sender service
    @EJB
    private IRabbitSender notifier;

    @EJB
    private IConfiguration configuration;     //  gives the configuration for the rest interface
    
    //  service for generating unique identifiers for devices and locations
    @EJB
    GeneratorInterface idGenerator; 
    
    private final Logger logger;
    private Properties privateConfiguration;  //  personal configuration of the REST sender
    
    //  shared pool of executors to send rest messages
    private final static ExecutorService executors = Executors.newCachedThreadPool(); 
    
    public DeviceCommandSender() {
        
        this.logger = LogManager.getLogger( getClass().getName() );
    
    }

    @PostConstruct
    private void initialization() {
        
        this.privateConfiguration = this.configuration.getConfiguration( "rest" );
        
    }

    
    //  sends a command to the device REST server
    private Future<Response> sendCommand(String address, int port, String path, RESTsender.REQ_TYPE reqType, RESTMessage request) {

        return executors.submit(new RESTsender(address, port, path, reqType, request));

    }


    ////////--  LOCATION REQUESTS  --////////


    /**
     *  Adds a new location to the user's smartHome. In case of success it automatically forwards the update to all the involved components.
     *  The locID information will be automatically generated and linked with the update forwarded to all the components
     *
     *  Destination: Simulator
     *  Path: /location/{locID} PUT
     *  BODY example:
     *  {
     *    "name" : "office",
     *    "user" : "example@service.it",
     *    "port" : "33334",
     *    "hostname" : "kali"
     *  }
     *  
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param location name of the location
     * @param ipAddr hostname/ipv4 of the location controller
     * @param port port used by the location controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean addLocation( String username, String from, String location, String ipAddr, int port ) {

        //  generation of a new unique location identifier(lID)
        String locID = idGenerator.generateLID();

        try {

            //  for some reasons can happen that the simulator refuses lID(already present), we loop until a valid one is found
            while( true ){
                
                Response result = this.sendCommand(
                        this.privateConfiguration.getProperty( "control_address" ),  //  address of the simulator
                        Integer.parseInt( this.privateConfiguration.getProperty( "control_port" )),  //  port of the simulator
                        "/location/" + locID, 
                        RESTsender.REQ_TYPE.PUT,    
                        new AddLocationReq( location, username, port, ipAddr )).get();  //  blocking request

                //  result management
                if ( result != null ) {

                    switch( result.getStatus() ) {

                        case 201:  //  if command correctly done, we notify it to all the involved components
                            DeviceUpdateMessage message = new DeviceUpdateMessage(username, from);
                            message.addUpdates(DeviceUpdate.buildAddLocation(new Date(System.currentTimeMillis()), location, locID, ipAddr, port));
                            return this.notifier.sendMessage(message) > 0;

                        case 400:  //  in stable version cannot happen
                            logger.error("Error, bad ADD_LOCATION request");
                            return false;

                        case 405: // in stable version cannot happen
                            logger.error( "Error, unsupported HTTP method on ADD_LOCATION");
                            return false;

                        case 406:
                            logger.error( "Error, hostname not valid into ADD_LOCATION");
                            return false;

                        case 409:  //  lID already present
                            locID = idGenerator.generateLID();
                            break;

                        case 412:
                            logger.error( "Error into ADD_LOCATION, port already assigned");
                            return false;

                        case 415:
                            logger.error( "Error into ADD_LOCATION, unsupported media type");
                            return false;

                        case 500:  //  an error has occurred inside the erlang network
                            logger.error("Error, internal server error of erlang network");
                            return false;

                        default:
                    }
                }
            }

        } catch (InvalidMessageException | InterruptedException | ExecutionException e){
            
            e.printStackTrace();
            return false;
        
        }

    }

    /**
     *  Changes the location name of a location. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Simulator
     *  Path: /location/{locID} POST
     *  BODY example:
     *  {
     *    "name" : "office"
     *  }
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param locID Identifier of a location(Stringed integer)
     * @param oldName Current name of the location
     * @param newName New name to be linked to the location
     * @return True in case of success otherwise false
     */
    @Override
    public boolean changeLocationName( String username, String from, String locID, String oldName, String newName ) {

        try {

            Response result = this.sendCommand(
                    this.privateConfiguration.getProperty( "control_address" ),  //  address of the simulator
                    Integer.parseInt(privateConfiguration.getProperty( "control_port" )),  //  port of the simulator
                    "/location/" + locID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateLocationReq( newName )).get();  //  blocking request

            //  result management
            if (result != null) {

                switch (result.getStatus()) {
                    
                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildRenameLocation( new Date( System.currentTimeMillis()), oldName, newName ));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad UPDATE_LOCATION request" );
                        break;

                    case 404:
                        logger.error( "Error, locID not found" );
                        break;

                    case 405: // in stable version cannot happen
                        logger.error( "Error, unsupported HTTP method on ADD_LOCATION" );
                        break;

                    case 415:
                        logger.error( "Error into ADD_LOCATION, unsupported media type" );
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warn( "Error, internal server error of erlang network" );
                        break;

                    default:
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ) {
            
            e.printStackTrace();
            return false;
        
        }
    }

    /**
     *  Removes the location identified by its locID. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Simulator
     *  Path: /location/{locID} DELETE
     *  NO BODY
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param name Name of the location to remove
     * @param locID Identifier of a location(Stringed integer) to remove
     * @return True in case of success otherwise false
     */
    @Override
    public boolean removeLocation( String username, String from, String name, String locID ) {

        try {

            Response result = this.sendCommand(
                    this.privateConfiguration.getProperty( "control_address" ),                 //  address of the simulator
                    Integer.parseInt( this.privateConfiguration.getProperty( "control_port" )), //  port of the simulator
                    "/location/" + locID,
                    RESTsender.REQ_TYPE.DELETE,
                    null ).get();   //  blocking request

            //  result management
            if( result != null ){

                switch ( result.getStatus() ) {

                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildRemoveLocation( new Date( System.currentTimeMillis() ), name));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error("Error, bad REMOVE_LOCATION request" );
                        break;

                    case 404:
                        logger.error("Error, locID not found" );
                        break;

                    case 405:
                        logger.error( "Error, unsupported HTTP method on REMOVE_LOCATION" );
                        break;

                    case 500:
                        logger.warn( "Error, internal server error of erlang network" );
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


    ////////--  SUBLOCATION REQUESTS  --////////


    /**
     *  Adds a new sublocation to the given location. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Controller
     *  Path: /sublocation/{subLocID} PUT
     *  BODY example:
     *  {
     *    "name" : "bedroom"
     *  }
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param location Location name in which deploy the subLocation
     * @param sublocation SubLocation name
     * @param sublocID Unique Controller subLocation Identifier(univocity only inside a controller) for the subLocation
     * @param ipAddr Address/Hostname of destination controller
     * @param port Port used by the destination controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean addSubLocation( String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port ) {

        AddSubLocationReq request = new AddSubLocationReq( sublocation );

        try {

            Response result = this.sendCommand(
                    ipAddr,                            //  address of the controller
                    port,                              //  port of the controller
                    "/sublocation/" + sublocID,
                    RESTsender.REQ_TYPE.PUT,
                    request ).get();                    //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ) {

                    case 201:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildAddSubLocation( new Date( System.currentTimeMillis() ), location, sublocation, sublocID ));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error("Error, bad ADD_SUBLOC request" );
                        break;

                    case 404:
                        logger.error( "Error, locID not found" );
                        break;

                    case 405: // in stable version cannot happen
                        logger.error( "Error, unsupported HTTP method on ADD_SUBLOC" );
                        break;

                    case 415:
                        logger.error( "Error into ADD_SUBLOC, unsupported media type" );
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warn("Error, internal server error of erlang network");
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

    /**
     *  Change a sublocation name identified by locID/sublocID. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Simulator
     *  Path: /location/{locID}/{subLocID} POST
     *  BODY example:
     *  {
     *    "name" : "living room"
     *  }
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param location Location name in which the subLocation is deployed
     * @param sublocation Current subLocation name
     * @param locID Unique identifier of the location
     * @param sublocID Unique Controller subLocation Identifier(univocity only inside a controller) for the subLocation
     * @param newName New name to assign to the subLocation
     * @return True in case of success otherwise false
     */
    @Override
    public boolean changeSubLocationName( String username, String from, String location, String sublocation, String locID, String sublocID, String newName ) {

        try {

            Response result = this.sendCommand(
                    this.privateConfiguration.getProperty( "control_address" ),                 //  address of the simulator
                    Integer.parseInt( this.privateConfiguration.getProperty( "control_port" )), //  port of the simulator
                    "/location/" + locID + "/sublocation/" + sublocID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateSubLocationReq( newName )).get();    //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ){

                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildRenameSubLocation( new Date( System.currentTimeMillis() ), location, sublocation, newName ));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad UPDATE_SUBLOCATION request" );
                        break;

                    case 404:
                        logger.error( "Error, {locID,sublID} not found" );
                        break;

                    case 405: // in stable version cannot happen
                        logger.error( "Error, unsupported HTTP method on UPDATE_SUBLOCATION" );
                        break;

                    case 415:
                        logger.error( "Error into UPDATE_SUBLOCATION, unsupported media type" );
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warn( "Error, internal server error of erlang network" );
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

    /**
     *  Removes a sublocation from the given location. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Controller
     *  Path: /sublocation/{subLocID} DELETE
     *  NO BODY
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param location Location name in which the subLocation is deployed
     * @param sublocation SubLocation name
     * @param sublocID Unique Controller subLocation Identifier(univocity only inside a controller) for the subLocation
     * @param ipAddr Address/Hostname of destination controller
     * @param port Port used by the destination controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean removeSubLocation(String username, String from, String location, String sublocation, String sublocID, String ipAddr, int port) {

        try {

            Response result = this.sendCommand(
                    ipAddr,                            //  address of the controller
                    port,                              //  port of the controller
                    "/sublocation/" + sublocID,
                    RESTsender.REQ_TYPE.DELETE,
                    null ).get();               //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ){

                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildRemoveSubLocation( new Date(System.currentTimeMillis()), location, sublocation ));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad REMOVE_SUB_LOCATION request" );
                        break;

                    case 404:
                        logger.error( "Error, locID not found" );
                        break;

                    case 405:
                        logger.error( "Error, unsupported HTTP method on REMOVE_SUB_LOCATION" );
                        break;

                    case 500:
                        logger.warn( "Error, internal server error of erlang network" );
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


    ////////--  DEVICES REQUESTS  --////////


    /**
     *  Removes a sublocation from the given location. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Controller
     *  Path: /device/{dID} PUT
     *  BODY example:
     *  {
     *    "subloc_id" : 234,
     *    "name" : "MyLamp",
     *    "type" : "Light"
     *  }
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param name Name of the device
     * @param location Location name in which the device will be deployed
     * @param sublocation SubLocation name in which the device will be deployed
     * @param sublocID Unique Controller subLocation Identifier(univocity only inside a controller) for the subLocation
     * @param type Device type in controller format(Light,Fan,Door,Conditioner,Thermostat)
     * @param ipAddr Address/Hostname of destination controller
     * @param port Port used by the destination controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean addDevice(String username, String from, String name, String location, String sublocation, String sublocID, DeviceType type, String ipAddr, int port ){

        String dID = idGenerator.generateDID();

        AddDeviceReq request = new AddDeviceReq(
                Integer.parseInt( sublocID ),
                name,
                type.toString().replace( "action.devices.types.", "" ),  //  easiest way to generate the controller types
                ipAddr );

        try {

            //  for some reasons can happen that the controller refuses dID(already present), we loop until a valid one is found
            while (true) {

                Response result = this.sendCommand(
                        ipAddr,                            //  address of the controller
                        port,                              //  port of the controller
                        "/device/" + dID,
                        RESTsender.REQ_TYPE.PUT,
                        request ).get();                    //  blocking request

                //  result management
                if( result != null ){

                    switch( result.getStatus() ){

                        case 200:  //  if command is correctly done, we notify it to all the involved components

                            //  into the response we find the initial status of the device, we use it to initialize its representation
                            AddDeviceResp response = result.readEntity( AddDeviceResp.class );
                            DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );

                            //  inserting "add device" as first request
                            message.addUpdates( DeviceUpdate.buildAddDevice(
                                    new Date(System.currentTimeMillis()),
                                    location,
                                    sublocation,
                                    response.getDev_id(),
                                    name,
                                    type ));

                            //  for each device parameter we insert a request to update it
                            response.getState().forEach( ( key,value ) -> message.addUpdates(
                                    DeviceUpdate.buildDeviceUpdate( new Date( System.currentTimeMillis() ),
                                                                    response.getDev_id(),
                                                                    DeviceBridge.controllerToServiceTrait( key ),
                                                                    DeviceBridge.controllerToServiceValue( value ))));

                            return this.notifier.sendMessage( message ) > 0;

                        case 400:  //  in stable version cannot happen
                            logger.error( "Error, bad ADD_DEVICE request" );
                            return false;

                        case 404:
                            logger.error( "Error, {locID,sublocID} not found" );
                            return false;

                        case 405:
                            logger.error( "Error, unsupported HTTP method on ADD_DEVICE" );
                            return false;

                        case 406:
                            logger.error( "Error, hostname not allowed" );
                            return false;

                        case 409:   //  dID already present
                            dID = idGenerator.generateDID();
                            break;

                        case 500:
                            logger.warn( "Error, internal server error of erlang network" );
                            return false;

                        default:
                    }
                }
            }

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){

            e.printStackTrace();
            return false;

        }
    }

    /**
     *  Changes the device's subLocation into the user's smartHome. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Controller
     *  Path: /device/{dID} POST
     *  BODY example:
     *  {
     *    "subloc_id" : 234
     *  }
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param dID Unique identifier of the device
     * @param name Name of the device
     * @param location Location name in which the device will be deployed
     * @param subLocation Current subLocation name in which the device will be deployed
     * @param newSubLocation Sublocation name in which e the device
     * @param sublocID Unique Controller subLocation Identifier(univocity only inside a controller) for the current subLocation
     * @param ipAddr Address/Hostname of destination controller
     * @param port Port used by the destination controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean changeDeviceSublocation( String username, String from, String dID, String name, String location, String subLocation, String newSubLocation, String sublocID, String ipAddr, int port) {

        try {

            Response result = this.sendCommand(
                    ipAddr,                            //  address of the controller
                    port,                              //  port of the controller
                    "/device/" + dID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateDeviceSubLocReq( sublocID )).get();                    //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ){

                    case 204: //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildChangeDeviceSubLocation(
                                new Date(System.currentTimeMillis()),
                                location,
                                dID,
                                name,
                                newSubLocation ));

                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad UPDATE_DEVICE_SUB_LOC request" );
                        break;

                    case 404:
                        logger.error( "Error, dID not found" );
                        break;

                    case 405: // in stable version cannot happen
                        logger.error( "Error, unsupported HTTP method on UPDATE_DEVICE_SUB_LOC" );
                        break;

                    case 415:
                        logger.error( "Error into UPDATE_DEVICE_SUB_LOC, unsupported media type" );
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warn( "Error, internal server error of erlang network" );
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

    /**
     *  Changes the device's subLocation into the user's smartHome. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Simulator
     *  Path: /device/{dID} POST
     *  BODY example:
     *  {
     *    "name" : "MyLamp"
     *  }
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param dID Unique identifier of the device
     * @param oldName Name of the device
     * @param newName Location name in which the device will be deployed
     * @return True in case of success otherwise false
     */
    @Override
    public boolean changeDeviceName( String username, String from, String dID, String oldName, String newName ) {

        try {

            Response result = this.sendCommand(
                    this.privateConfiguration.getProperty( "control_address" ),                 //  address of the simulator
                    Integer.parseInt( this.privateConfiguration.getProperty( "control_port" )), //  port of the simulator
                    "/device/" + dID,
                    RESTsender.REQ_TYPE.POST,
                    new UpdateDeviceNameReq( newName )).get();                    //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ){

                    case 204: //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildRenameDevice( new Date( System.currentTimeMillis()), dID, oldName, newName ));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad UPDATE_DEVICE_NAME request" );
                        break;

                    case 404:
                        logger.error( "Error, dID not found" );
                        break;

                    case 405: // in stable version cannot happen
                        logger.error( "Error, unsupported HTTP method on UPDATE_DEVICE_NAME" );
                        break;

                    case 415:
                        logger.error( "Error into UPDATE_DEVICE_NAME, unsupported media type" );
                        break;

                    case 500:  //  an error has occurred inside the erlang network
                        logger.warn( "Error, internal server error of erlang network" );
                        break;
                }
            }

            return false;

        }catch( InvalidMessageException | InterruptedException | ExecutionException e ){

            e.printStackTrace();
            return false;

        }

    }

    /**
     *  Removes the device from the user's smartHome. In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Controller
     *  Path: /device/{dID} DELETE
     *  NO BODY
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param dID Unique identifier of the device
     * @param name Name of the device to be removed
     * @param ipAddr Address/Hostname of destination controller
     * @param port Port used by the destination controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean removeDevice( String username, String from, String dID, String name, String ipAddr, int port ) {

        try {

            Response result = this.sendCommand(
                    ipAddr,                            //  address of the controller
                    port,                              //  port of the controller
                    "/device/" + dID,
                    RESTsender.REQ_TYPE.DELETE,
                    null ).get();                    //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ){

                    case 204:  //  if command correctly done, we notify it to all the involved components
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildRemoveDevice( new Date( System.currentTimeMillis() ), dID, name ));
                        return this.notifier.sendMessage( message ) > 0;

                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad REMOVE_DEVICE request" );
                        break;

                    case 404:
                        logger.error( "Error, dID not found" );
                        break;

                    case 405:
                        logger.error( "Error, unsupported HTTP method on REMOVE_DEVICE" );
                        break;

                    case 500:
                        logger.warn( "Error, internal server error of erlang network" );
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

    /**
     *  Executes the given command to the specified device of the user's smartHome.
     *  In case of success it automatically forwards the update to all the involved components
     *
     *  Destination: Controller
     *  Path: /devcommands PATCH
     *  BODY example:
     *  [
     *   {
     *      "dev_id": 123,
     *      "actions": {
     *          "onOff": "on",
     *          "brightness": 50
     *      }
     *   }
     *  ]
     *
     * @param username Email of the user
     * @param from An identifier of the component which made the request(see rabbit.out package)
     * @param dID Unique identifier of the device
     * @param action Internal Service formatted device action
     * @param value Internal Service formatted action value
     * @param ipAddr Address/Hostname of destination controller
     * @param port Port used by the destination controller
     * @return True in case of success otherwise false
     */
    @Override
    public boolean execCommand( String username, String from, String dID, String action, String value, String ipAddr, int port ) {

        HashMap<String,Object> req = new HashMap<>();
        req.put( DeviceBridge.serviceToControllerTrait( action ), DeviceBridge.serviceToControllerValue( action, value ));
        try{
            Response result = this.sendCommand(
                    ipAddr,                            //  address of the controller
                    port,                              //  port of the controller
                    "/devcommands",
                    RESTsender.REQ_TYPE.PATCH,
                    new ExecCommandsReq(   //  we can send more than one action, i will update if needed by Google home
                            Collections.singletonList(
                                    new ExecCommandReq( Integer.parseInt( dID ), req )
                            )
                    )).get();        //  blocking request

            //  result management
            if( result != null ){

                switch( result.getStatus() ){

                    case 200:
                        DeviceUpdateMessage message = new DeviceUpdateMessage( username, from );
                        message.addUpdates( DeviceUpdate.buildDeviceUpdate(
                                                new Date( System.currentTimeMillis() ),
                                                dID,
                                                action,
                                                value ));

                        return this.notifier.sendMessage( message ) > 0;


                    case 400:  //  in stable version cannot happen
                        logger.error( "Error, bad EXEC_COMMAND request" );
                        break;

                    case 405:
                        logger.error( "Error, method not allowed into EXEC_COMMAND" );
                        break;

                    case 415:
                        logger.error( "Error, unsupported mediaType on EXEC_COMMAND" );
                        break;

                    case 500:
                        logger.warn( "Error, internal server error of erlang network" );
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
}
