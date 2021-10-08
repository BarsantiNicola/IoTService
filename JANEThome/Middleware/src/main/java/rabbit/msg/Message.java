package rabbit.msg;

import com.google.gson.Gson;
import java.io.Serializable;

//  Class developed as a basic container for messages. It can be used to generate many
//  different messages that can be sent on the rabbitMQ message broker
public abstract class Message implements Serializable {

    private String destination;                    //  destination ID for a message
    private String from;                           //  source unique ID of the sender(usefull for google chrome for preventing double update)
    protected final static Gson converter = new Gson();  //  used to traslate the message into a json string

    public Message(){}

    public Message( String destination, String from ) throws InvalidMessageException{

        if( destination == null || destination.length() == 0 )
            throw new InvalidMessageException();

        if( !destination.contains("@"))
            throw new InvalidMessageException();

        if( from == null || from.length() == 0 )
            throw new InvalidMessageException();

        this.destination = destination;
        this.from = from;
    }

    public void setDestination( String destination ){ this.destination = destination; }

    public void setFrom( String from ){ this.from = from; }

    public String getDestination(){
        return this.destination;
    }

    public String getFrom(){
        return this.from;
    }
    @Override
    public String toString(){
        return converter.toJson(this );
    }

}
