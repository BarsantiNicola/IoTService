package rest.msg.out.req;

import rest.msg.RESTMessage;

public class AddLocationReq extends RESTMessage {

    private final String name;
    private final String user;
    private final int port;
    private final String hostname;

    public AddLocationReq( String name, String user, int port, String hostname ){

        this.name = name;
        this.user = user;
        this.port = port;
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
