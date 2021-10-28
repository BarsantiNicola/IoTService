package rest.msg.out.req;

import rest.msg.RESTMessage;

import java.util.List;

public class ExecCommandsReq extends RESTMessage {

    private final List<ExecCommandReq> requests;

    public ExecCommandsReq( List<ExecCommandReq> requests ){
        this.requests = requests;
    }

    public List<ExecCommandReq> getRequests(){
        return this.requests;
    }
}
