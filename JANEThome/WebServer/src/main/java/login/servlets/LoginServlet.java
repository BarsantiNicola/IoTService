package login.servlets;

//  internal services
import db.interfaces.IDatabase;
import login.beans.AuthData;
import login.beans.UserData;

//  ejb3.0
import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

//  http protocol management
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
 * Business logic behind the login and autologin actions. The servlet requests manegement follows three stages:
 *    - if variables are present the request is a logic -> verification of credentials( MAIN TARGET )
 *    - if cookies are present the request is an autologin -> verification of cookies validity ( SECONDARY TARGET )
 *    - redirection to the login.jsp page ( DEFAULT TARGET )
 */
@WebServlet(name="Login", urlPatterns={"/login"})
public class LoginServlet extends HttpServlet {

    //  database manager instance
    @EJB
    IDatabase db;

    private enum RequestType{  //  requests handled by the servlet
        LOGIN_REQ,
        AUTOLOGIN_REQ,
        UNKNOWN
    }


    public void service( HttpServletRequest req, HttpServletResponse resp ){

        HttpSession session = req.getSession(true );
        if( session.isNew() )
            session.setMaxInactiveInterval( 3600 ); //  session will expire after 1h of inactivity*/
        Logger logger = LogManager.getLogger(getClass().getName());

        //  extraction of the eventual parameters from the request(cookies, form values)
        HashMap<String,String> parameters = this.extractParameters( req );

        switch( this.typeOfRequest( parameters )) {

            case LOGIN_REQ:  //  login request from the form [email and password parameters present]


                logger.info( "Received request from [" + req.getRemoteAddr() + "] of type LOGIN_REQ. Email: " + parameters.get( "email" ));

                if( db.login( parameters.get( "email" ), parameters.get( "password" )) ){

                    session.removeAttribute( "smarthome" );
                    //  creation of credential cookies to enable auto login and authorize each request from now on
                    logger.info( "Login succeded, email: " + parameters.get( "email" ));

                    //  used by the web page to show the user name into the page
                    UserData infoData = new UserData();
                    String[] names = db.getUserFirstAndLastName(parameters.get( "email" ));
                    infoData.setParameters( names[0], names[1] );
                    req.getSession().setAttribute( "infoData", infoData );

                    //  used for authorizing the requests, authtoken provided by the user and stored into the server must be equal
                    AuthData userData = new AuthData();
                    userData.createToken( parameters.get( "email" ));
                    resp.addCookie( new Cookie( "auth", userData.getToken()));  //  we put the authorization into the user cookie
                    req.getSession().setAttribute( "authData", userData );  //  we put the authorization into the server for comparison

                    logger.info("Session for user " + parameters.get( "email" ) + " correctly deployed" );
                    resp.setStatus( 200 );  //  the receiving of ok status will perform the redirection to the webapp page

                }else
                    resp.setStatus( 500 ); //  the receiving of error status will be notified to the user from the login form

                break;

            case AUTOLOGIN_REQ: //  autologin request[auth cookie present]

                logger.info( "Received request from [" + req.getRemoteAddr() + "] of type AUTOLOGIN_REQ. Auth: " + parameters.get( "auth" ));

                //  getting the authentication information from the session and from the cookies
                AuthData userData = (AuthData) req.getSession().getAttribute( "authData" );

                if( userData == null || !userData.isValid( parameters.get( "auth" ))) {

                    if( userData != null ) {
                        logger.info( "Removing invalid authData information from the session" );
                        req.getSession().removeAttribute( "authData" );
                    }

                    logger.info( "Removing invalid authData information from the session" );
                    req.getSession().removeAttribute( "authData" );

                    //  cookie always present( or we will not be here )
                    logger.info("Removing invalid cookie: " + parameters.get("auth"));
                    //  the only way to remove a cookie is to substitute with an empty string
                    resp.addCookie(new Cookie("auth", ""));

                    try {

                        resp.sendRedirect("login.jsp");

                    }catch( IOException e ){

                        e.printStackTrace();

                    }

                } else {

                    //  at each request the cookie will be updated to a new one
                    logger.info("Valid cookie, updating cookie");
                    userData.recreateToken();  //  generation of new token
                    resp.addCookie( new Cookie( "auth", userData.getToken() ));  //  changing cookie
                    req.removeAttribute( "authData" );  //  changing session data
                    req.getSession().setAttribute( "authData", userData );

                    try{

                        resp.sendRedirect("webapp");

                    }catch( IOException e ){

                        e.printStackTrace();

                    }

                }
                break;

            default:
                try {
                    resp.sendRedirect("login.jsp");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }


    ////////--  UTILITIES  --////////


    /**
     * Method to identify the type of request analyzing the given data:
     * @param data the set of information obtained by the cookies and parameters fields
     * @return Returns the type of request
     *    - LOGIN_REQ: {@link RequestType} the email and password field given by the form are present( MAIN TARGET )
     *    - AUTOLOGIN_REQ: {@link RequestType} the auth cookie is present( SECONDARY TARGET )
     *    - UNKNOWN: {@link RequestType} unable to identify the type of request
     */
    private LoginServlet.RequestType typeOfRequest( HashMap<String,String> data ){

        boolean loginReq = data.containsKey( "email" ) && data.containsKey("password" );
        boolean autologin = data.containsKey( "auth" );

        return loginReq?
                RequestType.LOGIN_REQ :
                autologin?
                        RequestType.AUTOLOGIN_REQ :
                        RequestType.UNKNOWN;

    }

    /**
     * Method to extract parameters and cookies from the requests
     * @param request {@link HttpServletRequest} the request given by the servlet
     * @return Returns a set of key value pairs containing the found keys and their values
     */
    private HashMap<String,String> extractParameters( HttpServletRequest request ){

        //  searching the "email","password" keys inside the request parameters
        HashMap<String, String> result = new HashMap<>();
        Arrays.asList( "email", "password" ).forEach(
                (field)->{
                    String data = request.getParameter( field );
                    if( data!= null )
                        result.put( field, data );
                });

        //  searching the "auth" key inside the cookies
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