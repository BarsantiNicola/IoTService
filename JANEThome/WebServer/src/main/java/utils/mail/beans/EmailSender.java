package utils.mail.beans;

import utils.mail.interfaces.EmailServiceLocal;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.*;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

//  EmailSender
    //
    //  Description: static class to send email to an address. The class is based on the GlassFish JavaMail Session
    //               "mail/MailSession" which is linked to a Gmail account and gives two public methods for sending precompiled
    //               mail to a destination

@Stateless
@Named("mailService")
public class EmailSender implements EmailServiceLocal {

        @Resource(name = "mail/mailSession")
        @ApplicationScoped
        private Session mailSession;
        private final String IP="localhost";
        private final String PORT="8080";
        private final boolean SECURE = false;
        //  private function to perform the send of an email using the glassfish javamail session. The session isn't
        //  directly available in the private components of the server and it is passed from the servlet interface which
        //  calls the methods
        private boolean sendEmail(String address, String subject, String content){

            Message msg = new MimeMessage(mailSession);
            try {

                msg.setSubject(subject);
                msg.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(address));

                msg.setContent(content,"text/html; charset=utf-8");
                Transport.send(msg);

            } catch(MessagingException me) {
                me.printStackTrace();
                return false;
            }
            return true;
        }

        //  public method to send a password change email to a user. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the creation of the change password form
        public boolean sendMailPasswordChange(String email, String token, String fragment1, String fragment2){

            String object = "JANEThome Password Change Request";
            String URL = SECURE?"https":"http";
            URL = URL + "://" + IP + ":" + PORT + "/WebServer/password_change.jsp?token=" + token;

            String message = fragment1 + URL + fragment2;
            return email != null && email.length()>0 && sendEmail( email, object, message);

        }

        //  public method to send a mail form registration confirm. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the registration of the user
        public boolean sendMailLoginConfirm( String email, String token, String fragment1, String fragment2){

            String object = "JANEThome registration confirm";
            String URL = SECURE?"https":"http";
            URL = URL + "://" + IP + ":" + PORT + "/WebServer/registration?token=" + token;

            String message = fragment1 + URL + fragment2;

            return email != null && email.length()>0 && sendEmail(email, object, message);

        }


}



