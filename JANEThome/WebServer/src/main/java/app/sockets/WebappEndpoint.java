package app.sockets;

//  internal services
import config.interfaces.IConfiguration;
import db.interfaces.DBinterface;
import iot.DeviceType;
import iot.SmarthomeManager;
import login.beans.AuthData;
import rest.out.interfaces.RESTinterface;
import app.rabbit.in.WebUpdateReceiver;

//  utils
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;

//  ejb3.0
import javax.ejb.EJB;

//  http protocol management
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

//  exceptions
import java.io.IOException;

//  collections
import java.util.HashMap;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Websocket designed to receive the web clients request and forward them to the internal service and on the opposite
 * to update the associated webclient in case of requests from the internal service(automatic device updates, google home
 * modification on the shared smarthome)
 */
@ServerEndpoint( value="/controller", configurator = EndpointConfigurator.class )
public class WebappEndpoint {

    private Logger logger = null;
    private EndpointConfig config;       //  used to retrieve the communication channel parameters
    private HttpSession httpSession;     //  used to retrieve the webclient session
    private SmarthomeManager smarthome;  //  used to retrieve easily the smarthome information
    private String username;             //  used to retrieve the name of the user associated with the webclient
    private WebUpdateReceiver updater;   //  rabbitMQ input instance(to receive commands from the internal service)

    @EJB
    private IConfiguration configuration; //  configurations manager

    @EJB
    private RESTinterface restInterface;  //  rest interface instance(to send commands to devices)

    @EJB
    private DBinterface db;     //  database manager instance

    /**
     * Executed during the opening of the websocket, the EndpointConfig in combination with the EndpointConfigurator.class
     * is used to forward some servlet parameters into the webSocket component(http session, cookies)
     */
    @OnOpen
    public void onOpen( Session session, EndpointConfig config ){

        this.logger = LogManager.getLogger( getClass().getName() );

        //  saving the webclient channel components
        this.config = config;
        this.httpSession = (HttpSession) this.config.getUserProperties().get( "httpsession" );

        //  verification of the presence of the authData and httpsession objects and the validity of the authentication
        if( verification() ) {

            //  after verification() we already know that httpsession and authData are present
            this.username = ((AuthData) this.httpSession.getAttribute( "authData" )).getUser();

            //  getting the stored smarthome definition from the database/cache
            this.getSmarthome( this.username ); //  if the user hasn't already a smarthome the function creates a default one

            //  Generation of callback channel for web client update notification
            this.updater = new WebUpdateReceiver( this.username, session, this.smarthome, configuration );

            //  Sending the definition of the smartHome as first message to the web client
            try {

                session.getBasicRemote().sendText( this.smarthome.buildSmarthomeDefinition().trim() );

            }catch( IOException e ){

                e.printStackTrace();

            }

        }else{  //  user not authorized to make requests to the service. Closing the connection to prevent other requests

            this.sendMessage( new WebRequest( "EXPIRED_AUTH", new HashMap<>() ), session );
            this.dropClientInformation( session );

        }



    }

