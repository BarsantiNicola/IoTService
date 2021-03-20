package weblogic.login.servlets;

import utils.mail.interfaces.EmailServiceLocal;
import weblogic.login.interfaces.BasicTokenRemote;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Scanner;

@WebServlet(name="PasswordRequest", urlPatterns={"/password"})
public class PasswordServlet extends HttpServlet {

    @EJB
    private BasicTokenRemote data;

    @EJB
    private EmailServiceLocal mailService;

    @SessionScoped
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String email = req.getParameter("email");

        if( email!=null ){

            // TODO mail presence verification
            Scanner s = new Scanner(this.getServletContext().getResourceAsStream("/WEB-INF/password_change_fragment_1")).useDelimiter("\\A");
            String fragment1 = s.hasNext() ? s.next() : "";
            s.close();
            s = new Scanner(this.getServletContext().getResourceAsStream("/WEB-INF/password_change_fragment_2")).useDelimiter("\\A");
            String fragment2 = s.hasNext() ? s.next() : "";
            s.close();
            data.createToken(email);
            if(mailService.sendMailPasswordChange(email,data.getToken(),fragment1, fragment2))
                resp.sendRedirect("status.jsp?request_state=1");
            else
                resp.sendRedirect("status.jsp?request_state=10");

            return;

        }

        String token = req.getParameter("token");
        String password = req.getParameter( "password" );
        if (token != null && password != null ) {
            // TODO add verification and password update on mongo
            resp.sendRedirect("status.jsp?request_state=3");
            data.resetToken();
        }else
            resp.sendRedirect( "password.jsp" );

    }

}