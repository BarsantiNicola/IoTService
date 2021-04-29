package weblogic.login.servlets;

import jms.beans.WebUpdateReceiver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/info_container")
@WebServlet(name="Webapp", urlPatterns={"/webapp"})
public class WebappServlet extends HttpServlet{

    //private SmarthomeDescription smarthomeDescription;
    //private DeviceValues deviceValues;
    private WebUpdateReceiver updater;

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect("webapp.jsp");
    }

    @OnOpen
    public void onOpen(Session session) {
        try {
            //  TODO get home data(location/sublocation/devices)
            //  TODO get devices data(devices->values)

            session.getBasicRemote().sendText("Hi there, we are successfully connected.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.printf("Message received. Session id: %s Message: %s%n",
                session.getId(), message);
        try {
            session.getBasicRemote().sendText(String.format("We received your message: %s%n", message));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.printf("Session closed with id: %s%n", session.getId());
    }
}
