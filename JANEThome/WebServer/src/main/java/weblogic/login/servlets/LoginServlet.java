package weblogic.login.servlets;

import db.interfaces.DBinterface;
import weblogic.login.beans.BasicData;
import weblogic.login.beans.UserLogin;
import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


////////////////////////////////////////////////[ LoginServlet ]/////////////////////////////////////////////////////
//                                                                                                                 //
//   Description:  business logic behind user login and auto login. The servlets requests follow three             //
//                 stages for their management:                                                                    //
//                 -- if cookies are present the request is an autologin -> it verifies cookies validity           //
//                 -- if variables are present the request is a login -> it verifies user/password validity        //
//                 -- otherwise the request will be redirected to the login.jsp page                               //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet(name="Login", urlPatterns={"/login"})
public class LoginServlet extends HttpServlet {

    @EJB
    DBinterface db;

    private Logger logger;

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        LOGIN_REQ,
        AUTOLOGIN_REQ,
        UNKNOWN
    }

    public void service( HttpServletRequest req, HttpServletResponse resp ){

        this.initializeLogger();

        HashMap<String,String> parameters = this.extractParameters( req );

        switch( typeOfRequest( parameters )) {

            case LOGIN_REQ:  //  login request from the login form

                if( !parameters.containsKey( "email" ) || !parameters.containsKey( "password" )){

                    logger.severe( "Missing email or password during login. Abort operation" );
                    resp.setStatus( 500 );
                    return;

                }

                logger.info( "Received request from [" + req.getRemoteAddr() + "] of type LOGIN_REQ. Email: " + parameters.get( "email" ));

                if( db.login( parameters.get( "email" ), parameters.get( "password" ))){

                    //  creation of credential cookies to enable auto login and authorize each request from now on
                    logger.info( "Login succeded, email: " + parameters.get( "email" ));

                    //  used by the web page to show the user name into the page
                    UserLogin infoData = new UserLogin();
                    infoData.setParameters( db.getUserFirstName(parameters.get( "email" )),db.getUserLastName( parameters.get( "email" )));
                    req.getSession().setAttribute( "infoData", infoData );

                    //  used for authorizing the requests, authtoken provided by the user and stored into the server must be equal
                    BasicData userData = new BasicData();
                    userData.createToken( parameters.get( "email" ));
                    resp.addCookie(new Cookie( "auth", userData.getToken() ));  //  we put the authorization into the user cookie
                    req.getSession().setAttribute( "authData", userData );  //  we put the authorization into the server for comparison

                    logger.info("Session for user " + parameters.get( "email" ) + " correctly deployed" );
                    resp.setStatus( 200 );  //  the reciving of ok status will perform the redirection to the webapp page

                }else
                    resp.setStatus( 500 ); //  the reciving of error status will be notified to the user from the login form

                break;

            case AUTOLOGIN_REQ: //  the request contains the data generated from a previous login, can perform an autologin

                logger.info( "Received request from [" + req.getRemoteAddr() + "] of type AUTOLOGIN_REQ. Auth: " + parameters.get( "auth" ));

                //  getting the authentication information from the session and from the cookies
                //  (from the cookies will be getted from the extractParameter function)
                BasicData userData = (BasicData) req.getSession().getAttribute( "authData" );
                if (userData == null || !userData.isValid(parameters.get("auth"))) {

                    if( userData != null ) {
                        logger.info( "Removing invalid authData information from the session" );
                        req.getSession().removeAttribute( "authData" );
                    }

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

    //// UTILITY FUNCTIONS

    //  it identified the type of request basing on the available parameters
    //      Return:
    //          - UNKNOWN: request for the login.jsp page
    //          - LOGIN_REQ: login by the login.jsp form
    //          - AUTOLOGIN_REQ: login by session authentication
    private LoginServlet.RequestType typeOfRequest( HashMap<String,String> data ){

        boolean loginReq = data.containsKey( "email" ) && data.containsKey("password" );
        boolean autologin = data.containsKey( "auth" );

        return autologin? LoginServlet.RequestType.AUTOLOGIN_REQ : loginReq?  RequestType.LOGIN_REQ : RequestType.UNKNOWN;
    }

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
    private HashMap<String,String> extractParameters( HttpServletRequest request ){

        HashMap<String, String> result = new HashMap<>();
        Arrays.asList( "email", "password" ).forEach(
                (field)->{
                    String data = request.getParameter( field );
                    if( data!= null )
                        result.put( field, data );
                });

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

    //  initialization of the logger that prevent that more handlers are allocated
    private void initializeLogger(){

        this.logger = Logger.getLogger( getClass().getName() );

        //  verification of the number of instantiated handlers
        if( this.logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter( new SimpleFormatter() );
            this.logger.addHandler( consoleHandler );

        }
    }

}