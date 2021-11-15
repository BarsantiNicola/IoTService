package rest.msg.out.req;

import rest.msg.RESTMessage;

import java.util.List;

/**
 * Class implementing the body for the ExecCommand REST request
 */
public class ExecCommandsReq extends RESTMessage {

    private List<ExecCommandReq> requests;

    public ExecCommandsReq( List<ExecCommandReq> requests ){
        this.requests = requests;
    }


    ////////--  SETTERS  --////////


    public void setRequests( List<ExecCommandReq> requests ){ this.requests = requests; }


    ////////--  GETTERS  --////////


    public List<ExecCommandReq> getRequests(){
        return this.requests;
    }
}
