package rest;

import java.util.Date;

public class UpdateRequest{

    private String dID;
    private String action;
    private String value;
    private Date   timestamp;

    public void setdID( String dID ){
        this.dID = dID;
    }

    public void setAction( String action ){
        this.action = action;
    }

    public void setValue( String value ){
        this.value = value;
    }

    public void setTimestamp( Date timestamp ){
        this.timestamp = timestamp;
    }

    public String getdID(){
        return this.dID;
    }

    public String getAction(){
        return this.action;
    }

    public String getValue(){
        return this.value;
    }

    public Date getTimestamp(){
        return this.timestamp;
    }

}
