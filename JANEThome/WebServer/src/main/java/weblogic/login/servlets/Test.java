package weblogic.login.servlets;

import jms.beans.UpdateNotifier;
import jms.beans.WebUpdateReceiver;
import jms.interfaces.SenderInterface;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

@WebServlet(name="Test", urlPatterns={"/test"})
public class Test extends HttpServlet {

    @EJB
    SenderInterface producer;

    public void service(HttpServletRequest req, HttpServletResponse resp) {

        Logger logger = Logger.getLogger(getClass().getName());
        new WebUpdateReceiver("test0");
        new WebUpdateReceiver("barsantinicola9@hotmail.it");

        for (int i = 0; i < 500; i++) {
            HashMap message = new HashMap();
            message.put("message number", i);
            if( i%2 == 0 )
            producer.sendMessage(message,"test0");
            else
                producer.sendMessage(message, "barsantinicola9@hotmail.it");
            logger.severe("Message Number "+ i +" sent.");
        }
    }

        /*
        HttpSession session = req.getSession();
        test = (UserLogin)session.getAttribute("userData");


        if( test == null ){
            out.println("no object4");
            test = new UserLogin();
            test.setParameters("test","password");
            session.setAttribute("userData", new UserLogin("test","password"));
        }else{
            out.println("Username: " +test.getUsername());
            test.setParameters("test","password");
        }
*/

}
