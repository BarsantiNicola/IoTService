package app.sockets;//  Servlet information forwarding
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

//  Collections
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to forward service information to websockets
 */
public class EndpointConfigurator extends ServerEndpointConfig.Configurator {

    /**
     * Override of the websocket/servlet handshake to include cookies and httpSession into the webSocket
     *
     * @param config {@link ServerEndpointConfig} Configuration of the server
     * @param request {@link HandshakeRequest} Information linked with the request forwarded by the servlet to the websocket
     * @param response {@link HandshakeResponse} Information linked with the responde forwarded by the servlet from the websocket
     */
    @Override
    public void modifyHandshake( ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response ){

        Map<String,List<String>> headers = request.getHeaders();

        if( headers.containsKey( "cookie" )){
            HashMap<String, String> cookies = this.getCookies( headers.get( "cookie" ));  //  cookies extraction
            if ( cookies.containsKey( "auth" ))
                config.getUserProperties().put( "cookie", cookies.get( "auth" ));
        }
        config.getUserProperties().put("httpsession", request.getHttpSession());

    }

    /**
     * Method to extract the needed cookies information from the Cookie header
     *
     * @param cookies {@link HashMap<>} Configuration of the server
     * @return {@link HashMap<>} return the set of available cookies
     */
    private HashMap<String,String> getCookies( List<String> cookies ){

        HashMap<String,String> cookiesArchive = new HashMap<>();
        cookies.forEach( s -> Arrays.asList(
                s.replaceAll( " " , "" )  //  clean of cookies
                        .split( ";" ))       //  splitting of the cookies
                        .forEach( t->{

                            String[] values = t.split( "=" );  //  split cookie into a key-value pair
                            cookiesArchive.put(
                                values[0].replaceAll( "\"","" ),  //  clean and insertion of the key
                                values[1].replaceAll( "\"","" )); //  clean and insertion of the value

                        })
        );

        return cookiesArchive;
    }
}