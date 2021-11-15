package rest.msg.out.req;

import rest.msg.RESTMessage;

/**
 * Class implementing the message body of ChangeDeviceSubLocation REST request
 */
public class UpdateDeviceSubLocReq extends RESTMessage {

    private String subloc_id;

    public UpdateDeviceSubLocReq( String subloc_id ){
        this.subloc_id = subloc_id;
    }

    public UpdateDeviceSubLocReq(){}


    ////////--  SETTERS  --////////


    public void setSubloc_id( String subloc_id ){ this.subloc_id = subloc_id; }


    ////////--  GETTERS  --////////


    public String getSubloc_id(){
        return this.subloc_id;
    }
}
