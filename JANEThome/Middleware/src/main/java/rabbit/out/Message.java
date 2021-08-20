package rabbit.out;

import com.google.gson.Gson;
import java.io.Serializable;

//  Class developed as a basic container for messages. It can be used to generate many
//  different messages that can be sent on the rabbitMQ message broker
@SuppressWarnings("unused")
public abstract class Message implements Serializable {

    private final String destination;                    //  destination ID for a message
    protected final static Gson converter = new Gson();  //  used to traslate the message into a json string

    public Message( String destination ){

        this.destination = destination;

    }

    public String getDestination(){
        return destination;
    }

    @Override
    public String toString(){
        return converter.toJson(this );
    }

}