    /**
     * Executed after the receiving of a new message from the associated webClient.
     */
    @OnMessage
    public void onMessage( String message, Session session ) {

        if( this.logger == null )
            this.logger = LogManager.getLogger( getClass().getName() );

        this.logger.info("Message received with SessionID: " + session.getId() + " of user " + this.username );

        //  verification of the presence of the authData and httpsession objects and the validity of the authentication
        if( verification() ) {

            WebRequest request = WebRequest.buildRequest( message );  //  conversion of the message into an object

            if ( smarthome == null ){  //  over-control, a smartHome will be always present

                this.dropClientInformation( session );
                return;

            }

            logger.info( "Management of request: " + request.requestType().toString() + " of sessionID: " + session.getId());

            switch( request.requestType() ){  //  conversion of the request type into an enumerator for easy management

                case RENAME_LOCATION:
                    this.renameLocation( request, session );
                    break;

                case RENAME_SUBLOCATION:
                    this.renameSubLocation( request, session );
                    break;

                case RENAME_DEVICE:
                    this.renameDevice( request, session );
                    break;

                case ADD_LOCATION:
                    this.addLocation( request, session );
                    break;

                case ADD_SUBLOCATION:
                    this.addSubLocation( request, session );
                    break;

                case REMOVE_LOCATION:
                    this.removeLocation( request, session );
                    break;

                case REMOVE_SUBLOCATION:
                    this.removeSubLocation( request, session );
                    break;

                case ADD_DEVICE:
                    this.addDevice( request, session );
                    break;

                case CHANGE_DEVICE_SUBLOCATION:
                    this.changeDeviceSublocation( request, session );
                    break;

                case REMOVE_DEVICE:
                    this.removeDevice( request, session );
                    break;

                case STATISTIC:
                    this.getStatistic( request, session );
                    break;

                case UPDATE:

                    this.executeAction( request, session );
                    break;

                case LOGOUT:
                    this.dropClientInformation( session );
                    break;

                default:
                    logger.error("Error, request unknown: " + request.requestType().toString());
            }

        }else{  //  user not authorized to make requests to the service. Closing the connection to prevent other requests

            this.sendMessage( new WebRequest( "EXPIRED_AUTH", new HashMap<>() ), session );
            this.dropClientInformation( session );

        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {

        logger.info("Session closed with id: " +  session.getId());
        if( updater != null)
            updater.close();
        updater = null;

    }


    ////////--  [FUNCTIONALITIES: ADDING]  --////////

    /**
     * Method to forward to the inner service the request of adding a location
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void addLocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet( "location", "address", "port" ))
            return;

        //  verification that the location can be inserted from the server prospective
        boolean state = smarthome.addLocation(
                request.getData( "location" ),
                "" ,
                request.getData( "address" ),
                Integer.parseInt( request.getData( "port" )), true );

        //  if the addLocation test correctly done we request to insert
        //  the location in the real smarthome
        if( state )
            state = !this.restInterface.addLocation(
                    this.username,
                    "websocket_"+session.getId(),
                    request.getData( "location" ),
                    request.getData( "address" ),
                    Integer.parseInt( request.getData( "port" )));

        //  add location is the only action that a webclient cannot test by itself
        //  so we need a mechanism to notify it that the request is failed
        if ( !state )
            this.sendMessage( new WebRequest( "ERROR_LOCATION", new HashMap<>()), session );

    }

    /**
     * Method to forward to the inner service the request of adding a subLocation
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void addSubLocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet( "location", "sublocation" ))
            return;

        //  verification that the subLocation can be inserted from the server prospective
        if ( smarthome.addSubLocation( request.getData("location" ), request.getData( "sublocation" ), "", true )){

            String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
            String sublocID = this.smarthome.giveNextSublocID( request.getData( "location" ));

            if( network != null && sublocID != null ){

                String[] netInfo = network.split( ":" );

                //  forward the request to the real smarthome
                this.restInterface.addSubLocation(
                        this.username,
                        "websocket_"+session.getId(),
                        request.getData( "location" ),
                        request.getData( "sublocation" ),
                        sublocID,
                        netInfo[0], Integer.parseInt( netInfo[1] ));

            }
        }
    }

    /**
     * Method to forward to the inner service the request of adding a device
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void addDevice( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet( "location", "sublocation", "name", "type"  ))
            return;

        //  verification that the device can be inserted from the server prospective
        if( smarthome.addDevice(request.getData( "location" ),
                request.getData( "sublocation" ),
                "",
                request.getData( "name" ),
                DeviceType.StringToType(request.getData( "type" )), true )){

            String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
            String sublocID = this.smarthome.giveSubLocIdByName(request.getData( "location" ), request.getData( "sublocation" ));
            DeviceType type = DeviceType.StringToType(request.getData( "type" ));

            if( network != null && sublocID != null && type != DeviceType.UNKNOWN) {

                String[] netInfo = network.split( ":" );

                //  forward the request to the real smarthome
                this.restInterface.addDevice(
                        this.username,
                        "websocket_"+session.getId(),
                        request.getData( "name" ),
                        request.getData( "location" ),
                        request.getData( "sublocation" ),
                        sublocID,
                        type,
                        netInfo[0], Integer.parseInt( netInfo[1] ));
            }
        }
    }


    ////////--  [FUNCTIONALITIES: RENAMING]  --////////

    /**
     * Method to forward to the inner service the request of renaming a location
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void renameLocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if( !request.areSet( "old_name", "new_name" ) )
            return;

        //  verification that the location can be renamed from the server prospective
        if( this.smarthome.changeLocationName( request.getData( "old_name" ), request.getData( "new_name" ), true )){

            String locID = this.smarthome.giveLocIdByName(request.getData("old_name" ));

            if( locID != null && locID.length() > 0 ) {

                //  forward the request to the real smarthome
                this.restInterface.changeLocationName(
                        this.username,
                        "websocket_"+session.getId(),
                        locID,
                        request.getData( "old_name" ),
                        request.getData( "new_name" ));

            }
        }
    }

    /**
     * Method to forward to the inner service the request of renaming a subLocation
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void renameSubLocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet("location", "old_name", "new_name" ))
            return;

        //  verification that the subLocation can be renamed from the server prospective
        if( smarthome.changeSublocationName( request.getData( "location" ), request.getData( "old_name" ), request.getData( "new_name" ), true )){

            String locID =this.smarthome.giveLocIdByName(request.getData( "location" ));
            String sublocID = this.smarthome.giveSubLocIdByName( request.getData( "location" ), request.getData( "old_name" ));

            if( locID != null && sublocID != null ) {

                //  forward the request to the real smarthome
                this.restInterface.changeSubLocationName(
                        this.username,
                        "websocket_"+session.getId(),
                        request.getData( "location" ),
                        request.getData( "old_name" ),
                        locID,
                        sublocID,
                        request.getData("new_name"));

            }
        }
    }

    /**
     * Method to forward to the inner service the request of renaming a device
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void renameDevice( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet("old_name", "new_name"))
            return;

        //  verification that the device can be renamed from the server prospective
        if ( smarthome.changeDeviceName( request.getData( "old_name" ), request.getData( "new_name" ), true )){

            String dID = this.smarthome.giveDeviceIdByName( request.getData( "old_name" ));

            if( dID != null && dID.length() > 0 ) {

                //  forward the request to the real smarthome
                this.restInterface.changeDeviceName(
                        this.username,
                        "websocket_"+session.getId(),
                        dID,
                        request.getData( "old_name" ),
                        request.getData( "new_name" ));

            }
        }
    }


    ////////--  [FUNCTIONALITIES: DELETING]  --////////

    /**
     * Method to forward to the inner service the request of deleting a location
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void removeLocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet( "location" ))
            return;

        //  verification that the location can be removed from the server prospective
        if ( smarthome.removeLocation( request.getData( "location" ), true )){

            String locID = this.smarthome.giveLocIdByName(request.getData( "location" ));

            if( locID != null && locID.length() > 0 ) {

                //  forward the request to the real smarthome
                this.restInterface.removeLocation(
                        this.username,
                        "websocket_"+session.getId(),
                        request.getData( "location" ),
                        locID );
            }
        }
    }

    /**
     * Method to forward to the inner service the request of deleting a subLocation
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void removeSubLocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if( !request.areSet( "location", "sublocation" ))
            return;

        //  verification that the subLocation can be removed from the server prospective
        if( smarthome.removeSublocation( request.getData( "location" ), request.getData( "sublocation" ), true )){

            String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
            String sublocID = this.smarthome.giveSubLocIdByName( request.getData( "location" ), request.getData( "sublocation" ));

            if( network != null && network.length() > 0 && sublocID != null && sublocID.length() > 0 ) {

                String[] netInfo = network.split( ":" );

                //  forward the request to the real smarthome
                this.restInterface.removeSubLocation(
                        this.username,
                        "websocket_"+session.getId(),
                        request.getData( "location" ),
                        request.getData( "sublocation" ),
                        sublocID,
                        netInfo[0], Integer.parseInt( netInfo[1] ));
            }
        }
    }

    /**
     * Method to forward to the inner service the request of deleting a device
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void removeDevice( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if( !request.areSet( "name" ))
            return;

        //  verification that the device can be removed from the server prospective
        if( smarthome.removeDevice(request.getData( "name" ), true )) {

            String network = this.smarthome.giveDeviceNetwork( request.getData( "name" ));
            String dID = this.smarthome.giveDeviceIdByName( request.getData( "name" ));

            if( network != null && network.length() > 0 && dID != null && dID.length() > 0 ){

                String[] netInfo = network.split( ":" );

                //  forward the request to the real smarthome
                this.restInterface.removeDevice(
                        this.username,
                        "websocket_"+session.getId(),
                        dID,
                        request.getData( "name" ),
                        netInfo[0], Integer.parseInt( netInfo[1] ));

            }
        }
    }


    ////////--  [FUNCTIONALITIES: OTHERS]  --////////

    /**
     * Method to forward to the inner service the request of changing the device subLocation
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void changeDeviceSublocation( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet( "location", "sublocation", "name" ))
            return;

        //  verification that the subLocation can be moved from the server prospective
        if ( smarthome.changeDeviceSubLocation( request.getData( "location" ), request.getData( "sublocation" ), request.getData( "name" ), true )) {

            String network = this.smarthome.giveDeviceNetwork( request.getData( "name" ));
            String dID = this.smarthome.giveDeviceIdByName( request.getData( "name" ));
            String subLoc = this.smarthome.giveDeviceSubLocation( request.getData( "name" ));
            String subLocID = this.smarthome.giveSubLocIdByName( request.getData( "location" ) , request.getData( "sublocation" ));

            if( network != null && network.length() > 0 &&
                    dID != null && dID.length() > 0 &&
                        subLoc != null && subLoc.length() > 0 &&
                            subLocID != null && subLocID.length() > 0 ) {

                String[] netInfo = network.split( ":" );

                //  forward the request to the real smarthome
                this.restInterface.changeDeviceSublocation(
                        this.username,
                        "websocket_"+session.getId(),
                        dID,
                        request.getData( "name" ),
                        request.getData( "location" ),
                        subLoc,
                        request.getData( "sublocation" ),
                        subLocID,
                        netInfo[0], Integer.parseInt( netInfo[1] ));

            }
        }
    }

    /**
     * Method to forward to the inner service the request of executing a device action
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void executeAction( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if( !request.areSet( "device_name", "action", "value"))
            return;

        //  verification that the action can be executed from the server prospective
        if( smarthome.performAction(request.getData("device_name"), request.getData( "action"), request.getData( "value"), null, true )) {

            String network = this.smarthome.giveDeviceNetwork( request.getData( "device_name" ));
            String dID = this.smarthome.giveDeviceIdByName( request.getData( "device_name" ));

            if( network != null && network.length() > 0 && dID != null && dID.length() > 0 ) {

                String[] netInfo = network.split( ":" );

                //  forward the request to the real smarthome
                this.restInterface.execCommand(
                        this.username,
                        "websocket_"+session.getId(),
                        dID,
                        request.getData( "action" ),
                        request.getData( "value" ),
                        netInfo[0], Integer.parseInt( netInfo[1] ));
            }
        }
    }

    /**
     * Method to request to the database a specific statistic of a device
     * @param request Message received from the webclient
     * @param session Websocket session used to respond to the message if is is needed and get information
     */
    private void getStatistic( WebRequest request, Session session ){

        //  verification mandatory parameters present into the request
        if ( !request.areSet( "device_name", "statistic", "start", "stop" ))
            return;

        //  verification the device is present
        if (!smarthome.devicePresent( request.getData( "device_name" )))
            return;

        //  needed to convert the dates given by the web pages
        Gson clientGson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create(); //  server uses complete timestamp
        Gson serverGson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH" ).create();  //  client uses just day-hour for stats

        HashMap<String, String> statResponse = new HashMap<>();

        statResponse.put( "device_name", request.getData().get( "device_name" ));
        statResponse.put( "statistic", request.getData().get( "statistic" ));
        statResponse.put( "values", clientGson.toJson( db.getStatistics(
                this.smarthome.giveDeviceIdByName( request.getData().get( "device_name" )),
                this.smarthome.giveDeviceTypeByName( request.getData().get("device_name" )),
                request.getData().get( "statistic" ),
                serverGson.fromJson( request.getData().get( "start" ).replace( ":00:00.000Z", "" ), Date.class ),
                serverGson.fromJson( request.getData().get( "stop" ).replace( ":00:00.000Z", "" ), Date.class )
        )));

        WebRequest response = new WebRequest( request.getStringType(), statResponse );
        System.out.println("Results: " + clientGson.toJson( response ));
        this.sendMessage( response, session );

    }


    ////////--  UTILITIES  --////////


    /**
     * Function to remove all the client information preventing new access to the webpage without performing a new login
     */
    private void dropClientInformation( Session session ) {

        try {

            httpSession.removeAttribute( "authData" );
            httpSession.removeAttribute( "infoData" );
            session.close();

        }catch( IOException ignored ){}

    }

    /**
     *  The method verifies the validity of the user information and if it is authorized to perform a request.
     *  Authentication is based on two replicas of the same data, one is stored inside the user session
     *  and the other is given by the user with a cookie. If the code inside both the object is the same then
     *  the user is authorized to perform the request
     */
    private boolean verification(){

        logger.info( "Starting verification of information" );
        String authCookie = (String) this.config.getUserProperties().get( "cookie" );

        //  verification of object presence
        if( this.httpSession == null ){

            logger.error( "Error missing the session information. Verification failed" );
            return false;

        }

        if( authCookie == null || authCookie.length() == 0 ){

            logger.error( "Error missing the authentication cookie. Verification failed" );
            return false;

        }

        AuthData userData = (AuthData) this.httpSession.getAttribute( "authData" );
        if( userData == null ) {

            logger.error( "Error missing the user authentication information. Verification failed" );
            return false;

        }

        //  verification of authentication code
        if( userData.isValid( authCookie )){

            logger.info("User authorized to create a websocket: " + userData.getUser() );
            return true;

        }

        logger.error( "Warning, user not authorized to open a websocket" );
        return false;

    }

    /**
     *  The method searches a smarthome for the given user. Initially it tries to found the smarthome into the session
     *  of the user(already initialized session) otherwise it forward the request to the central database(new session).
     *  Moreover in the second case it is in charge of storing the smarthome into the session
     */
    private void getSmarthome( String username ){

        //  first we search into the webClient session
        this.smarthome = (SmarthomeManager) this.httpSession.getAttribute( "smarthome" );

        if( this.smarthome == null ) {

            //  if new session(no smarthome found) we request it to the database
            this.smarthome = db.getSmarthome( username );

            if( this.smarthome == null )  //  if no smarthome found on the database(cannot happen) we create a new one
                this.smarthome = new SmarthomeManager( username, true, configuration );

            this.smarthome.connect( this.configuration );  //  we enable auto-update of the smarthome
            this.httpSession.setAttribute( "smarthome", this.smarthome ); //  we save the smarthome into the session

        }

    }

    /**
     * Method to send a message to the webClient. Usefull for direct response to a request
     * @param response Message to send to the webclient(same format as received messages)
     * @param session Websocket session used to respond to the message
     */
    private void sendMessage( WebRequest response, Session session ){

        Gson clientGson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create();
        try {

            session.getBasicRemote().sendText( clientGson.toJson( response ));

        }catch( IOException e){

            e.printStackTrace();

        }

    }
}
