package rest.msg.in;

import rest.msg.RESTMessage;

public class StateResponse extends RESTMessage{

    private final int dID;
    private final int status;

    private StateResponse( int dID, int status ){
        this.dID = dID;
        this.status = status;
    }

    public static StateResponse buildSuccess( String dID ){
        return new StateResponse( Integer.parseInt(dID), 200 );
    }

    public static StateResponse buildError( String dID ){
        return new StateResponse( Integer.parseInt(dID), 400 );
    }

    public int getdID(){ return this.dID; }

    public int getStatus(){ return this.status; }
}
