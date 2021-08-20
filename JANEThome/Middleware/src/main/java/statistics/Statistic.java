package statistics;

import java.io.Serializable;
import java.util.Date;

public class Statistic implements Serializable {

    private final Date x;
    private final int y;

    public Statistic(Date x, int y ){
        this.x = x;
        this.y = y;
    }

    public Date getX(){ return x; }

    public int getY(){ return y; }
}
