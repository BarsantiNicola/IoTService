package iot;

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

    public Operation(String dID, String action, String value,Date date) {

        this.dID = dID;
        this.action = action;
        this.value = value;
        this.date = date;

    }

}
