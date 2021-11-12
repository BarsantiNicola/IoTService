package app.sockets;

//  utilities
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;

//  collections
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Class designed to contain a single request/response given/sent to the webClient
 */
public class WebRequest implements Serializable {

    //  update type for easy switch management
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

    private final String type; //  type of request
    private final HashMap<String,String> data;  // message fields

    public WebRequest(String type, HashMap<String, String> data){

        this.type = type;
        this.data = data;

    }

    /**
     * Converts the stringed type given by the webpage into an enumerator
      */
    public UpdateType requestType(){

        List<String> values = Arrays.asList(

                "RENAME_LOCATION",
                "RENAME_SUBLOCATION",
                "RENAME_DEVICE",
                "ADD_LOCATION",
                "ADD_SUBLOCATION",
                "ADD_DEVICE",
                "CHANGE_SUBLOC",
                "REMOVE_LOCATION",
                "REMOVE_SUBLOCATION",
                "REMOVE_DEVICE",
                "STATISTIC",
                "UPDATE",
                "LOGOUT"

        );

        int index = values.indexOf( type );
        return index == -1? UpdateType.values()[ values.size() ] : UpdateType.values()[ index ];

    }

    /**
     * Returns the type of the request without any conversion
     * @return a string containing the type of the request
     */
    public String getStringType(){
        return type;
    }


    /**
     * Returns the data stored into the class
     * @return An hashmap describing all the data stored into the class
     */
    public HashMap<String,String> getData(){
        return data;
    }

    /**
     * Returns a particular value stored into the data
     * @return A string if the value is present otherwise null
     */
    public String getData( String key ){
        return this.data.get( key );
    }


    /**
     * converts the message received from the webclient into a WebRequest instance
     * @return {@link WebRequest} The data given from the client converted into an object
     */
    public static WebRequest buildRequest( String data ){

        //  set the date format is mandatory to convert the date given by javascript
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create();

        return gson.fromJson( data, WebRequest.class );
    }

    /**
     * Verifies that the given fields are present into the data received
     * @return true if all the keys are present
     */
    public boolean areSet( String...keys ){
        for( String key: keys )
            if( !this.data.containsKey( key ))
                return false;
        return true;

    }
}
