package rest.msg.out.req;

import rest.msg.RESTMessage;

/**
 * Class implementing the message body of ChangeDeviceSubLocation REST request
 */
public class UpdateDeviceSubLocReq extends RESTMessage {

    private int subloc_id;

    public UpdateDeviceSubLocReq( int subloc_id ){
        this.subloc_id = subloc_id;
    }

    public UpdateDeviceSubLocReq(){}


    ////////--  SETTERS  --////////


    public void setSubloc_id( int subloc_id ){ this.subloc_id = subloc_id; }


    ////////--  GETTERS  --////////


    public int getSubloc_id(){
        return this.subloc_id;
    }
}
