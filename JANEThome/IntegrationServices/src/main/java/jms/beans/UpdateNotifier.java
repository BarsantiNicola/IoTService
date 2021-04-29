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

    @EJB
    private ArchiveInterface destinations;

    private final Logger logger;

    UpdateNotifier(){
        logger = Logger.getLogger(getClass().getName());
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
    }

    public boolean sendMessage( Serializable object, String uID ){

        if( destinations == null ) {

            logger.severe("Error, unable to find the destinations archive");
            return false;

        }

        if( destinations.authorizedDestination(uID))
            try{

                channel.basicPublish("DeviceUpdate", uID, null, SerializationUtils.serialize(object));
                return true;

            }catch(IOException e){

                logger.severe("Error, unable to send the message");
                e.printStackTrace();
                return false;

            }

        else{

            logger.severe("Error, destination not authorized");
            return false;

        }
    }
}

