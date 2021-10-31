package rest.out.beans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import rest.msg.RESTMessage;
import rest.msg.out.req.ExecCommandsReq;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings("all")
public class RESTsender implements Callable<Response> {

    private final String address;
    private final int port;
    private final RESTMessage request;
    private final String path;
    private final REQ_TYPE reqType;

    public enum REQ_TYPE{
        GET,
        PUT,
        POST,
        PATCH,
        DELETE
    }

    RESTsender(String address, int port, String path, REQ_TYPE reqType, RESTMessage request){

        this.address = address;
        this.port = port;
        this.request = request;
        this.path = path;
        this.reqType = reqType;

    }

    @Override
    public Response call(){

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println( "REST REQUEST:\naddr: " + address + " port: " + port + " path: " + path + "\nRequest:\n" + gson.toJson(request));
        switch( this.reqType ){

            case PUT:
                Response response = ClientBuilder
                        .newClient()
                        .target( "http://" + this.address + ":" + this.port )
                        .path(  path )
                        .request( MediaType.APPLICATION_JSON )
                        .put(Entity.entity( request, MediaType.APPLICATION_JSON ));

                System.out.println("OKOKOK " + response);
                System.out.println("Status: " + response.getStatus());
                return response;

            case POST:
                return ClientBuilder
                        .newClient()
                        .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                        .target( "http://" + this.address + ":" + this.port  )
                        .path( path )
                        .request( MediaType.APPLICATION_JSON )
                        .post(Entity.entity( request, MediaType.APPLICATION_JSON ));
            case DELETE:
                return ClientBuilder
                        .newClient()
                        .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                        .target( "http://" + this.address + ":" + this.port  )
                        .path( path )
                        .request( MediaType.APPLICATION_JSON )
                        .delete();
            case PATCH:
                System.out.println("GSON FORMAT: " + gson.toJson( ((ExecCommandsReq)request).getRequests()));
                return ClientBuilder
                        .newClient()
                        .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                        .target( "http://" + this.address + ":" + this.port )
                        .path( path )
                        .request( MediaType.APPLICATION_JSON )
                        .build("PATCH", Entity.entity( ((ExecCommandsReq)request).getRequests(), MediaType.APPLICATION_JSON ))
                        .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                        .invoke();
        }

        System.out.println("ERROR");

        return new Response() {

            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public StatusType getStatusInfo() {
                return null;
            }

            @Override
            public Object getEntity() {
                return null;
            }

            @Override
            public <T> T readEntity(Class<T> aClass) {
                return null;
            }

            @Override
            public <T> T readEntity(GenericType<T> genericType) {
                return null;
            }

            @Override
            public <T> T readEntity(Class<T> aClass, Annotation[] annotations) {
                return null;
            }

            @Override
            public <T> T readEntity(GenericType<T> genericType, Annotation[] annotations) {
                return null;
            }

            @Override
            public boolean hasEntity() {
                return false;
            }

            @Override
            public boolean bufferEntity() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public MediaType getMediaType() {
                return null;
            }

            @Override
            public Locale getLanguage() {
                return null;
            }

            @Override
            public int getLength() {
                return 0;
            }

            @Override
            public Set<String> getAllowedMethods() {
                return null;
            }

            @Override
            public Map<String, NewCookie> getCookies() {
                return null;
            }

            @Override
            public EntityTag getEntityTag() {
                return null;
            }

            @Override
            public Date getDate() {
                return null;
            }

            @Override
            public Date getLastModified() {
                return null;
            }

            @Override
            public URI getLocation() {
                return null;
            }

            @Override
            public Set<Link> getLinks() {
                return null;
            }

            @Override
            public boolean hasLink(String s) {
                return false;
            }

            @Override
            public Link getLink(String s) {
                return null;
            }

            @Override
            public Link.Builder getLinkBuilder(String s) {
                return null;
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getStringHeaders() {
                return null;
            }

            @Override
            public String getHeaderString(String s) {
                return null;
            }
        };

    }
}
