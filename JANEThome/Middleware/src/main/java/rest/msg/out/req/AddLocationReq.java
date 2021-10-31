package rest.msg.out.req;

import rest.msg.RESTMessage;

public class AddLocationReq extends RESTMessage {

    private String name;
    private String user;
    private int port;
    private String hostname;

    public AddLocationReq( String name, String user, int port, String hostname ){

        this.name = name;
        this.user = user;
        this.port = port;
        this.hostname = hostname;

    }

    public AddLocationReq(){}

    public void setName( String name ){
        this.name = name;
    }

    public void setUser( String user ){
        this.user = user;
    }

    public void setPort( int port ){
        this.port = port;
    }

    public void setHostname( String hostname ){
        this.hostname = hostname;
    }

    public String getName(){
        return this.name;
    }

    public String getUser(){
        return this.user;
    }

    public int getPort(){
        return this.port;
    }

    public String getHostname(){
        return this.hostname;
    }


}
