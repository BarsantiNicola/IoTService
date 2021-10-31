package weblogic.login.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.interfaces.ConfigurationInterface;
import db.interfaces.DBinterface;
import iot.SmarthomeManager;
import iot.SmarthomeDevice;
import iot.SmarthomeWebDevice;
import rest.out.interfaces.RESTinterface;
import weblogic.login.EndpointConfigurator;
import utils.rabbit.in.WebUpdateReceiver;
import weblogic.login.beans.BasicData;

import javax.ejb.EJB;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

///////////////////////////////////////////////[ WebappEndpoint ]////////////////////////////////////////////////////
//                                                                                                                 //
//   Websocket for communication between the server and the webclients into an async way. It is used by the webapp //
//   to send request and the responses.                                                                            //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@ServerEndpoint( value="/controller", configurator = EndpointConfigurator.class )
public class WebappEndpoint {

    private Logger logger;
    private EndpointConfig config;       //  used to retrieve the communication channel outside the functions
    private SmarthomeManager smarthome;  //  used to retrieve easily the smarthome information directly
    private String username;             //  used to retrieve the name of the user associated with the web page

    @SuppressWarnings("unused")
    private WebUpdateReceiver updater;   //  used to receive messages from the middleware

    @EJB
    private ConfigurationInterface configuration; //  configuration of the service

    @EJB
    private RESTinterface restInterface;  //  interface to send commands to the devices

    @EJB
    private DBinterface db; //  interface to request data from the database

