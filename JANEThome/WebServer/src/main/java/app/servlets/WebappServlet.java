package app.servlets;

//  internal services
import login.beans.AuthData;

//  http protocol management
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

//  exceptions
import java.io.IOException;

//  collections
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The servlet verifies the authorization of the user and in the case it forward the user to the correct page(webapp.jsp).
 * The servlet is not mandatory(the verification is performed every time a user request is sent to the service) however it
 * is important for the global user experience(instead of seeing a page which not respond to the user, he will see a redirection
 * to the login page so he can understand that a new login has to be performed
 */
@WebServlet(name="Webapp", urlPatterns={"/webapp"})
public class WebappServlet extends HttpServlet{

    public void service( HttpServletRequest req, HttpServletResponse resp ){

        Logger logger = LogManager.getLogger( getClass().getName() );
        logger.info( "Starting user authentication verification" );

        //  extraction of the eventual parameters from the request(cookies)
        HashMap<String,String> parameters = this.extractParameters( req );

        //  getting the authentication information from the session and from the cookies
        //  (from the cookies will be getted from the extractParameter function)
        AuthData userAuthData = (AuthData) req.getSession().getAttribute( "authData" );
        try{

            //  if authentication fails(no cookie, no data of user, no valid authentication token)
            if( !parameters.containsKey( "auth" ) || userAuthData == null || !userAuthData.isValid( parameters.get( "auth" ))){

                logger.info( "Invalid access to the webapp, starting information removal: "  +
                        !parameters.containsKey( "auth" ) + " : " + ( userAuthData == null ) + " : " );

                //  removing of eventual stored user authentication data stored into the session
                if ( userAuthData != null ) { //  removing of userData

                    logger.info( "Removing authentication data from the session" );
                    req.getSession().removeAttribute( "authData" );

                }

                //  removing of eventual cookies into the client browser
                if ( parameters.containsKey( "auth" )) {

                    logger.info( "Removing authentication data from the user's cookies" );
                    resp.addCookie(new Cookie( "auth", "" ));

                }

                //  redirection to the login page to force the user to perform the login
                logger.info( "Redirecting the user to the login page" );
                resp.sendRedirect( "login.jsp" );

            }else  //  if user correctly authenticated it is redirected to the webapp.jsp page
                resp.sendRedirect( "webapp.jsp" );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }


    ////////--  UTILITIES  --////////


    /**
     * Method to extract needed cookies from the requests
     * @param request {@link HttpServletRequest} the request given by the servlet
     * @return Returns a set of key value pairs containing the found keys and their values
     */
    private HashMap<String,String> extractParameters( HttpServletRequest request ){

        HashMap<String, String> result = new HashMap<>();

        if( request.getCookies() != null ) {    //  cookies can be deleted by the browser

            Optional<String> authtoken = Arrays.stream( request.getCookies() )
                    .filter( c -> "auth".equals( c.getName() ) )
                    .map( Cookie::getValue )
                    .findAny();

            if( authtoken.isPresent() && authtoken.get().length() > 0 )
                result.put( "auth", authtoken.get() );

        }
        return result;
    }
}
