package rest.msg.out.req;

import rest.msg.RESTMessage;

public class AddSubLocationReq extends RESTMessage {

    private String name;

    public AddSubLocationReq( String name ){
        this.name = name;
    }

    public AddSubLocationReq(){}

    public void setName( String name ){ this.name = name; }

    public String getName(){
        return this.name;
    }
}
