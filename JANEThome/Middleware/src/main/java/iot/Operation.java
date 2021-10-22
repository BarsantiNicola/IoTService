package iot;

import java.io.Serializable;
import java.util.Date;

public class Operation implements Serializable {
    public String action;
    public String value;
    public Date date;

    public Operation(String action, String value,Date date) {
        this.action = action;
        this.value = value;
        this.date = date;
    }

    public Operation() {
    }
}
