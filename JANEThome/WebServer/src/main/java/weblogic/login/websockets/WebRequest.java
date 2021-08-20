package weblogic.login.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        LOGOUT,
        UNKNOWN
    }

    WebRequest( String type, HashMap<String,String> data){
        this.type = type;
        this.data = data;
    }

    private String type;
    private HashMap<String,String> data;

    public UpdateType requestType(){
        List<String> values = Arrays.asList("RENAME_LOCATION", "RENAME_SUBLOCATION", "RENAME_DEVICE","ADD_LOCATION","ADD_SUBLOCATION","ADD_DEVICE","CHANGE_SUBLOC","REMOVE_LOCATION","REMOVE_SUBLOCATION","REMOVE_DEVICE","STATISTIC","UPDATE","LOGOUT");
        int index =  values.indexOf(type);
        return index == -1? UpdateType.values()[values.size()]:UpdateType.values()[index];
    }

    public String getStringType(){
        return type;
    }

    public HashMap<String,String> getData(){
        return data;
    }

    public String getData(String key){
        return this.data.get( key );
    }

    public static WebRequest buildRequest(String data){

        Logger logger = Logger.getLogger(WebRequest.class.getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create();

        return gson.fromJson( data, WebRequest.class );
    }

    public boolean areSet( String...keys ){
        for( String key: keys )
            if( !this.data.containsKey( key ))
                return false;
        return true;

    }

    public String getBadResponse(){

        Gson gson = new Gson();
        String app = this.type;
        this.type = this.type+":ERROR";
        String response = gson.toJson(this);
        this.type = app;
        return response;

    }
}
