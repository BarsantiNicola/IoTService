package rest.msg.out.req;

import rest.msg.RESTMessage;

public class UpdateSubLocationReq extends RESTMessage {

    private String name;

    public UpdateSubLocationReq(String name){
        this.name = name;
    }

    public UpdateSubLocationReq(){}

    public void setName( String name ){ this.name = name; }

    public String getName(){
        return this.name;
    }

}
