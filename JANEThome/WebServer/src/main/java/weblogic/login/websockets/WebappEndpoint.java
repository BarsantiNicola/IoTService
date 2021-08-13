package weblogic.login.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import iot.SmarthomeManager;
import iot.SmarthomeDevice;
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
            SmarthomeManager smarthome;
            InitialContext context = null;
            try{

                context = new InitialContext();
                smarthome = (SmarthomeManager) context.lookup("smarthome_"+userData.getUser());

            } catch (NamingException e) {

                smarthome = SmarthomeManager.createTestingEnvironment(userData.getUser());

                if( context != null ) {
                    try {
                        context.bind("smarthome_"+userData.getUser(),smarthome);
                    } catch (NamingException namingException) {
                        logger.info("Error during the save of the smarthome");
                        namingException.printStackTrace();
                    }
                }

            }

            updater = new WebUpdateReceiver(userData.getUser(),session);
            try {
                session.getBasicRemote().sendText(smarthome.buildSmarthomeDefinition().trim());
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

            SmarthomeManager smarthome;
            InitialContext context = null;
            try {

                context = new InitialContext();
                smarthome = (SmarthomeManager) context.lookup("smarthome_" + userData.getUser());

            } catch (NamingException e) {
                logger.info("Error during the generation of the smarthome");
                smarthome = SmarthomeManager.createTestingEnvironment(userData.getUser());  //  TODO TO BE CHANGED WITH REQUEST TO DB
                if (context != null) {
                    try {
                        context.bind("smarthome_" + userData.getUser(), smarthome);
                    } catch (NamingException namingException) {
                        namingException.printStackTrace();
                    }
                }

            }

            if (smarthome == null) {
                httpSession.removeAttribute("authData");
                httpSession.removeAttribute("infoData");
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            HashMap<String, String> jsonMessage = request.getData();
            if (jsonMessage != null && jsonMessage.containsKey("data"))
                jsonMessage = gson.fromJson(jsonMessage.get("data"), new TypeToken<HashMap<String, String>>(){}.getType());

            switch (request.requestType()) {
                case RENAME_LOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("old_name") || !jsonMessage.containsKey("new_name"))
                        return;

                    if (smarthome.changeLocationName(jsonMessage.get("old_name"), jsonMessage.get("new_name"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                session.getBasicRemote().sendText(request.getBadResponse());
                            } catch (IOException ee) {
                                ee.printStackTrace();
                            }
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case RENAME_SUBLOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("location") ||
                            !jsonMessage.containsKey("old_name") || !jsonMessage.containsKey("new_name"))
                        return;

                    if (smarthome.changeSublocationName(jsonMessage.get("location"), jsonMessage.get("old_name"), jsonMessage.get("new_name"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case RENAME_DEVICE:
                    if (jsonMessage == null || !jsonMessage.containsKey("old_name") || !jsonMessage.containsKey("new_name"))
                        return;

                    if (smarthome.changeDeviceName(jsonMessage.get("old_name"), jsonMessage.get("new_name"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                session.getBasicRemote().sendText(request.getBadResponse());
                            } catch (IOException ee) {
                                ee.printStackTrace();
                            }
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case ADD_LOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("location") || !jsonMessage.containsKey("address") ||
                            !jsonMessage.containsKey("port"))
                        return;

                    if (smarthome.addLocation(jsonMessage.get("location"), jsonMessage.get("address"), Integer.parseInt(jsonMessage.get("port")))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                session.getBasicRemote().sendText(request.getBadResponse());
                            } catch (IOException ee) {
                                ee.printStackTrace();
                            }
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case ADD_SUBLOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("location") || !jsonMessage.containsKey("sublocation"))
                        return;
                    if (smarthome.addSubLocation(jsonMessage.get("location"), jsonMessage.get("sublocation"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                session.getBasicRemote().sendText(request.getBadResponse());
                            } catch (IOException ee) {
                                ee.printStackTrace();
                            }
                        }
                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case REMOVE_LOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("location"))
                        return;

                    if (smarthome.removeLocation(jsonMessage.get("location"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                session.getBasicRemote().sendText(request.getBadResponse());
                            } catch (IOException ee) {
                                ee.printStackTrace();
                            }
                        }
                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case REMOVE_SUBLOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("location") || !jsonMessage.containsKey("sublocation"))
                        return;
                    if (smarthome.removeSublocation(jsonMessage.get("location"), jsonMessage.get("sublocation"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                session.getBasicRemote().sendText(request.getBadResponse());
                            } catch (IOException ee) {
                                ee.printStackTrace();
                            }
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case ADD_DEVICE:
                    if (jsonMessage == null || !jsonMessage.containsKey("location") || !jsonMessage.containsKey("sublocation") ||
                            !jsonMessage.containsKey("name") || !jsonMessage.containsKey("type"))
                        return;

                    //  TODO Request to db for a new DID
                    String dID = "placeholder";
                    if (smarthome.addDevice(jsonMessage.get("location"), jsonMessage.get("sublocation"), dID, jsonMessage.get("name"), SmarthomeDevice.DeviceType.valueOf(jsonMessage.get("type").toUpperCase()))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)

                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case CHANGE_DEVICE_SUBLOCATION:
                    if (jsonMessage == null || !jsonMessage.containsKey("name") || !jsonMessage.containsKey("location") ||
                            !jsonMessage.containsKey("sublocation"))
                        return;

                    if (smarthome.changeDeviceSubLocation(jsonMessage.get("location"), jsonMessage.get("sublocation"), jsonMessage.get("name"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case REMOVE_DEVICE:
                    if (jsonMessage == null || !jsonMessage.containsKey("name"))
                        return;
                    if (smarthome.removeDevice(jsonMessage.get("name"))) {
                        //  TODO Request to db or just update the db with rabbitMQ(no reply)
                        try {
                            session.getBasicRemote().sendText(gson.toJson(request));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else
                        try {
                            session.getBasicRemote().sendText(request.getBadResponse());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case STATISTIC:
                    if (jsonMessage == null || !jsonMessage.containsKey("device_name") || !jsonMessage.containsKey("statistic") ||
                            !jsonMessage.containsKey("start") || !jsonMessage.containsKey("stop"))
                        return;

                    if (!smarthome.devicePresent(jsonMessage.get("device_name")))
                        return;

                    //  TODO Request to the server for statistics
                    WebRequest req = gson.fromJson(message, WebRequest.class);
                    HashMap<String, String> data = new HashMap<>();
                    data.put("device_name", req.getData().get("device_name"));
                    data.put("statistic", req.getData().get("statistic"));
                    data.put("values", gson.toJson(Statistics.buildTestEnvironment()));
                    WebRequest resp = new WebRequest(req.getStringType(), data);
                    notifier.sendMessage(gson.toJson(resp), userData.getUser());
                    break;

                case UPDATE:

                    if (jsonMessage == null || !jsonMessage.containsKey("device_name") || !jsonMessage.containsKey("action") ||
                        !jsonMessage.containsKey("value"))
                        return;

                    notifier.sendMessage(message, userData.getUser());
                    return;

                case LOGOUT:
                    httpSession.removeAttribute("authData");
                    httpSession.removeAttribute("infoData");
                    break;

                default:
                    logger.severe("Error, request unknown: " + request.requestType().toString());
            }
            try {
                if( context != null )
                    context.rebind("smarthome_" + userData.getUser(), smarthome);

            } catch (NamingException e) {
                e.printStackTrace();
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
