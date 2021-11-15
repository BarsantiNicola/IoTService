package rest.in;

//  Jersey REST management
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


/**
 * Class designed as basing class of Jersey REST service. Defines the relative path used by the REST server
 */
@ApplicationPath("/deviceUpdate")
public class RESTApplication extends Application {

}