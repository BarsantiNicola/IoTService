package weblogic.login.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iot.SmarthomeDefinition;
import jms.DeviceUpdate;
import jms.beans.UpdateNotifier;
import statistics.Statistics;
import utils.configuration.EndpointConfigurator;
import utils.jms.WebUpdateReceiver;
import weblogic.login.beans.BasicData;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@ServerEndpoint(value="/controller", configurator = EndpointConfigurator.class)
public class WebappEndpoint {

    private Logger logger;
    private WebUpdateReceiver updater;
    private EndpointConfig config;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config){

        logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        this.config = config;

        if( verification(config)) {

            BasicData userData =(BasicData)((HttpSession)config.getUserProperties().get("httpsession")).getAttribute("authData");
            logger.info("User authorized to create a websocket: " + userData);
            //  TODO get home data(location/sublocation/devices)
            SmarthomeDefinition smarthome;
            InitialContext context = null;
            try{
                context = new InitialContext();
                smarthome = (SmarthomeDefinition) context.lookup("test_"+userData.getUser());

            } catch (NamingException e) {
                smarthome = SmarthomeDefinition.createTestingEnvironment(userData.getUser());
                if( context != null ) {
                    try {
                        context.bind("test_"+userData.getUser(),smarthome);
                    } catch (NamingException namingException) {
                        namingException.printStackTrace();
                    }
                }

            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            updater = new WebUpdateReceiver(userData.getUser(),session);
            try {
                session.getBasicRemote().sendText(gson.toJson(smarthome));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            logger.warning("User not authorized to open a websocket");
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @OnMessage
    public void onMessage(String message, Session session) {

        logger.info("Message received. Session id: "+ session.getId() +" Message: message" + " Protocol: ");
        WebRequest request = WebRequest.buildRequest(message);
        if( verification(config)) {
            HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpsession");
            BasicData userData = (BasicData) httpSession.getAttribute("authData");
            UpdateNotifier notifier = new UpdateNotifier();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
            switch (request.requestType()) {
                case RENAME_LOCATION:
                    //  TODO request to database
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case RENAME_SUBLOCATION:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case RENAME_DEVICE:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case ADD_LOCATION:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case ADD_SUBLOCATION:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case ADD_DEVICE:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case CHANGE_DEVICE_SUBLOCATION:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case REMOVE_LOCATION:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case REMOVE_SUBLOCATION:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case REMOVE_DEVICE:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                case STATISTIC:
                    if( true ){
                        WebRequest req = gson.fromJson(message,WebRequest.class);
                        HashMap<String,String> data = new HashMap<>();
                        data.put("device_name",req.getData().get("device_name"));
                        data.put("statistic",req.getData().get("statistic"));
                        data.put("values",gson.toJson(Statistics.buildTestEnvironment()));
                        WebRequest resp = new WebRequest(req.getStringType(), data);
                        notifier.sendMessage(gson.toJson(resp), userData.getUser());
                    }
                    break;
                case UPDATE:
                    if( true )
                        notifier.sendMessage(message, userData.getUser());
                    break;
                default:
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
        //updater.close();
        updater = null;
    }

    private boolean verification(EndpointConfig config){
        HttpSession session = (HttpSession)config.getUserProperties().get("httpsession");
        String authCookie = (String)config.getUserProperties().get("cookie");
        logger.severe("authcookie: "+authCookie);
        if( authCookie == null || authCookie.length() == 0 )
            return false;

        BasicData userData = (BasicData)session.getAttribute("authData");
        if( userData == null )
            return false;

        return userData.isValid(authCookie);

    }

    private void createSession(EndpointConfig config){
        //  TODO Request to database for smarthome description
    }


}
