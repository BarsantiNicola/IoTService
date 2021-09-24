package rest.out.beans;

import javax.ws.rs.core.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings("unused")
public class RESTsender implements Callable<Response> {

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
    public Response call(){

        //  TODO ENABLE REST INTERFACE
       /*return ClientBuilder
                .newClient()
                .target( this.address + ":" + this.port )
                .path( path )
                .request( MediaType.APPLICATION_JSON )
                .post(Entity.entity( this.params, MediaType.APPLICATION_JSON ));*/

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
        //return true;

    }
}
