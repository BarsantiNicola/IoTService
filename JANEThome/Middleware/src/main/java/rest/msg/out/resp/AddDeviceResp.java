package rest.msg.out.resp;

import rest.msg.RESTMessage;

import java.util.HashMap;

/**
 * Class implementing the message body of the AddDevice response
 */
public class AddDeviceResp extends RESTMessage {

    private String dev_id;
    private HashMap<String,String> state;

    ////////--  SETTERS  --////////

    public void setDev_id( String dev_id ){ this.dev_id = dev_id; }

    public void setState( HashMap<String,String> state ){ this.state = state; }


    ////////--  GETTERS  --////////


    public String getDev_id(){
        return this.dev_id;
    }

    public HashMap<String,String> getState(){
        return this.state;
    }
}
