package jms.interfaces;

import jms.Message;

import javax.ejb.Remote;
import java.io.Serializable;


@Remote
public interface SenderInterface {

    boolean sendMessage(Serializable object, String uID);

}
