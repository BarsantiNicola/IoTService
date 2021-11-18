package login.servlets;

//  internal services
import db.interfaces.IDatabase;
import login.mail.interfaces.IEmailService;
import login.beans.AuthData;
import login.beans.TempData;
import login.interfaces.IAuthStorage;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//  ejb3.0
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;

//  exceptions
import javax.naming.NamingException;

//  http protocol management
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//  collections
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


/**
 *  Servlet to manage the user password change. The change of the password is performed by two different asynchronous actions.
 *  First of all the user will send a request to change the password, this will force
 *  the server to send an email to the given mail containing a link to change the password. Clicking on the link
 *  will start the second phase giving a form authenticated by a token that authorize the user to change its password
 *  The servlet requests manegement follows two stages:
 *    - if variable "email" is present the request is the first phase of the password change
 *    - if variables "auth" and "password" are present the request is the second phase of password change
 */
@WebServlet(name="PasswordRequest", urlPatterns={"/password"})
public class PasswordServlet extends HttpServlet {

    //  instance of mail service
    @EJB
    private IEmailService mailService;

    //  instance of database manager
    @EJB
    private IDatabase db;

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        PASSWORD_REQ,
        PASSWORD_CHANGE,
        UNKNOWN
    }

    @SessionScoped
    public void service( HttpServletRequest req, HttpServletResponse resp ){

        Logger logger = LogManager.getLogger(getClass().getName());

        //  extracting the parameters from the request
        HashMap<String, String> parameters = this.extractParameters( req );

        IAuthStorage data;

        try{

            InitialContext context = new InitialContext();

            switch( typeOfRequest( parameters )){

                case PASSWORD_REQ:     //  initial request to change the password[ email parameter present ]

                    String email = parameters.get( "email" );
                    logger.info( "Received request from [" + req.getRemoteAddr() + "] of type PASSWORD_REQ. Email: " + email );
                    System.out.println("Request for email ");

                    if( !this.verifyEmail( email )){

                        logger.info( "Error during the password change. Invalid email provided" );
                        resp.setStatus( 500 );  //  sending an error will change the registration form to notify it
                        return;

                    }
                    System.out.println("Email verified ");
                    if( !db.emailPresent( email )){

                        logger.info( "Error during password change. Provided email not present" );
                        resp.setStatus( 500 ); //  sending an error will change the registration form to notify it
                        return;

                    }
                    System.out.println("Email present ");
                    //  generation of temporary data node maintaining the user email
                    data = new TempData();
                    data.createToken( email );
                    System.out.println("Token generated");
                    //  getting the email data used to send back the email
                    Scanner s = new Scanner( this.getServletContext().getResourceAsStream( "/WEB-INF/password.html" )).useDelimiter( "\\A" );
                    String emailContent = s.hasNext() ? s.next() : "";
                    s.close();
                    System.out.println("data generation ");
                    for( int count = 0; count<6; count++ )
                        try {

                            //  storing the data for the password change
                            context.bind( "ejb:module/password_" + data.getToken(), data );
                            logger.info( "Password change instance correctly stored" );
                            System.out.println("data send " + parameters.get("email"));
                            //  sending to the provided email a redirection link for password change
                            if( mailService.sendMailPasswordChange( parameters.get( "email" ), emailContent, data.getToken()))
                                resp.setStatus( 200 );
                            else
                                resp.setStatus( 500 );
                            return;

                        }catch( NamingException e ){

                            logger.info( "Token already present. Random recreation" );
                            data.recreateToken();

                        }

                    logger.info( "Unable to create a valid token. Abort operation" );
                    resp.setStatus( 500 );
                    break;

                case PASSWORD_CHANGE:    //  redirection for the password change[auth and password parameters present]

                    logger.info( "Received request from [" + req.getRemoteAddr() + "] of type PASSWORD_CHANGE. Auth: " + parameters.get( "auth" ));
                    data = (AuthData)context.lookup( "ejb:module/password_" + parameters.get( "auth" ));

                    if( db.changePassword( data.getUser(), parameters.get( "password" ))) {

                        logger.info( "User password correctly updated" );
                        resp.setStatus( 200 );

                    }else {

                        logger.error( "An error has occurred during the password update into the database. Abort operation" );
                        resp.setStatus( 500 );

                    }
                    context.unbind( "ejb:module/password_" + parameters.get( "auth" ));
                    break;

                default:
                    resp.setStatus( 500 );
            }

        }catch( NamingException e ) {

            resp.setStatus( 500 );
            logger.error( "Expired password token. Abort" );

        }

    }


    ////////--  UTILITIES  --////////


    /**
     * Method to identify the type of request analyzing the given data:
     * @param data the set of information obtained by the cookies and parameters fields
     * @return Returns the type of request
     *    - PASSWORD_REQ: {@link PasswordServlet.RequestType} the email field given by the form is present
     *    - PASSWORD_CHANGE: {@link PasswordServlet.RequestType} the password and auth parameters are present
     *    - UNKNOWN: {@link {@link PasswordServlet.RequestType}} unable to identify the type of request
     */
    private PasswordServlet.RequestType typeOfRequest(HashMap<String,String> data){

        boolean passwordReq = data.containsKey( "email" );
        boolean passwordChange = data.containsKey( "password" ) && data.containsKey("auth");

        return passwordReq?
                passwordChange?
                        PasswordServlet.RequestType.UNKNOWN :
                        RequestType.PASSWORD_REQ :
                passwordChange?
                        RequestType.PASSWORD_CHANGE :
                        PasswordServlet.RequestType.UNKNOWN;
    }

    /**
     * Method to extract the parameters from the request and return it as a key-value collection
     * @param request {@link HttpServletRequest} the request given by the servlet
     * @return Returns an hashmap containing all the fields found
     */
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

    /**
     * Function to verify if an email is correctly formatted
     * @param email An email to verify
     * @return response of the verification as a boolean
     */
    private boolean verifyEmail( String email ){

        if( email == null  || email.length() == 0 )
            return false;

        int host_index = email.indexOf( '@' );
        if( host_index == -1 )
            return false;

        return email.substring(host_index).contains(".");

    }
}