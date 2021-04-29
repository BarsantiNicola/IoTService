package weblogic.login.servlets;

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

@WebServlet(name="PasswordRequest", urlPatterns={"/password"})
public class PasswordServlet extends HttpServlet {

    @EJB
    private EmailServiceLocal mailService;

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        PASSWORD_REQ,
        PASSWORD_CHANGE,
        UNKNOWN
    }

    @SessionScoped
    public void service(HttpServletRequest req, HttpServletResponse resp){

        Logger logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);

        HashMap<String, String> parameters = extractParameters(req);
        BasicTokenRemote data;
        try{
            InitialContext context = new InitialContext();
            switch(typeOfRequest(parameters)){
                case PASSWORD_REQ:     //  registration of a new account
                    logger.info("Received request from ["+req.getRemoteAddr()+"] of type PASSWORD_REQ. Email: " +
                            parameters.get("email"));
                    data = new UserData();
                    data.createToken(parameters.get("email"));

                    Scanner s = new Scanner(this.getServletContext().getResourceAsStream("/WEB-INF/password.html")).useDelimiter("\\A");
                    String email = s.hasNext() ? s.next() : "";
                    s.close();
                    int count=0;
                    while(true)
                        try {
                            context.bind("ejb:module/password_" + data.getToken(), data);
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

                    if(mailService.sendMailPasswordChange(parameters.get("email"), email, data.getToken()))
                        resp.setStatus(200);
                    else
                        resp.setStatus(500);
                    break;

                case PASSWORD_CHANGE:    //  email confirmation of a new account
                    logger.info("Received request from ["+req.getRemoteAddr()+"] of type PASSWORD_CHANGE. Auth: " +
                            parameters.get("auth"));
                    data = (BasicData) context.lookup("ejb:module/password_" + parameters.get("auth"));

                    //  TODO change the password value
                    // if(changePassword(data.getUser(), paramegers.get("password")){
                    if(true)
                        resp.setStatus(200);
                    else
                        resp.setStatus(500);

                    context.unbind("ejb:module/password_" + parameters.get("auth"));
                    break;

                default:
                    resp.setStatus(500);
            }

        } catch (NamingException e) {

            resp.setStatus(500);
            logger.warning("Expired password token. Abort" );

        }

    }

    //  it identified the type of request basing on the available parameters
    //      Return:
    //          - UNKNOWN: unable to identify the type of request
    //          - PASSWORD_REQ: request to change the password
    //          - PASSWORD_CHANGE: message for creating the new password
    private PasswordServlet.RequestType typeOfRequest(HashMap<String,String> data){

        boolean passwordReq = data.containsKey( "email" );
        boolean passwordChange = data.containsKey( "password" ) && data.containsKey("auth");

        return passwordReq?passwordChange? PasswordServlet.RequestType.UNKNOWN: RequestType.PASSWORD_REQ:passwordChange? RequestType.PASSWORD_CHANGE: PasswordServlet.RequestType.UNKNOWN;
    }

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
    private HashMap<String,String> extractParameters(HttpServletRequest request){

        HashMap<String, String> result = new HashMap<>();
        Arrays.asList("email", "password", "auth").forEach(
                (field)->{
                    String data = request.getParameter(field);
                    if(data!= null)
                        result.put(field, data);
                });
        return result;
    }

}