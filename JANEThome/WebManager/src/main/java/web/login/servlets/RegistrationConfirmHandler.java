package web.login.servlets;

import utils.POJOType;
import web.login.interfaces.DataContainerInterfaceLocal;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="Confirm", urlPatterns={"/confirm"})
public class RegistrationConfirmHandler extends HttpServlet {

    @EJB
    private DataContainerInterfaceLocal data;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html");
        if( !data.verifyToken(req.getParameter("name"), req.getParameter("token"), POJOType.REGISTRATION) )
            resp.sendRedirect("status.jsp?request_state=10");

        resp.sendRedirect( "status.jsp?request_state=0");

    }

}