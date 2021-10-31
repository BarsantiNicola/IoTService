package rest.msg.out.resp;

import rest.msg.RESTMessage;

import java.util.HashMap;

public class AddDeviceResp extends RESTMessage {

    private String dev_id;
    private HashMap<String,String> state;

    public AddDeviceResp( String dev_id, HashMap<String,String> state){
        this.dev_id = dev_id;
        this.state = state;
    }

    public AddDeviceResp(){}

    public void setDev_id( String dev_id ){ this.dev_id = dev_id; }

    public void setState( HashMap<String,String> state ){ this.state = state; }

    public String getDev_id(){
        return this.dev_id;
    }

    public HashMap<String,String> getState(){
        return this.state;
    }
}
