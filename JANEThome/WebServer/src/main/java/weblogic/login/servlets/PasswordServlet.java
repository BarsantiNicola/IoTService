package weblogic.login.servlets;

import utils.mail.interfaces.EmailServiceLocal;
import weblogic.login.interfaces.BasicTokenRemote;
import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="PasswordRequest", urlPatterns={"/password"})
public class PasswordServlet extends HttpServlet {

    @EJB
    private BasicTokenRemote data;

    @EJB
    private EmailServiceLocal mailService;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String email = req.getParameter("email");

        if( email!=null ){

            // TODO mail presence verification

            data.createToken();
            if(mailService.sendMailPasswordChange(email,data.getToken()))
                resp.sendRedirect("status.jsp?request_state=1");
            else
                resp.sendRedirect("status.jsp?request_state=10");

            return;

        }

        String token = req.getParameter("token");
        if (token != null) {
            if (data.isValid(token)) {

                // TODO add user registration to mongo
                resp.sendRedirect("status.jsp?request_state=0&token="+token);
                data.resetToken();

            } else
                resp.sendRedirect("status.jsp?request_state=10");
        }else
                resp.sendRedirect( "password.jsp" );

    }

}