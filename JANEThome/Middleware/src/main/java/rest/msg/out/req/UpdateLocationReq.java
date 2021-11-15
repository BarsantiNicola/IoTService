package rest.msg.out.req;

import rest.msg.RESTMessage;

/**
 * Class implementing the message body of ChangeLocationName REST request
 */
public class UpdateLocationReq extends RESTMessage {

    private String name;

    public UpdateLocationReq( String name ){
        this.name = name;
    }


    ////////--  SETTERS  --////////


    public void setName( String name ){ this.name = name; }


    ////////--  GETTERS  --////////


    public String getName(){ return this.name; }

}
