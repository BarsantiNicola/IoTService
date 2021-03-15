package web.login.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="Page", urlPatterns={"/login"})
public class PageHandler extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html");
        resp.sendRedirect("login.jsp");
        //  TODO Insert cookie management for automatic login

    }

}