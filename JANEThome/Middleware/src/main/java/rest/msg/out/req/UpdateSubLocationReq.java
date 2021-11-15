package rest.msg.out.req;

import rest.msg.RESTMessage;

/**
 * Class implementing the message body of ChangeSubLocationName REST request
 */
public class UpdateSubLocationReq extends RESTMessage {

    private String name;

    public UpdateSubLocationReq(String name){
        this.name = name;
    }

    public UpdateSubLocationReq(){}


    ////////--  SETTERS  --////////


    public void setName( String name ){ this.name = name; }


    ////////--  GETTERS  --////////


    public String getName(){
        return this.name;
    }

}
