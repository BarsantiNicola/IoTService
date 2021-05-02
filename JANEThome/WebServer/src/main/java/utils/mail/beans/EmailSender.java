package utils.mail.beans;

import utils.mail.interfaces.EmailServiceLocal;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.*;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//  EmailSender
    //
    //  Description: static class to send email to an address. The class is based on the GlassFish JavaMail Session
    //               "mail/MailSession" which is linked to a Gmail account and gives two public methods for sending precompiled
    //               mail to a destination

@Stateless
@Named("mailService")
public class EmailSender implements EmailServiceLocal {

        @ApplicationScoped
        @Resource(lookup = "mail/mailSession")
        private Session mailSession;
        private final String IP="janethome.asuscomm.com";
        private final String PORT="8080";
        private final boolean SECURE = false;

        //  private function to perform the send of an email using the glassfish javamail session. The session isn't
        //  directly available in the private components of the server and it is passed from the servlet interface which
        //  calls the methods
        private boolean sendEmail(String address, String subject, String content){

            Logger logger = Logger.getLogger(getClass().getName());
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            logger.info("Send email to ["+address+"] for ["+subject+"]");

            Message msg = new MimeMessage(mailSession);
            try {

                msg.setSubject(subject);
                msg.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(address));

                msg.setContent(content,"text/html; charset=utf-8");
                Transport.send(msg);

            } catch(MessagingException me) {

                logger.severe("Error during email sending");
                me.printStackTrace();
                return false;

            }
            logger.fine("Email correctly sent");
            return true;

        }

        //  public method to send a password change email to a user. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the creation of the change password form
        public boolean sendMailPasswordChange(String email, String content, String token){

            String object = "JANEThome Password Change Request";

            return email != null && email.length()>0 && sendEmail(email, object, createEmailPasswordContent(content, token));

        }

        //  public method to send a mail form registration confirm. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the registration of the user
        public boolean sendMailLoginConfirm( String email, String content, String token){

            String object = "JANEThome registration confirm";

            return email != null && email.length()>0 && sendEmail(email, object, createEmailRegistrationContent(content, token));

        }

        private String createEmailRegistrationContent(String email, String token){

            String URL = SECURE?"https":"http";
            URL = URL + "://" + IP + ":" + PORT + "/WebServer/registration?token=" + token;

            return email.replaceFirst("--placeholder--", URL);
        }

        private String createEmailPasswordContent(String email, String token){

            String URL = SECURE?"https":"http";
        URL = URL + "://" + IP + ":" + PORT + "/WebServer/password.jsp?state=1&auth=" + token;

        return email.replaceFirst("--placeholder--", URL);

    }

}



