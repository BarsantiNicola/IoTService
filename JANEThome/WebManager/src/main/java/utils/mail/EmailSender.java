package utils.mail;

import utils.mail.interfaces.EmailServiceLocal;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.*;
import javax.mail.internet.*;

    //  EmailSender
    //
    //  Description: static class to send email to an address. The class is based on the GlassFish JavaMail Session
    //               "mail/MailSession" which is linked to a Gmail account and gives two public methods for sending precompiled
    //               mail to a destination
@Stateless
public class EmailSender implements EmailServiceLocal {

        @Resource(name = "mail/mailSession")
        private Session mailSession;

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
        public boolean sendMailPasswordChange(String email, String token){

            String object = "JANEThome Password Change Request";
            String message =
                        "Click on the link below to change your password:<br><br>"+
                        "<a href='"+token+"'>Change Password</a><br><br>"+
                        "Do not reply to this email, it will not be read by anyone";

            return email != null && email.length()>0 && sendEmail( email, object, message);

        }

        //  public method to send a mail form registration confirm. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the registration of the user
        public boolean sendMailLoginConfirm( String email, String token){

            String object = "JANEThome registration confirm";
            String message =
                        "Click on the link below to confirm your registration:<br><br>"+
                        "<a href='"+token+"'>Confirm Registration</a><br><br>"+
                        "Do not reply to this email, it will not be read by anyone";

            return email != null && email.length()>0 && sendEmail(email, object, message);

        }
}



