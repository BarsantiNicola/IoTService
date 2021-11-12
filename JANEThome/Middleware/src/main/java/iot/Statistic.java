package iot;

import java.io.Serializable;
import java.util.Date;

//  class compatible with web Clients for the definition of statistics
//  it is used as basic component for the generation of a Statistics class instance
public class Statistic implements Serializable {

    private Date x;
    private String y;

    public Statistic(Date x, String y ){
        this.x = x;
        this.y = y;
    }

    public Statistic() {
    }

    public Date getX(){ return x; }

    public String getY(){ return y; }
}
