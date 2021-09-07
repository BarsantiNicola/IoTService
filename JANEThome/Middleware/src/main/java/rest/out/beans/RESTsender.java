package rest.out.beans;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class RESTsender implements Callable<Boolean> {

    private final String address;
    private final int port;
    private final HashMap<String,String> params;
    private final String path;

    RESTsender(String address, int port, String path, HashMap<String,String> params ){

        this.address = address;
        this.port = port;
        this.params = params;
        this.path = path;

    }

    @Override
    public Boolean call(){

       /* return ClientBuilder
                .newClient()
                .target( this.address + ":" + this.port )
                .path( path )
                .request( MediaType.APPLICATION_JSON )
                .post(Entity.entity( this.params, MediaType.APPLICATION_JSON ))
                .getStatus() == 200;*/
        return true;

    }
}
