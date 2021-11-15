package iot;

import java.io.Serializable;
import java.util.Date;

/**
 * Class compatible with web Clients for the definition of statistics.
 * it is used as basic component for the generation of a Statistics class instance
  */
public class Statistic implements Serializable {

    private final Date x;
    private final String y;

    public Statistic( Date x, String y ){

        this.x = x;
        this.y = y;

    }

    public Date getX(){ return x; }

    public String getY(){ return y; }
}
