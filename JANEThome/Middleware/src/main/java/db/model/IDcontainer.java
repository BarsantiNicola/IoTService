package db.model;

public class IDcontainer {

    private int value;
    private String type;

    @SuppressWarnings( "unused" )
    public IDcontainer(){}

    public IDcontainer(String type, int value){
        this.type = type;
        this.value = value;
    }

    public void setValue( int value ){ this.value = value; }

    public void setType( String type ){ this.type = type; }

    public int getValue(){ return this.value; }

    public String getType(){ return this.type; }

    public void incrementValue(){
        value++;
    }
}
