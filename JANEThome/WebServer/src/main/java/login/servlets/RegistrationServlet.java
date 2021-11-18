package login.servlets;

//  internal services
import db.interfaces.IDatabase;
import iot.SmarthomeManager;
import db.model.User;
import login.mail.interfaces.IEmailService;
import login.beans.TempData;
import login.interfaces.ITempStorage;

//  ejb3.0
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;

//  http protocol management
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//  exceptions
import java.io.IOException;

//  collections
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *   Servlet to manage user account registration requests. After the receipt of a request it allocates a timed
 *   stateful EJB into the system to maintain a random string and all the registration information.
 *   It then send an email to the user containing a pre-formatted URL to give back the random string. If
 *   the two random strings coincide then the email is valid and the user will be registered info the system
 */
@WebServlet(name="Registration", urlPatterns={"/registration"})
public class RegistrationServlet extends HttpServlet {

    //  instance of mail service
    @EJB
    private IEmailService mailService;  // STATELESS EJB FOR EMAIL MANAGEMENT

    //  instance of database manager
    @EJB
    private IDatabase db;

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        REGISTRATION,
        EMAIL_CONFIRM,
        UNKNOWN
    }

    @SessionScoped
    public void service( HttpServletRequest req, HttpServletResponse resp ) throws IOException {

        Logger logger = LogManager.getLogger(getClass().getName());

        //  extracting the parameters from the request
        HashMap<String, String> parameters = this.extractParameters( req );

        ITempStorage data;

        try{

            InitialContext context = new InitialContext();

            switch( typeOfRequest( parameters )) {

                case REGISTRATION:  //  registration of a new account["name","surname","email","password" parameters present]

                    String email = parameters.get("email");
                    logger.info("Received request from [" + req.getRemoteAddr() + "] of type REGISTRATION. Email: " + email);
                    data = new TempData();

                    if (!this.verifyEmail(email)) {

                        logger.info("Error during registration. Invalid email provided");
                        resp.setStatus(500);  //  sending an error will change the registration form to notify it
                        return;

                    }

                    if (db.emailPresent(email)) {

                        logger.info("Error during registration. The provided email is already present");
                        resp.setStatus(500); //  sending an error will change the registration form to notify it
                        return;

                    }
                    data.setInformations(parameters.get("name"), parameters.get("surname"), email, parameters.get("password"));

                    //  getting the email data used to send back the email
                    Scanner s = new Scanner(this.getServletContext().getResourceAsStream("/WEB-INF/registration.html")).useDelimiter("\\A");
                    String emailContent = s.hasNext() ? s.next() : "";
                    s.close();

                    //  storing of the registration information, when the user will confirm its registration by the email link
                    //  it will resend its associated token which will be used to retrieve its personal information and make the registration
                    for (int count = 0; count < 6; count++)
                        try {

                            //  storing the data for registration confirmation
                            context.bind("ejb:module/registration_" + data.getToken(), data);

                            //  sending the email for password confirmation
                            if (mailService.sendMailLoginConfirm(parameters.get("email"), emailContent, data.getToken()))
                                resp.setStatus(200); //  sending status ok will change the registration form to notify it
                            else
                                resp.setStatus(500); //  sending an error will change the registration form to notify it

                            return;

                        } catch (NamingException e) {

                            //  the token is randomly generated, another instance can be already bound

                            logger.info("Token already present. Random recreation");
                            if (count == 5) {

                                logger.info("Unable to create a valid token. Abort operation");
                                resp.setStatus(500); //  sending an error will change the registration form to notify it
                                break;

                            }
                            data.recreateToken();
                        }

                    break;

                case EMAIL_CONFIRM:  //  email confirmation of a new account["token" parameter present]

                    logger.info("Received request from [" + req.getRemoteAddr() + "] of type EMAIL_CONFIRM. Token: " + parameters.get("token"));
                    //  retrieving the stored information during the registration phase
                    try {

                        data = (TempData) context.lookup("ejb:module/registration_" + parameters.get("token"));
                        if (data != null)
                            req.getSession().setAttribute("userData", data);

                        else {

                            logger.info("The required information are not present. Abort operation");
                            resp.sendRedirect("registration.jsp?state=3");
                            return;

                        }

                    } catch (NamingException e) {

                        logger.info("The required information are not present. Abort operation");
                        resp.sendRedirect("registration.jsp?state=3");
                        return;

                    }

                    context.unbind("ejb:module/registration_" + parameters.get("token"));

                    User user = new User(data.getEmail(), data.getFirstName(), data.getLastName(), data.getEmail(), data.getPassword());

                    if (db.addUser(user)){
                        resp.sendRedirect("registration.jsp?state=2");
                        db.addManager(new SmarthomeManager(data.getEmail(), false, null));
                    }else{  //  if the user is already present, we have to destroy the uploaded smarthome
                        resp.sendRedirect(" registration.jsp?state=3" );
                    }
                    break;

                default:  //  requesting the registration page
                    resp.sendRedirect( "registration.jsp" );

            }

        } catch( NamingException e ) {

            resp.sendRedirect( "registration.jsp?state=3" );
            logger.error( "Expired registration token. Abort" );

        }

    }


    ////////--  UTILITIES  --////////


    /**
     * Method to identify the type of request basing on the available parameters
     * @param data the set of information obtained by the cookies and parameters fields
     * @return Returns the type of request
     *    - LOGIN_REQ: {@link RegistrationServlet.RequestType} the name, surname, email and password fields are present
     *    - AUTOLOGIN_REQ: {@link RegistrationServlet.RequestType} the token field is present
     *    - UNKNOWN: {@link RegistrationServlet.RequestType} unable to identify the type of request
     */
    private RequestType typeOfRequest( HashMap<String,String> data ){

        boolean registration = data.containsKey( "name" ) && data.containsKey( "surname" ) &&
                data.containsKey( "email" ) && data.containsKey( "password" );
        boolean confirmation = data.containsKey( "token" );

        return registration?
                confirmation?
                        RequestType.UNKNOWN :
                        RequestType.REGISTRATION :
                confirmation?
                        RequestType.EMAIL_CONFIRM :
                        RequestType.UNKNOWN;
    }

    /**
     * Method to extract the parameters from the request and return it as a key-value collection
     * @param request {@link HttpServletRequest} the request given by the servlet
     * @return Returns an hashmap containing all the fields found
     */
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