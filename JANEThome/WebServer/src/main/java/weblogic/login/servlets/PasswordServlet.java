package weblogic.login.servlets;

import db.interfaces.DBinterface;
import utils.mail.interfaces.EmailServiceLocal;
import weblogic.login.beans.BasicData;
import weblogic.login.beans.UserData;
import weblogic.login.interfaces.BasicTokenRemote;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//////////////////////////////////////////////[ PasswordServlet ]////////////////////////////////////////////////////
//                                                                                                                 //
//   Servlet to manage the user password change. The change of the password is performed by two different actions  //
//   performed asynchronously, first of all the user will send a request to change the password, this will force   //
//   the server to send an email to the given mail containing a link to change the password. Clicking on the link  //
//   will start the second phase giving a form validated by a token inserted into the link that authorize the user //
//   to change his password                                                                                        //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet(name="PasswordRequest", urlPatterns={"/password"})
public class PasswordServlet extends HttpServlet {

    @EJB
    private EmailServiceLocal mailService;

    @EJB
    private DBinterface db;

    private Logger logger;

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        PASSWORD_REQ,
        PASSWORD_CHANGE,
        UNKNOWN
    }

    @SessionScoped
    public void service( HttpServletRequest req, HttpServletResponse resp ){

        this.initializeLogger();   //  initialization of the class logger

        //  extracting the parameters from the request
        HashMap<String, String> parameters = this.extractParameters( req );

        BasicTokenRemote data;

        try{

            InitialContext context = new InitialContext();

            switch( typeOfRequest( parameters )){

                case PASSWORD_REQ:     //  registration of a new account

                    if( !parameters.containsKey( "email" )){

                        logger.severe( "Error, email not present into the given parameters. Abort operation" );
                        resp.setStatus( 500 );
                        return;

                    }

                    logger.info( "Received request from [" + req.getRemoteAddr() + "] of type PASSWORD_REQ. Email: " + parameters.get( "email" ));
                    data = new UserData();
                    data.createToken( parameters.get( "email" ));

                    if( !this.verifyEmail( parameters.get( "email" ))){

                        logger.severe( "Error during the password change. Invalid email provided" );
                        resp.setStatus( 500 );  //  sending an error will change the registration form to notify it
                        return;

                    }

                    if( !db.emailPresent( parameters.get( "email" ))){

                        logger.warning( "Error during password change. Provided email not present" );
                        resp.setStatus( 500 ); //  sending an error will change the registration form to notify it
                        return;

                    }

                    //  getting the email data used to send back the email
                    Scanner s = new Scanner( this.getServletContext().getResourceAsStream( "/WEB-INF/password.html" )).useDelimiter( "\\A" );
                    String email = s.hasNext() ? s.next() : "";
                    s.close();

                    int count=0;
                    while( true )
                        try {

                            //  storing the data for the password change
                            context.bind( "ejb:module/password_" + data.getToken(), data );
                            this.logger.info( "Password change instance correctly stored" );
                            break;

                        }catch( NamingException e ){

                            count++;
                            logger.warning( "Token already present. Random recreation" );

                            if( count == 5 ){

                                logger.severe( "Unable to create a valid token. Abort operation" );
                                resp.setStatus( 500 );
                                return;

                            }
                            data.recreateToken();
                        }

                    //  sending to the provided email a redirection link for password change
                    if( mailService.sendMailPasswordChange( parameters.get( "email" ), email, data.getToken() ))
                        resp.setStatus( 200 );
                    else
                        resp.setStatus( 500 );
                    break;

                case PASSWORD_CHANGE:    //  redirection for the password change

                    if( !parameters.containsKey( "auth" ) || !parameters.containsKey( "password" )){

                        logger.severe( "Error missing parameters auth or password into the request. Abort operation" );
                        resp.setStatus( 500 );
                        return;

                    }

                    logger.info("Received request from ["+req.getRemoteAddr()+"] of type PASSWORD_CHANGE. Auth: " + parameters.get("auth"));
                    data = (BasicData) context.lookup("ejb:module/password_" + parameters.get("auth"));

                    if( db.changePassword( data.getUser(), parameters.get( "password" ))) {

                        this.logger.info( "User password correctly updated" );
                        resp.setStatus(200);

                    }else {

                        this.logger.severe( "An error has occurred during the password update into the database. Abort operation" );
                        resp.setStatus(500);

                    }
                    context.unbind("ejb:module/password_" + parameters.get("auth"));
                    break;

                default:
                    resp.setStatus( 500 );
            }

        } catch (NamingException e) {

            resp.setStatus( 500 );
            logger.warning("Expired password token. Abort" );

        }

    }

    //// UTILITY FUNCTIONS

    //  it identified the type of request basing on the available parameters
    //      Return:
    //          - UNKNOWN: unable to identify the type of request
    //          - PASSWORD_REQ: request to change the password
    //          - PASSWORD_CHANGE: message for creating the new password
    private PasswordServlet.RequestType typeOfRequest(HashMap<String,String> data){

        boolean passwordReq = data.containsKey( "email" );
        boolean passwordChange = data.containsKey( "password" ) && data.containsKey("auth");

        return passwordReq? passwordChange? PasswordServlet.RequestType.UNKNOWN : RequestType.PASSWORD_REQ : passwordChange? RequestType.PASSWORD_CHANGE : PasswordServlet.RequestType.UNKNOWN;
    }

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
    private HashMap<String,String> extractParameters(HttpServletRequest request){

        HashMap<String, String> result = new HashMap<>();
        Arrays.asList( "email", "password", "auth" ).forEach(
                (field)->{
                    String data = request.getParameter( field );
                    if( data!= null )
                        result.put( field, data );
                });
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

    //  verifies that the email is correctly defined
    private boolean verifyEmail( String email ){

        if( email == null  || email.length() == 0 )
            return false;

        int host_index = email.indexOf( '@' );
        if( host_index == -1 )
            return false;

        return email.substring(host_index).contains(".");

    }

}