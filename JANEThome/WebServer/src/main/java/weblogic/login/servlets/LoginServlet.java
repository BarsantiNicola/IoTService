package weblogic.login.servlets;

import utils.token.interfaces.TokenManagerRemote;
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
import java.util.Optional;

//// LOGINSERVLET
//
//   Description:  business logic behind user login and auto login. The servlets requests follow three
//                 stages for their management:
//                 -- if cookies are present the request is an autologin -> it verifies cookies validity
//                 -- if variables are present the request is a login -> it verifies user/password validity
//                 -- otherwise the request will be redirected to the login.jsp page

@WebServlet(name="Login", urlPatterns={"/login"})
public class LoginServlet extends HttpServlet {

    @ApplicationScoped
    @EJB
    TokenManagerRemote permissionArchive;    // COOKIES MANAGER

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        permissionArchive.updatePermissions();  //  delete expired cookies

        resp.setContentType("text/html");
        if( req.getCookies() != null ) {    //  cookies can be deleted by the browser

            Optional<String> authtoken = Arrays.stream(req.getCookies())
                    .filter(c -> "authtoken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findAny();

            Optional<String> email = Arrays.stream(req.getCookies())
                    .filter(c -> "email".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findAny();

            //  verification of cookie with the archive, both the token and the email must be correct
            if (authtoken.isPresent() && email.isPresent() && permissionArchive.verify(authtoken.get(), email.get())) {
                //  redirection to the application page
                //  to be here the user has cookies, i can directly use them
                resp.sendRedirect("webapp.jsp");
                return;

            }
        }

        // the servlet will also be used by the login form, if email and password variables are set this is the case
        String username = req.getParameter("email");
        String password = req.getParameter( "password");

        if( username != null && password != null ){
            //  verification of credentials with the saved into the database
            if( username.compareTo("barsantinicola9@hotmail.it") == 0 && password.compareTo("66dbc64b2bcc080197b0bcc720dd2745c4e39b638977713ccc40e753cfead78f") == 0 ) {
                //  creation of credential cookies to enable auto login and authorize each request from now on
                String auth = permissionArchive.addPermission(username);
                resp.addCookie(new Cookie("authtoken",auth));
                resp.addCookie(new Cookie("email",username));
                //  i need to save the values not only in cookies
                //  a user can disable them and otherwise he cannot do anything(needs auth)
                PrintWriter out = resp.getWriter();
                out.print("?auth="+auth+"?user="+username);
                out.flush();
                out.close();

            }else
                resp.setStatus(500);  //  an error will trigger the html page to fail the login
            return;
        }

        //  neither cookies or password was provided, the user wants to see the login page
        resp.sendRedirect("login.jsp");

    }

}