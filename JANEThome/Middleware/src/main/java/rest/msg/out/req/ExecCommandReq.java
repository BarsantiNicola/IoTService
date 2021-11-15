package rest.msg.out.req;

import java.util.HashMap;

/**
 * Class implementing the internal requests of ExecCommandsReq.class
 */
public class ExecCommandReq{

    private int dev_id;
    private HashMap<String,Object> actions;

    public ExecCommandReq( int dev_id, HashMap<String,Object> actions ){
        this.dev_id = dev_id;
        this.actions = actions;
    }


    ////////--  SETTERS  --////////


    public void setDev_id( int dev_id ){
        this.dev_id = dev_id;
    }

    public void setActions( HashMap<String,Object> actions ){
        this.actions = actions;
    }


    ////////--  GETTERS  --////////


    public int getDev_id(){
        return this.dev_id;
    }

    public HashMap<String,Object> getActions(){
        return this.actions;
    }
}
