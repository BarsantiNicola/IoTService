package db.model;

//  utils
import java.io.Serializable;
import java.util.Date;

/**
 * Data structure for statistic storage
 */
public class Operation implements Serializable {

    public String dID;
    public String action;
    public String value;
    public Date date;

    @SuppressWarnings( "unused" )
    public Operation(){}

    public Operation(String dID, String action, String value,Date date) {

        this.dID = dID;
        this.action = action;
        this.value = value;
        this.date = date;

    }


    ////////--  SETTERS  --////////

    public void setdID( String dID ){ this.dID = dID; }

    public void setAction( String action ){ this.action = action; }

    public void setValue( String value ){ this.value = value; }

    public void setDate( Date date ){ this.date = date; }


    ////////--  GETTERS  --////////


    public String getdID(){ return this.dID; }

    public String getAction(){ return this.action; }

    public String getValue(){ return this.value; }

    public Date getDate(){ return this.date; }

}
