package rest.out;

import rest.UpdateRequest;

import java.util.List;

public class UpdateMessage {

    private List<UpdateRequest> requests;
    private String user;

    public UpdateMessage(){}

    public void setUser( String user ){
        this.user = user;
    }

    public void setRequests( List<UpdateRequest> requests ){
        this.requests = requests;
    }

    public String getUser(){
        return this.user;
    }

    public List<UpdateRequest> getRequests(){
        return this.requests;
    }

}
