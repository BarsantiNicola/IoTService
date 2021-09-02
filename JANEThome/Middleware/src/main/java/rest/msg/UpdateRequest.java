package rest.msg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class UpdateRequest{

    private String dID;
    private String action;
    private String value;
    private String timestamp;

    public void setdID( String dID ){
        this.dID = dID;
    }

    public void setAction( String action ){
        this.action = action;
    }

    public void setValue( String value ){
        this.value = value;
    }

    public void setTimestamp( String timestamp ){
        this.timestamp = timestamp;
    }

    public Date giveConvertedTimestamp(){

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" ).create();
        return gson.fromJson( this.timestamp, Date.class );

    }

    public String getdID(){
        return this.dID;
    }

    public String getAction(){
        return this.action;
    }

    public String getValue(){
        return this.value;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

}
