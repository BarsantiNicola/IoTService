package web.login.servlets;

import utils.POJOType;
import web.login.interfaces.DataContainerInterfaceLocal;
import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@WebServlet(name="Password", urlPatterns={"/password_change"})
public class PasswordChangeHandler extends HttpServlet {

    @EJB
    private DataContainerInterfaceLocal data;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html");
        if( !data.verifyToken(req.getParameter("name"), req.getParameter("token"), POJOType.PASSWORD) )
            resp.sendRedirect("status.jsp?request_state=10");

        PrintWriter out = resp.getWriter();
        ServletContext context2 =  req.getServletContext();
        try (Scanner scanner = new Scanner(context2.getResourceAsStream("/WEB-INF/password_change.jsp"), StandardCharsets.UTF_8.name())) {
            out.println( scanner.useDelimiter("\\A").next());
        }
        out.close();

    }

}
