package rest.msg.out.req;

import rest.msg.RESTMessage;

public class UpdateLocationReq extends RESTMessage {

    private final String name;

    public UpdateLocationReq( String name ){
        this.name = name;
    }

    public String getName(){ return this.name; }

}
