package rest.msg.out.req;

import rest.msg.RESTMessage;

public class AddDeviceReq extends RESTMessage {

    private final int subloc_id;
    private final String name;
    private final String type;
    private final String hostname;

    public AddDeviceReq( int subloc_id, String name, String type, String hostname ){
        this.subloc_id = subloc_id;
        this.name = name;
        this.type = type;
        this.hostname = hostname;
    }

    public int getSubloc_id(){ return this.subloc_id; }

    public String getName(){ return this.name; }

    public String getType(){ return this.type; }

    public String getHostname(){ return this.hostname; }

}
