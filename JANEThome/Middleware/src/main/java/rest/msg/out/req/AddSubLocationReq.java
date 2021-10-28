package rest.msg.out.req;

import rest.msg.RESTMessage;

public class AddSubLocationReq extends RESTMessage {

    private final String name;

    public AddSubLocationReq( String name ){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
