package web.login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="List", urlPatterns={"/login"})
public class LoginHandler extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        resp.sendRedirect("login.jsp");
        //  TODO Insert cookie management for automatic login

    }

}