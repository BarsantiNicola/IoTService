package weblogic.login.servlets;

import utils.token.interfaces.TokenManagerRemote;
import weblogic.login.beans.BasicData;
import weblogic.login.beans.UserLogin;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//// LOGINSERVLET
//
//   Description:  business logic behind user login and auto login. The servlets requests follow three
//                 stages for their management:
//                 -- if cookies are present the request is an autologin -> it verifies cookies validity
//                 -- if variables are present the request is a login -> it verifies user/password validity
//                 -- otherwise the request will be redirected to the login.jsp page

@WebServlet(name="Login", urlPatterns={"/login"})
public class LoginServlet extends HttpServlet {

    enum RequestType{  //  TYPE OF REQUEST HANDLED BY THE SERVLET
        LOGIN_REQ,
        AUTOLOGIN_REQ,
        UNKNOWN
    }

    public void service(HttpServletRequest req, HttpServletResponse resp){

        Logger logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);

        HashMap<String,String> parameters = extractParameters(req);

        switch(typeOfRequest(parameters)) {

            case LOGIN_REQ:
                // TODO Add database username/password verification
                logger.info("Received request from [" + req.getRemoteAddr() + "] of type LOGIN_REQ. Email: " +
                        parameters.get("email"));
                if (parameters.get("email").compareTo("barsantinicola9@hotmail.it") == 0 && parameters.get("password").compareTo("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8") == 0) {
                    //  creation of credential cookies to enable auto login and authorize each request from now on
                    logger.info("Login succeded, email: " + parameters.get("email"));
                    BasicData userData = new BasicData();
                    UserLogin infoData = new UserLogin();
                    infoData.setParameters("nicola","barsanti");
                    userData.createToken(parameters.get("email"));
                    resp.addCookie(new Cookie("auth", userData.getToken()));
                    req.getSession().setAttribute("authData", userData);
                    req.getSession().setAttribute("infoData", infoData);
                    logger.info("Session data generated: " + userData.getToken());
                    resp.setStatus(200);
                } else
                    resp.setStatus(500);
                break;

            case AUTOLOGIN_REQ:
                logger.info("Received request from [" + req.getRemoteAddr() + "] of type AUTOLOGIN_REQ. Auth: " +
                        parameters.get("auth"));
                BasicData userData = (BasicData) req.getSession().getAttribute("authData");
                if (userData == null || !userData.isValid(parameters.get("auth"))) {
                    logger.info("Removing invalid cookie: " + parameters.get("auth"));
                    resp.addCookie(new Cookie("auth", ""));

                } else {
                    logger.info("Valid cookie, updating cookie");
                    userData.recreateToken();
                    resp.addCookie(new Cookie("auth", userData.getToken()));
                    req.removeAttribute("authData");
                    req.getSession().setAttribute("authData", userData);
                    try {
                        resp.sendRedirect("webapp");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

            default:
                try {
                    resp.sendRedirect("login.jsp");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

    //  it identified the type of request basing on the available parameters
    //      Return:
    //          - UNKNOWN: request for the login.jsp page
    //          - LOGIN_REQ: login by the login.jsp form
    //          - AUTOLOGIN_REQ: login by session authentication
    private LoginServlet.RequestType typeOfRequest(HashMap<String,String> data){

        boolean loginReq = data.containsKey( "email" ) && data.containsKey("password" );
        boolean autologin = data.containsKey( "auth" );

        return autologin?LoginServlet.RequestType.AUTOLOGIN_REQ: loginReq?  RequestType.LOGIN_REQ: RequestType.UNKNOWN;
    }

    //  it extracts all of the usable parameters from the request and return it as a key-value collection
    private HashMap<String,String> extractParameters(HttpServletRequest request){

        HashMap<String, String> result = new HashMap<>();
        Arrays.asList("email", "password").forEach(
                (field)->{
                    String data = request.getParameter(field);
                    if(data!= null)
                        result.put(field, data);
                });

        if( request.getCookies() != null ) {    //  cookies can be deleted by the browser

            Optional<String> authtoken = Arrays.stream(request.getCookies())
                    .filter(c -> "auth".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findAny();
            if(authtoken.isPresent() && authtoken.get().length() > 0 )
                result.put("auth", authtoken.get());

        }

        return result;
    }

}