package weblogic.login.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/////////////////////////////////////////////////[ WebRequest ]//////////////////////////////////////////////////////
//                                                                                                                 //
//   Class designed to contain a single request from a webpage, it is implemented for giving to the developer the  //
//   possibility to work on the data given by the webpage in a simple and easy way                                 //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    WebRequest( String type, HashMap<String,String> data){

        this.type = type;
        this.data = data;

    }

    //  converts the stringed type given by the webpage into an enumerator
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

    ////  GETTERS

    public String getStringType(){
        return type;
    }

    public HashMap<String,String> getData(){
        return data;
    }

    public String getData( String key ){
        return this.data.get( key );
    }


    ////  PUBLIC FUNCTIONS

    //  converts the message received from the webclient into a WebRequest instance
    public static WebRequest buildRequest( String data ){

        //  set the date format is mandatory to convert the date given by javascript
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).create();

        return gson.fromJson( data, WebRequest.class );
    }

    //  used to quickly verifies the content of a message
    public boolean areSet( String...keys ){
        for( String key: keys )
            if( !this.data.containsKey( key ))
                return false;
        return true;

    }
}
