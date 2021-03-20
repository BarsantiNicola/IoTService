

package weblogic.login.servlets;

import weblogic.login.interfaces.UserLoginLocal;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="Login", urlPatterns={"/login"})
public class LoginServlet extends HttpServlet {

    @SessionScoped
    @EJB
    UserLoginLocal userData;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html");

        String username = req.getParameter("email");
        String password = req.getParameter( "pass");

        if( username != null && password != null ){
            // TODO perform login
            userData.setParameters(username, password);
            resp.sendRedirect("main.jsp");
            return;
        }

        if( userData.getUsername() != null && userData.getPassword() != null ){
            // TODO perform automatic login
            resp.sendRedirect("main.jsp");
        }else
            resp.sendRedirect("login.jsp");
            //  TODO Insert cookie management for automatic login

    }

}