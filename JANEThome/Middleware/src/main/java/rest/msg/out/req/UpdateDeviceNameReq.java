package rest.msg.out.req;

import rest.msg.RESTMessage;

public class UpdateDeviceNameReq extends RESTMessage {

    private final String name;

    public UpdateDeviceNameReq(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
