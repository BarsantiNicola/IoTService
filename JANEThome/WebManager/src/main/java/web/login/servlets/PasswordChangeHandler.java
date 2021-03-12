package web.login.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

@WebServlet(name="Password", urlPatterns={"/password_change"})
public class PasswordChangeHandler extends HttpServlet {

    @Context ServletContext context;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ServletContext context = getServletContext();
        resp.setContentType("text/html");
        Object token = req.getParameter("token");

        if( token == null ) {
            resp.sendRedirect("status.jsp?request_state=error");
            return;
        }

        //  TODO Token Verification
        PrintWriter out = resp.getWriter();
        try (Scanner scanner = new Scanner(context.getResourceAsStream("/WEB-INF/password_change.jsp"), StandardCharsets.UTF_8.name())) {
            out.println( scanner.useDelimiter("\\A").next());
        }

    }

}
