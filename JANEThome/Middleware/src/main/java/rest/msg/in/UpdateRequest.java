package rest.msg.in;

//  internal services
import rest.msg.RESTMessage;

//  utils
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;

//  collections
import java.util.HashMap;

/**
 * Class implementing the message received by the REST server
 */
public class UpdateRequest extends RESTMessage {

    private String dev_id;
    private String timestamp;
    private String user;
    private HashMap<String,Object> actions;

    public UpdateRequest(){}


    ////////--  SETTERS  --////////


    public void setDev_id( String dID ){
        this.dev_id= dID;
    }

    public void setTimestamp( String timestamp ){
        this.timestamp = timestamp;
    }

    public void setUser( String user ){ this.user = user; }

    public void setActions( HashMap<String,Object> actions ){
        this.actions = actions;
    }


    ////////--  GETTERS  --////////


    public String getDev_id(){
        return this.dev_id;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public String getUser(){ return this.user; }

    public HashMap<String,Object> getActions(){ return this.actions; }


    ////////--  UTILITIES --////////


    /**
     *  Method to obtain a converted timestamp following the smarthome date format
     * @return {@link Date} Returns the converted timestamp
     * @throws ClassCastException In case of not valid date format
     */
    public Date giveConvertedTimestamp() throws ClassCastException{

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        Gson gson2 = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH").create();
        Gson gson3 = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        if(!this.timestamp.contains("\""))
            this.timestamp = "\"" + this.timestamp + "\"";

        try {

            return gson.fromJson( this.timestamp, Date.class );

        }catch( Exception e ){

            try{

                return gson2.fromJson( this.timestamp, Date.class );

            }catch( Exception e2 ){

                try {
                    return gson3.fromJson(this.timestamp, Date.class);

                }catch( Exception e3 ){

                    e3.printStackTrace();
                    return null;

                }

            }

        }


    }



}
