package rest.msg.in;

import rest.msg.RESTMessage;

public class StateResponse extends RESTMessage{

    private final String dID;
    private final int status;

    private StateResponse( String dID, int status ){
        this.dID = dID;
        this.status = status;
    }

    public static StateResponse buildSuccess( String dID ){
        return new StateResponse( dID, 200 );
    }

    public static StateResponse buildError( String dID ){
        return new StateResponse( dID, 400 );
    }

    public String getdID(){ return this.dID; }

    public int getStatus(){ return this.status; }
}
