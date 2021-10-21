package rest.msg.in;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rest.msg.RESTMessage;

import java.util.Date;
import java.util.HashMap;

public class UpdateRequest extends RESTMessage {

    private String dev_id;
    private String timestamp;
    private String user;
    private HashMap<String,String> actions;

    public UpdateRequest(){}

    public void setDev_id( String dID ){
        this.dev_id= dID;
    }

    public void setTimestamp( String timestamp ){
        this.timestamp = timestamp;
    }

    public void setUser( String user ){ this.user = user; }

    public void setActions( HashMap<String,String> actions ){
        this.actions = actions;
    }

    public String getDev_id(){
        return this.dev_id;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public String getUser(){ return this.user; }

    public HashMap<String,String> getActions(){ return this.actions; }

    public Date giveConvertedTimestamp(){

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" ).create();
        return gson.fromJson( this.timestamp, Date.class );

    }



}
