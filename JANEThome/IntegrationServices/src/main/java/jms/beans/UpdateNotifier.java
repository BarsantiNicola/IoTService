package jms.beans;

import jms.EndPoint;
import javax.ejb.EJB;
import java.io.IOException;
import java.io.Serializable;
import javax.ejb.Stateless;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import jms.interfaces.ArchiveInterface;
import jms.interfaces.SenderInterface;
import org.apache.commons.lang.SerializationUtils;

@Stateless
public class UpdateNotifier extends EndPoint implements SenderInterface {

    private final Logger logger;

    public UpdateNotifier(){
        logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
    }

    public boolean sendMessage( Serializable object, String uID ){

            try{
                logger.info("send object: " + object);
                channel.basicPublish("DeviceUpdate", uID, null, SerializationUtils.serialize(object));
                logger.info("ok");
                return true;

            }catch(IOException e){

                logger.severe("Error, unable to send the message");
                e.printStackTrace();
                return false;

            }

    }
}

