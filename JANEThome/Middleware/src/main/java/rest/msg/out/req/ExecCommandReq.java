package rest.msg.out.req;

import rest.msg.RESTMessage;

import java.util.HashMap;

public class ExecCommandReq{

    private final String dev_id;
    private final HashMap<String,String> params;

    public ExecCommandReq( String dev_id, HashMap<String,String> params ){
        this.dev_id = dev_id;
        this.params = params;
    }

    public String getDev_id(){
        return this.dev_id;
    }

    public HashMap<String,String> getParams(){
        return this.params;
    }
}
