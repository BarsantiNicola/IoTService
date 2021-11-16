package db.model;

import org.bson.types.Decimal128;

import java.io.Serializable;
import java.util.Date;

/**
 * Class compatible with web Clients for the definition of statistics.
 * it is used as basic component for the generation of a Statistics class instance
  */
public class Statistic implements Serializable {

    private Date x;
    private String y;

    @SuppressWarnings( "unused" )
    public Statistic(){}

    public Statistic( Date x, String y ){

        this.x = x;
        this.y = y;

    }

    public void setX( Decimal128 x ){ this.x = new Date(x.longValue()); }

    public void setY( Double y ){ this.y = String.valueOf(y); }

    public Decimal128 getX(){ return new Decimal128(x.getTime()); }

    public Double getY(){ return Double.parseDouble(y); }
}
