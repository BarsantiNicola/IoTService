package weblogic.login.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name="Webapp", urlPatterns={"/webapp"})
public class WebappServlet extends HttpServlet{

    //private SmarthomeDescription smarthomeDescription;
    //private DeviceValues deviceValues;

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect("webapp.jsp");
    }

}
