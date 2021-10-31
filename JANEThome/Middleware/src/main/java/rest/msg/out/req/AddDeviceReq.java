package rest.msg.out.req;

import rest.msg.RESTMessage;

public class AddDeviceReq extends RESTMessage {

    private int subloc_id;
    private String name;
    private String type;
    private String hostname;


    public AddDeviceReq( int subloc_id, String name, String type, String hostname ){
        this.subloc_id = subloc_id;
        this.name = name;
        this.type = type;
        this.hostname = hostname;
    }

    public AddDeviceReq(){}

    public void setSubloc_id( int subloc_id ){
        this.subloc_id = subloc_id;
    }

    public void setName( String name ){
        this.name = name;
    }

    public void setType( String type ){
        this.type = type;
    }

    public void setHostname( String hostname ){
        this.hostname = hostname;
    }

    public int getSubloc_id(){ return this.subloc_id; }

    public String getName(){ return this.name; }

    public String getType(){ return this.type; }

    public String getHostname(){ return this.hostname; }

}
