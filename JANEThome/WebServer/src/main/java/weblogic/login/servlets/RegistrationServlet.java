package weblogic.login.servlets;

import jms.interfaces.ArchiveInterface;
import utils.device.DeviceContainer;
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
    private ArchiveInterface authorizedUser;

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
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Logger logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);

        HashMap<String, String> parameters = extractParameters(req);
        RegistrationInterface data;
        logger.severe("new request received: " + typeOfRequest(parameters));
        try{
            InitialContext context = new InitialContext();
            switch(typeOfRequest(parameters)){
                case REGISTRATION:     //  registration of a new account
                    logger.info("Received request from ["+req.getRemoteAddr()+"] of type REGISTRATION. Email: " +
                            parameters.get("email"));
                    data = new UserData();
                    data.setInformations(parameters.get("name"), parameters.get("surname"),
                            parameters.get("email"), parameters.get("password"));

                    Scanner s = new Scanner(this.getServletContext().getResourceAsStream("/WEB-INF/registration.html")).useDelimiter("\\A");
                    String email = s.hasNext() ? s.next() : "";
                    s.close();
                    int count=0;
                    while(true)
                        try {
                            context.bind("ejb:module/registration_" + data.getToken(), data);
                            break;
                        }catch( NamingException e ){
                            count++;
                            logger.warning("Token already present. Random recreation");
                            if( count == 5 ){
                                logger.severe("Unable to create a valid token. Abort operation");
                                resp.setStatus(500);
                                return;
                            }
                            data.recreateToken();
                        }

                    if(mailService.sendMailLoginConfirm(parameters.get("email"), email, data.getToken()))
                        resp.setStatus(200);
                    else
                        resp.setStatus(500);
                    break;

                case EMAIL_CONFIRM:    //  email confirmation of a new account
                    logger.info("Received request from ["+req.getRemoteAddr()+"] of type EMAIL_CONFIRM. Token: " +
                            parameters.get("token"));
                    data = (UserData) context.lookup("ejb:module/registration_" + parameters.get("token"));
                    req.getSession().setAttribute("userData", data);
                    //  TODO use data information to register an account
                    try{
                        context.bind("ejb:module/data_container_"+data.getEmail(), new DeviceContainer(data.getEmail()));
                        authorizedUser.addDestination(data.getEmail());

                    }catch( NamingException err){
                        logger.severe("Error, user container already present");
                        context.unbind("ejb:module/registration_" + parameters.get("token"));
                        resp.sendRedirect("registration.jsp?state=3");
                        return;
                    }

                    context.unbind("ejb:module/registration_" + parameters.get("token"));
                    resp.sendRedirect("registration.jsp?state=2");
                    break;

                default:
                    resp.sendRedirect("registration.jsp");
            }

        } catch (NamingException e) {

            resp.sendRedirect("registration.jsp?state=3");
            logger.warning("Expired registration token. Abort" );

        }

    }

    //  it identified the type of request basing on the available parameters
    //      Return:
    //          - UNKNOWN: unable to identify the type of request
    //          - REGISTRATION: request to register a new account
    //          - EMAIL_CONFIRM: verification of the email by a token
    private RequestType typeOfRequest(HashMap<String,String> data){

        boolean registration = data.containsKey( "name" ) && data.containsKey( "surname" ) &&
                data.containsKey( "email" ) && data.containsKey( "password" );
        boolean confirmation = data.containsKey( "token" );

        return registration?confirmation?RequestType.UNKNOWN:RequestType.REGISTRATION:confirmation?RequestType.EMAIL_CONFIRM:RequestType.UNKNOWN;
    }

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
    private HashMap<String,String> extractParameters(HttpServletRequest request){

        HashMap<String, String> result = new HashMap<>();
        Arrays.asList("name", "surname", "email", "password", "token").forEach(
                (field)->{
                    String data = request.getParameter(field);
                    if(data!= null)
                        result.put(field, data);
                });
        return result;
    }

}