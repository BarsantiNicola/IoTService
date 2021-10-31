package rest.msg.out.req;

import rest.msg.RESTMessage;

public class UpdateDeviceSubLocReq extends RESTMessage {

    private String subloc_id;

    public UpdateDeviceSubLocReq( String subloc_id ){
        this.subloc_id = subloc_id;
    }

    public UpdateDeviceSubLocReq(){}

    public void setSubloc_id( String subloc_id ){ this.subloc_id = subloc_id; }

    public String getSubloc_id(){
        return this.subloc_id;
    }
}
