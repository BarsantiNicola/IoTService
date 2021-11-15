package rest.msg.out.req;

import rest.msg.RESTMessage;

/**
 * Class implementing the message body for the AddSubLocation REST request
 */
public class AddSubLocationReq extends RESTMessage {

    private String name;

    public AddSubLocationReq( String name ){
        this.name = name;
    }


    ////////--  SETTERS  --////////


    public void setName( String name ){ this.name = name; }


    ////////--  GETTERS  --////////


    public String getName(){
        return this.name;
    }
}
