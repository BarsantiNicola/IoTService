package rabbit.msg;

//  utils
import com.google.gson.Gson;
import java.io.Serializable;

/**
 * Basic class for developing rabbitMQ messages
 */
public abstract class Message implements Serializable {

    private String destination;                    //  destination ID for a message

    //  source unique ID of the sender(useful for GoogleHome part for double update prevention: sending an update
    //  i receive the same update, if from == receiver instance name -> discard message)
    private String from;
    protected final static Gson converter = new Gson();  //  used to translate the message into a json string

    public Message(){}

    public Message( String destination, String from ) throws InvalidMessageException{

        //  verification of mandatory fields
        if( destination == null || destination.length() == 0 )
            throw new InvalidMessageException();

        if( !destination.contains("@"))
            throw new InvalidMessageException();

        if( from == null || from.length() == 0 )
            throw new InvalidMessageException();

        this.destination = destination;
        this.from = from;

    }


    ////////--  SETTERS  --////////


    public void setDestination( String destination ){ this.destination = destination; }

    public void setFrom( String from ){ this.from = from; }


    ////////--  GETTERS  --////////


    public String getDestination(){
        return this.destination;
    }

    public String getFrom(){
        return this.from;
    }


    ////////--  UTILITIES  --////////


    @Override
    public String toString(){

        return converter.toJson(this );

    }

}
