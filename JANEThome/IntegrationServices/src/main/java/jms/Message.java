package jms;

import com.google.gson.Gson;

import java.io.Serializable;

@SuppressWarnings("unused")
public abstract class Message implements Serializable {

    private final String destination;
    final static Gson converter = new Gson();

    Message(String destination){

        this.destination = destination;

    }

    String getDestination(){
        return destination;
    }

    @Override
    public String toString(){
        return converter.toJson(this );
    }

}
