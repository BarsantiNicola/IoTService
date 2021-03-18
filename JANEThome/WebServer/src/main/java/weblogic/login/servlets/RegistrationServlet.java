package weblogic.login.servlets;

import utils.mail.interfaces.EmailServiceLocal;
import weblogic.login.interfaces.NamedTokenRemote;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name="Registration", urlPatterns={"/registration"})
public class RegistrationServlet extends HttpServlet {

    @EJB
    private NamedTokenRemote data;

    @EJB
    private EmailServiceLocal mailService;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String name     = req.getParameter("name");
        String surname  = req.getParameter("surname");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        if(name !=null && surname != null && email != null && password != null ){
            data.setInformations(name, surname, email, password);
            if(mailService.sendMailLoginConfirm(email,data.getToken()))
                resp.sendRedirect("status.jsp?request_state=1");
            else
                resp.sendRedirect("status.jsp?request_state=10");

        }else {

            String token = req.getParameter("token");
            if (token != null) {
                if (data.isValid(token)) {

                    // TODO add user registration to mongo
                    resp.sendRedirect("status.jsp?request_state=0");
                    data.resetInformations();

                } else
                    resp.sendRedirect("status.jsp?request_state=10");
            }else
                resp.sendRedirect( "registration.jsp" );
        }

    }

}