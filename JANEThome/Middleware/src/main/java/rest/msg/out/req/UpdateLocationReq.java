package rest.msg.out.req;

import rest.msg.RESTMessage;

public class UpdateLocationReq extends RESTMessage {

    private String name;

    public UpdateLocationReq( String name ){
        this.name = name;
    }

    public UpdateLocationReq(){}

    public void setName( String name ){ this.name = name; }

    public String getName(){ return this.name; }

}