    @OnOpen
    public void onOpen( Session session, EndpointConfig config ){

        this.config = config;
        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if( this.logger.getHandlers().length == 0 ){ //  only the first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter( new SimpleFormatter() );
            this.logger.addHandler( consoleHandler );

        }

        //  verification of the presence of the authData and httpsession objects and the validity of the authentication
        if( verification()) {

            BasicData userData  = (BasicData)((HttpSession) config.getUserProperties().get( "httpsession" )).getAttribute( "authData" );
            this.username = userData.getUser();

            //  getting the stored smarthome definition from the database/cache
            this.getSmarthome( this.username ); //  if the user hasn't already a smarthome the function creates a default one

            //  Generation of callback channel for web client update notification
            this.updater = new WebUpdateReceiver( this.username , session, this.smarthome, configuration );

            //  Sending the definition of the smartHome as first message to the web client
            try {

                session.getBasicRemote().sendText(this.smarthome.buildSmarthomeDefinition().trim());

            } catch (IOException e) {

                e.printStackTrace();

            }

        }else{

            try {

                session.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }


    }

    @OnMessage
    public void onMessage( String message, Session session ) {

        this.logger.info("Message received with SessionID: " + session.getId() + " of user " + this.username );

        //  verification of the presence of the authData and httpsession objects and the validity of the authentication
        if( verification()) {

            WebRequest request = WebRequest.buildRequest( message );  //  conversion of the message into an object
            HttpSession httpSession = (HttpSession) config.getUserProperties().get( "httpsession" );

            //  needed to convert the dates given by the web pages
            Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create();
            Gson gson2 = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH").create();
            String dID;

            if ( smarthome == null ) {

                logger.severe( "Error a user is trying to use a session in which the smartHome definition is not initialized" );
                httpSession.removeAttribute( "authData" );
                httpSession.removeAttribute( "infoData" );

                try {

                    session.close();

                } catch (IOException e) {

                    e.printStackTrace();

                }
                return;

            }

            logger.info( "Management of request: " + request.requestType().toString() + " of sessionID: " + session.getId());

            switch( request.requestType() ) {

                case RENAME_LOCATION:
                    if( !request.areSet( "old_name", "new_name" ) )
                        return;

                    if( this.smarthome.changeLocationName(request.getData( "old_name" ), request.getData( "new_name" ), true )){

                        String network = this.smarthome.giveLocationNetwork( request.getData( "old_name" ));
                        if( network != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.changeLocationName(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    this.smarthome.giveLocIdByName(request.getData("old_name" )),
                                    request.getData( "old_name" ),
                                    request.getData( "new_name" ),
                                    netInfo[0]);
                        }
                    }
                    break;

                case RENAME_SUBLOCATION:

                    if ( !request.areSet("location", "old_name", "new_name" ))
                        return;

                    if (smarthome.changeSublocationName(request.getData("location"), request.getData("old_name"), request.getData("new_name"), true )){

                        String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
                        if( network != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.changeSubLocationName(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    request.getData( "location" ),
                                    request.getData("old_name"),
                                    this.smarthome.giveLocIdByName(request.getData( "location")),
                                    this.smarthome.giveSubLocIdByName(request.getData( "location"), request.getData( "old_name")),
                                    request.getData("new_name"),
                                    netInfo[0]);
                        }
                    }

                    break;

                case RENAME_DEVICE:

                    if ( !request.areSet("old_name", "new_name"))
                        return;

                    if ( smarthome.changeDeviceName( request.getData( "old_name" ), request.getData( "new_name" ), true )){

                        String network = this.smarthome.giveDeviceNetwork( request.getData( "old_name" ));
                        dID = this.smarthome.giveDeviceIdByName( request.getData( "old_name") );
                        if( network != null && dID.length() > 0 ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.changeDeviceName(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    dID,
                                    request.getData( "old_name" ),
                                    request.getData( "new_name" ),
                                    netInfo[0]);
                        }
                    }
                    break;

                case ADD_LOCATION:

                    if ( !request.areSet("location", "address", "port"))
                        return;

                    if (!smarthome.addLocation( request.getData("location"), "" , request.getData("address"), Integer.parseInt(request.getData("port")), true ) ||
                        !this.restInterface.addLocation(
                                this.username,
                                "websocket_"+session.getId(),
                                request.getData( "location" ),
                                request.getData( "address" ),
                                Integer.parseInt( request.getData( "port" ) ))){

                        HashMap<String, Object> response = new HashMap<>();
                        response.put("type", "ERROR_LOCATION");
                        try {
                            session.getBasicRemote().sendText(gson.toJson(response));
                        }catch( IOException e){
                            e.printStackTrace();
                        }
                    }
                    break;

                case ADD_SUBLOCATION:

                    if ( !request.areSet("location", "sublocation" ))
                        return;

                    if (smarthome.addSubLocation(request.getData("location"), request.getData("sublocation"), "", true )){

                        String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
                        if( network != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.addSubLocation(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    request.getData( "location" ),
                                    request.getData( "sublocation" ),
                                    this.smarthome.giveNextSublocID( request.getData( "location" )),
                                    netInfo[0], Integer.parseInt( netInfo[1] ));
                        }
                    }
                    break;

                case REMOVE_LOCATION:

                    if ( !request.areSet( "location" ))
                        return;

                    if (smarthome.removeLocation(request.getData("location"), true )){

                        String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
                        if( network != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.removeLocation(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    request.getData( "location" ),
                                    this.smarthome.giveLocIdByName(request.getData( "location" )),
                                    netInfo[0]);
                        }
                    }
                    break;

                case REMOVE_SUBLOCATION:

                    if ( !request.areSet( "location", "sublocation" ))
                        return;

                    if (smarthome.removeSublocation(request.getData("location"), request.getData("sublocation"), true )){

                        String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
                        if( network != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.removeSubLocation(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    request.getData( "location" ),
                                    request.getData( "sublocation" ),
                                    this.smarthome.giveSubLocIdByName( request.getData( "location"), request.getData( "sublocation" )),
                                    netInfo[0], Integer.parseInt( netInfo[1] ));
                        }
                    }
                    break;

                case ADD_DEVICE:

                    if ( !request.areSet( "location", "sublocation", "name", "type"  ))
                        return;

                    //  TODO request to the db to obtain a new ID to be assigned to the device
                    if( smarthome.addDevice(request.getData("location"),
                                                                request.getData("sublocation"),
                                                                "",
                                                                request.getData("name"),
                                                                SmarthomeDevice.DeviceType.StringToType(request.getData("type")), true )){

                        String network = this.smarthome.giveLocationNetwork( request.getData( "location" ));
                        if( network != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.addDevice(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    request.getData( "name" ),
                                    request.getData( "location" ),
                                    request.getData( "sublocation" ),
                                    this.smarthome.giveSubLocIdByName(request.getData( "location" ), request.getData( "sublocation") ),
                                    SmarthomeDevice.DeviceType.StringToType(request.getData("type")),
                                    netInfo[0], Integer.parseInt( netInfo[1] ));
                        }
                    }
                    break;

                case CHANGE_DEVICE_SUBLOCATION:

                    if ( !request.areSet( "location", "sublocation", "name" ))
                        return;

                    if (smarthome.changeDeviceSubLocation(request.getData("location"), request.getData("sublocation"), request.getData("name"), true )) {

                        String network = this.smarthome.giveDeviceNetwork( request.getData( "name" ));
                        dID = this.smarthome.giveDeviceIdByName( request.getData( "name" ));
                        String subLoc = this.smarthome.giveDeviceSubLocation( request.getData( "name" ));

                        if( network != null && dID != null && subLoc != null ) {

                            String[] netInfo = network.split( ":" );
                            this.restInterface.changeDeviceSublocation(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    dID,
                                    request.getData( "name" ),
                                    request.getData( "location" ),
                                    subLoc,
                                    request.getData( "sublocation" ),
                                    this.smarthome.giveSubLocIdByName( request.getData( "location" ) , request.getData( "sublocation" )),
                                    netInfo[0], Integer.parseInt( netInfo[1] ));

                        }
                    }
                    break;

                case REMOVE_DEVICE:
                    if( !request.areSet( "name" ))
                        return;

                    if(smarthome.removeDevice(request.getData("name"), true )) {
                        logger.info( "REQUEST TESTED");
                        String network = this.smarthome.giveDeviceNetwork( request.getData( "name" ));
                        dID = this.smarthome.giveDeviceIdByName( request.getData( "name" ));
                        if( network != null && dID != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.removeDevice(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    dID,
                                    request.getData( "name" ),
                                    netInfo[0], Integer.parseInt( netInfo[1] ));
                        }
                    }
                    break;

                case STATISTIC:

                    if ( !request.areSet( "device_name", "statistic", "start", "stop" ))
                        return;

                    if (!smarthome.devicePresent(request.getData("device_name")))
                        return;

                    //  TODO Request to the server for statistics
                    WebRequest req = gson.fromJson(message, WebRequest.class);
                    HashMap<String, String> data2 = new HashMap<>();
                    System.out.println("STATISTIC: " + req.getData().get("statistic") +  " CONVERTION: " + this.convertStatistics( req.getData().get("statistic"), req.getData().get("device_name")));
                    System.out.println("START: " + gson2.fromJson( req.getData().get( "start" ).replace(":00:00.000Z", ""), Date.class).toString());
                    System.out.println("STOP: " + req.getData().get( "stop" ).replace(":00:00.000Z", ""));
                    data2.put("device_name", req.getData().get("device_name"));
                    data2.put("statistic", req.getData().get("statistic"));
                    data2.put("values", gson.toJson(db.getStatistics(
                            this.smarthome.giveDeviceIdByName(req.getData().get("device_name")),
                            this.convertStatistics(req.getData().get("statistic"), req.getData().get("device_name")),
                            gson2.fromJson( req.getData().get( "start" ).replace(":00:00.000Z", ""), Date.class),
                            gson2.fromJson( req.getData().get( "stop" ).replace(":00:00.000Z", ""), Date.class)
                    )));

                    WebRequest resp = new WebRequest(req.getStringType(), data2 );
                    try {
                        session.getBasicRemote().sendText(gson.toJson(resp));
                    }catch( IOException e){
                        e.printStackTrace();
                    }

                    break;

                case UPDATE:

                    if( !request.areSet( "device_name", "action", "value"))
                        return;

                    if( smarthome.performAction(request.getData("device_name"), request.getData( "action"), request.getData( "value"), null, true )) {

                        String network = this.smarthome.giveDeviceNetwork( request.getData( "device_name" ));
                        dID = this.smarthome.giveDeviceIdByName( request.getData( "device_name" ));
                        if( network != null && dID != null ) {
                            String[] netInfo = network.split( ":" );
                            this.restInterface.execCommand(
                                    this.username,
                                    "websocket_"+session.getId(),
                                    dID,
                                    request.getData( "action" ),
                                    request.getData( "value" ),
                                    netInfo[0], Integer.parseInt( netInfo[1] ));
                        }
                    }
                    break;

                case LOGOUT:
                    httpSession.removeAttribute("authData");
                    httpSession.removeAttribute("infoData");

                    try{
                        session.close();
                    }catch( Exception e ){
                        e.printStackTrace();
                    }
                    break;

                default:
                    logger.severe("Error, request unknown: " + request.requestType().toString());
            }

        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        logger.exiting("Session closed with id: %s%n", session.getId());
        if( updater != null)
            updater.close();
        updater = null;
    }

    //// UTILITY FUNCTIONS

    //  verifies the validity of the user information and if it is authorized to perform a request.
    //  the authentication is based on the replica of the same data, one is stored inside the user session
    //  and the other is given by the user with a cookie. If the code inside both the object is the same then
    //  the user is authorized to perform the request
    private boolean verification(){

        logger.info( "Starting verification of information" );
        HttpSession session = (HttpSession) this.config.getUserProperties().get("httpsession");
        String authCookie = (String) this.config.getUserProperties().get("cookie");

        //  verification of object presence
        if( session == null ){

            logger.severe( "Error missing the session information. Verification failed" );
            return false;

        }

        if( authCookie == null || authCookie.length() == 0 ){

            logger.severe( "Error missing the authentication cookie. Verification failed" );
            return false;

        }

        BasicData userData = (BasicData)session.getAttribute("authData");
        if( userData == null ) {

            logger.severe( "Error missing the user authentication information. Verification failed" );
            return false;

        }

        //  verification of authentication code
        if( userData.isValid( authCookie )){

            logger.info("User authorized to create a websocket: " + userData.getUser() );
            return true;

        }

        logger.warning( "Warning, user not authorized to open a websocket" );
        return false;

    }

    //  connector with the db, request the stored smarthome definition formatted has a SmarthomeManager to the database
    private void getSmarthome( String username ){

        //  try to get a copy from the user http session
        this.smarthome = (SmarthomeManager)((HttpSession) config.getUserProperties().get( "httpsession" )).getAttribute( "smarthome" );
        //  TODO to be changed with a request to the db to obtain the stored smarthome definition
        if( this.smarthome == null ) {
            this.smarthome = db.getSmarthome(username);// SmarthomeManager.createTestingEnvironment(username, true, configuration );
            if( this.smarthome == null )
                this.smarthome = new SmarthomeManager(username, true, configuration );
            ((HttpSession) config.getUserProperties().get( "httpsession" )).setAttribute( "smarthome", this.smarthome );
        }else
            this.smarthome.connect( this.configuration );

    }

    private String convertStatistics( String statistic, String device_name ){
        SmarthomeWebDevice device = this.smarthome.giveDevices().get(device_name);
        if( device != null ){
            switch(SmarthomeWebDevice.convertType( device.getType())){
                case LIGHT:
                    if(statistic.contains("Device"))
                        return "action.devices.traits.OnOff";
                    else
                        return "action.devices.traits.Brightness";
                case FAN:
                    if(statistic.contains("Device"))
                        return "action.devices.traits.OnOff";
                    else
                        return "action.devices.traits.FanSpeed";
                case THERMOSTAT:
                case CONDITIONER:
                    if(statistic.contains("Device"))
                        return "";
                    else
                        return "action.devices.traits.Temperature";
                case DOOR:
                    if(statistic.contains("Opening"))
                        return "";
                case UNKNOWN:
                default:

            }
        }
        return "";
    }

}
