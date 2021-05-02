package utils.configuration;

import javax.servlet.http.Cookie;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndpointConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response){
        Map<String,List<String>> headers = request.getHeaders();
        HashMap<String,String> cookies = getCookies(headers.get("cookie"));
        config.getUserProperties().put("cookie", cookies.get("auth"));
        config.getUserProperties().put("httpsession",request.getHttpSession());
    }

    private HashMap<String,String> getCookies(List<String> cookies){
        HashMap<String,String> cookiesArchive = new HashMap<>();
        cookies.forEach( s ->
            Arrays.asList( s.replaceAll(" " , "").split(";")).forEach( t->{
                String[] values = t.split("=");
                cookiesArchive.put(
                        values[0].replaceAll("\"",""),
                        values[1].replaceAll("\"",""));
                    })
        );

        return cookiesArchive;
    }
}
