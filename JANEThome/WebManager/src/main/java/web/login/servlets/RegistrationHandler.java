package web.login.servlets;

import utils.POJOType;
import utils.TimedDataPOJO;
import utils.mail.interfaces.EmailServiceLocal;
import web.login.interfaces.DataContainerInterfaceLocal;
import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name="Registration", urlPatterns={"/registration"})
public class RegistrationHandler extends HttpServlet {

    @EJB
    private DataContainerInterfaceLocal data;

    @EJB
    private EmailServiceLocal mailService;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        TimedDataPOJO info = new TimedDataPOJO( POJOType.REGISTRATION,6 );
        String URI = "http://localhost:8080/WebManager-1.0-SNAPSHOT/confirm?token="+info.getToken()+"&name="+data.addToken(info);

        String email = req.getParameter("email" );
        if( mailService.sendMailLoginConfirm( email, URI ))
            resp.sendRedirect("status.jsp?request_state=1");
        else
            resp.sendRedirect("status.jsp?request_state=10");

    }

}