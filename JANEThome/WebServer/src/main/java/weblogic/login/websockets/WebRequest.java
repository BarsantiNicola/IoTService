package weblogic.login.websockets;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WebRequest implements Serializable {

    enum UpdateType{
        RENAME_LOCATION,
        RENAME_SUBLOCATION,
        RENAME_DEVICE,
        ADD_LOCATION,
        ADD_SUBLOCATION,
        ADD_DEVICE,
        CHANGE_DEVICE_SUBLOCATION,
        REMOVE_LOCATION,
        REMOVE_SUBLOCATION,
        REMOVE_DEVICE,
        STATISTIC,
        UPDATE,
        UNKNOWN
    }

    WebRequest( String type, HashMap<String,String> data){
        this.type = type;
        this.data = data;
    }

    private String type;
    private HashMap<String,String> data;

    public UpdateType requestType(){
        List<String> values = Arrays.asList("RENAME_LOCATION", "RENAME_SUBLOCATION", "RENAME_DEVICE","ADD_LOCATION","ADD_SUBLOCATION","ADD_DEVICE","CHANGE_SUBLOC","REMOVE_LOCATION","REMOVE_SUBLOC","REMOVE_DEVICE","STATISTIC","UPDATE");
        int index =  values.indexOf(type);
        return index == -1? UpdateType.values()[values.size()]:UpdateType.values()[index];
    }

    public String getStringType(){
        return type;
    }

    public HashMap<String,String> getData(){
        return data;
    }

    public static WebRequest buildRequest(String data){
        Logger logger = Logger.getLogger(WebRequest.class.getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        Gson gson = new Gson();
        logger.info("statistic_request:" + data);
        return gson.fromJson(data.substring(data.indexOf("{")), WebRequest.class);
    }
}
