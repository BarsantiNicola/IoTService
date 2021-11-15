package rest.msg.out.req;

import rest.msg.RESTMessage;

/**
 * Class implementing the message body of ChangeDeviceName REST request
 */
public class UpdateDeviceNameReq extends RESTMessage {

    private String name;

    public UpdateDeviceNameReq(String name){
        this.name = name;
    }


    ////////--  SETTERS  --////////


    public void setName( String name ){ this.name = name; }


    ////////--  GETTERS  --////////


    public String getName(){
        return this.name;
    }
}
