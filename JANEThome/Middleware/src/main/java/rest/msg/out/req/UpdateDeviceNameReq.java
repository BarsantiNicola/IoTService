package rest.msg.out.req;

import rest.msg.RESTMessage;

public class UpdateDeviceNameReq extends RESTMessage {

    private String name;

    public UpdateDeviceNameReq(String name){
        this.name = name;
    }

    public UpdateDeviceNameReq(){}

    public void setName( String name ){ this.name = name; }

    public String getName(){
        return this.name;
    }
}
