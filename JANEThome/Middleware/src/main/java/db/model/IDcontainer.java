package db.model;

/**
 * Class developed as a container for dID/lID extraction
 */
public class IDcontainer {

    private int value;
    private String type;

    @SuppressWarnings( "unused" )
    public IDcontainer(){}

    public IDcontainer(String type, int value){
        this.type = type;
        this.value = value;
    }


    ////////--  SETTERS  --////////


    public void setValue( int value ){ this.value = value; }

    public void setType( String type ){ this.type = type; }


    ////////--  GETTERS  --////////


    public int getValue(){ return this.value; }

    public String getType(){ return this.type; }


    ////////--  UTILITIES  --////////


    public void incrementValue(){
        value++;
    }
}
