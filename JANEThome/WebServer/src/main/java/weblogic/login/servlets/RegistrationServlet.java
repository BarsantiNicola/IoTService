package weblogic.login.servlets;

import db.interfaces.DBinterface;
import iot.SmarthomeManager;
import iot.User;
import utils.mail.interfaces.EmailServiceLocal;
import weblogic.login.beans.UserData;
import weblogic.login.interfaces.RegistrationInterface;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


////////////////////////////////////////////[ RegistrationServlet ]//////////////////////////////////////////////////
//                                                                                                                 //
//   Servlet to manage user account registration requests. After the receipt of a request it allocates a timed     //
//   stateful EJB into the system to maintain a random string and all the registration information.                //
//   It then send an email to the user containing a pre-formatted URL to give back the random string. If           //
//   the two random strings coincide then the email is valid and the user will be registered info the system       //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@WebServlet(name="Registration", urlPatterns={"/registration"})
public class RegistrationServlet extends HttpServlet {

    @EJB
    private EmailServiceLocal mailService;  // STATELESS EJB FOR EMAIL MANAGEMENT

    @EJB
    private DBinterface db;

    private Logger logger;

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        REGISTRATION,
        EMAIL_CONFIRM,
        UNKNOWN
    }

    //  handles the account registration requests
    //     - create a new account
    //     - confirm an account
    //     - show the registration page
    @SessionScoped
    public void service( HttpServletRequest req, HttpServletResponse resp ) throws IOException {

        this.initializeLogger();  //  initialization of the class logger

        //  extracting the parameters from the request
        HashMap<String, String> parameters = this.extractParameters( req );

        RegistrationInterface data;

        try{

            InitialContext context = new InitialContext();

            switch( typeOfRequest( parameters )){

                case REGISTRATION:     //  registration of a new account

                    logger.info("Received request from [" + req.getRemoteAddr() + "] of type REGISTRATION. Email: " + parameters.get( "email" ));
                    data = new UserData();

                    if( !this.verifyEmail( parameters.get( "email" ))){

                        logger.severe( "Error during registration. Invalid email provided" );
                        resp.setStatus( 500 );  //  sending an error will change the registration form to notify it
                        return;

                    }

                    if( db.emailPresent( parameters.get("email"))){

                        logger.warning( "Error during registration. The provided email is already present" );
                        resp.setStatus( 500 ); //  sending an error will change the registration form to notify it
                        return;

                    }
                    data.setInformations( parameters.get( "name"), parameters.get( "surname" ), parameters.get( "email" ), parameters.get( "password" ));

                    //  getting the email data used to send back the email
                    Scanner s = new Scanner( this.getServletContext().getResourceAsStream( "/WEB-INF/registration.html" )).useDelimiter( "\\A" );
                    String email = s.hasNext() ? s.next() : "";
                    s.close();

                    int count=0;

                    //  storing of the registration information, when the user will confirm its registration by the email link
                    //  it will resend its associated token which will be used to retrieve its personal information and make the registration
                    while( true )

                        try{

                            //  storing the data for registration confirmation
                            context.bind( "ejb:module/registration_" + data.getToken(), data );
                            break;

                        }catch( NamingException e ){

                            //  the token is randomly generated, another instance can be already binded
                            count++;
                            logger.warning( "Token already present. Random recreation" );
                            if( count == 5 ){

                                logger.severe( "Unable to create a valid token. Abort operation" );
                                resp.setStatus( 500 ); //  sending an error will change the registration form to notify it
                                return;

                            }
                            data.recreateToken();

                        }

                    //  sending the email for password confirmation
                    if( mailService.sendMailLoginConfirm( parameters.get("email"), email, data.getToken() ))
                        resp.setStatus( 200 ); //  sending status ok will change the registration form to notify it
                    else
                        resp.setStatus( 500 ); //  sending an error will change the registration form to notify it
                    break;

                case EMAIL_CONFIRM:    //  email confirmation of a new account

                    logger.info( "Received request from [" + req.getRemoteAddr() + "] of type EMAIL_CONFIRM. Token: " + parameters.get( "token" ));
                    //  retrieving the stored information during the registration phase
                    try {

                        data = (UserData) context.lookup( "ejb:module/registration_" + parameters.get( "token" ));
                        if( data != null )
                            req.getSession().setAttribute( "userData", data );

                        else{

                            logger.info( "The required information are not present. Abort operation" );
                            resp.sendRedirect("registration.jsp?state=3");
                            return;

                        }

                    }catch( NamingException e ){

                        logger.info( "The required information are not present. Abort operation" );
                        resp.sendRedirect( "registration.jsp?state=3" );
                        return;

                    }

                    context.unbind("ejb:module/registration_" + parameters.get("token"));
                    SmarthomeManager manager = new SmarthomeManager(data.getEmail(), false, null);
                    db.addManager(manager);
                    User user = new User( data.getEmail(), data.getFirstName(), data.getLastName(), data.getEmail(), data.getPassword());
                    user.setHomeManager(manager);
                    if( db.addUser( user ))
                        resp.sendRedirect( "registration.jsp?state=2" );
                    else{
                        db.deleteManager(manager.getKey());
                        resp.sendRedirect("registration.jsp?state=3");
                    }

                    break;

                default:  //  requesting the registration page
                    resp.sendRedirect( "registration.jsp" );
            }

        } catch (NamingException e) {

            resp.sendRedirect("registration.jsp?state=3");
            logger.warning("Expired registration token. Abort" );

        }

    }

    ////  UTILITY FUNCTIONS

    //  it identified the type of request basing on the available parameters
    //      Return:
    //          - UNKNOWN: unable to identify the type of request
    //          - REGISTRATION: request to register a new account
    //          - EMAIL_CONFIRM: verification of the email by a token
    private RequestType typeOfRequest(HashMap<String,String> data){

        boolean registration = data.containsKey( "name" ) && data.containsKey( "surname" ) &&
                data.containsKey( "email" ) && data.containsKey( "password" );
        boolean confirmation = data.containsKey( "token" );

        return registration? confirmation? RequestType.UNKNOWN : RequestType.REGISTRATION : confirmation?RequestType.EMAIL_CONFIRM : RequestType.UNKNOWN;
    }

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
    private HashMap<String,String> extractParameters( HttpServletRequest request ){

        HashMap<String, String> result = new HashMap<>();
        Arrays.asList("name", "surname", "email", "password", "token").forEach(
                (field)->{
                    String data = request.getParameter( field );
                    if( data != null )
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