package weblogic.login.servlets;

import weblogic.login.beans.BasicData;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

///////////////////////////////////////////////[ WebappServlet ]/////////////////////////////////////////////////////
//                                                                                                                 //
//   Description:  business logic behind user webapp page. The servlet will verifies that the user is authorized   //
//                 to open the page and in the case it will forward his request to the webapp.jsp facelet          //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet(name="Webapp", urlPatterns={"/webapp"})
public class WebappServlet extends HttpServlet{

    private Logger logger;

    public void service( HttpServletRequest req, HttpServletResponse resp ){

        this.initializeLogger();
        logger.info( "Starting verification of information" );

        HashMap<String,String> parameters = this.extractParameters( req );

        //  getting the authentication information from the session and from the cookies
        //  (from the cookies will be getted from the extractParameter function)
        BasicData userData = (BasicData) req.getSession().getAttribute( "authData" );
        try{
            if( !parameters.containsKey( "auth" ) || userData == null || !userData.isValid( parameters.get( "auth" ))){
                logger.info( "Invalid access to the webapp, starting information removal" );

                if (userData != null) {

                    logger.info( "Removing authentication data from the session" );
                    req.getSession().removeAttribute( "authData" );

                }

                if ( parameters.containsKey( "auth" )) {

                    logger.info( "Removing authentication data from the user's cookies" );
                    resp.addCookie(new Cookie( "auth", "" ));

                }
                logger.info( "Redirecting the user to the login page" );
                resp.sendRedirect( "login.jsp" );

            }else
                resp.sendRedirect( "webapp.jsp" );

        }catch( IOException e ){
            e.printStackTrace();
        }
    }

    //// UTILITY FUNCTIONS

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

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
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
