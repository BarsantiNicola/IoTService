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
import java.io.PrintWriter;
import java.util.Scanner;

@WebServlet(name="PasswordRequest", urlPatterns={"/password"})
public class PasswordServlet extends HttpServlet {

    @EJB
    @SessionScoped
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
                resp.setStatus(200);
            else
                resp.setStatus(500);

            return;

        }

        String token = req.getParameter("auth");
        String password = req.getParameter( "password" );
        if (token != null && password != null && data.isValid(token)) {
            // TODO add verification and password update on mongo
            resp.setStatus(200);
            data.resetToken();
        }else
            resp.setStatus(500);

    }

}