package utils.mail.beans;

import config.interfaces.ConfigurationInterface;
import utils.mail.interfaces.EmailServiceLocal;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.*;

import java.util.HashMap;
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

        @EJB
        private ConfigurationInterface configuration;

        private Logger logger;

        //// PUBLIC FUNCTIONS

        //  public method to send a password change email to a user. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the creation of the change password form
        public boolean sendMailPasswordChange( String email, String content, String token ){

            this.initializeLogger();
            String object = "JANEThome Password Change Request";

            return email != null && email.length()>0 && sendEmail(email, object, createEmailPasswordContent(content, token));

        }

        //  public method to send a mail form registration confirm. It returns true in case of success
        //  Arguments:
        //      - mailSession: an instance of the JavaMail Session maintained by glassfish
        //      - email: email of the user
        //      - token: token given to the user to authorize the registration of the user
        public boolean sendMailLoginConfirm( String email, String content, String token ){

            this.initializeLogger();
            String object = "JANEThome registration confirm";

            return email != null && email.length()>0 && sendEmail(email, object, createEmailRegistrationContent(content, token));

        }

        ////  UTILITY FUNCTIONS

        //  initialization of the logger that prevent that more handlers are allocated
        private void initializeLogger(){

            this.logger = Logger.getLogger( getClass().getName() );

            //  verification of the number of instantiated handlers
            if( this.logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

                Handler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new SimpleFormatter());
                this.logger.addHandler(consoleHandler);

            }
        }

        //  private function to perform the send of an email using the glassfish javamail session. The session isn't
        //  directly available in the private components of the server and it is passed from the servlet interface which
        //  calls the methods
        private boolean sendEmail( String address, String subject, String content ){

            logger.info("Send email to ["+address+"] for ["+subject+"]" );

            Message msg = new MimeMessage( mailSession );

            try{

                msg.setSubject( subject );
                msg.setRecipient( Message.RecipientType.TO, new InternetAddress( address ));
                msg.setContent( content,"text/html; charset=utf-8" );

                Transport.send( msg );
                logger.fine( "Email correctly sent to " + address );
                return true;

            }catch( MessagingException me ) {

                logger.severe( "Error during email sending" );
                me.printStackTrace();
                return false;

            }
        }

        //  generates a redirection link for the registration and from it generates the email to be send
        private String createEmailRegistrationContent( String email, String token ){

            HashMap<String,String> conf = null;
            if( configuration != null )
                conf = configuration.getConfiguration( "mail" );

            String URL;
            if( conf != null && conf.containsKey("secure") && conf.containsKey( "hostname") && conf.containsKey( "port" )) {

                URL = Integer.parseInt(conf.get("secure")) == 1 ? "https" : "http";
                URL = URL + "://" + conf.get("hostname") + ":" + conf.get("port") + "/WebServer/registration?token=" + token;
                logger.info( "Redirection link correctly generated: " + URL );

            }else {
                URL = "http://localhost:8080/WebServer/registration?token=" + token;
                logger.severe( "Error, unable to load the configuration, generated default link: " + URL );
            }

            return email.replaceFirst("--placeholder--", URL);

        }
        //  generates a redirection link for the password change and from it generates the email to be send
        private String createEmailPasswordContent( String email, String token ){

            HashMap<String,String> conf = null;
            if( configuration != null )
                conf = configuration.getConfiguration( "mail" );

            String URL;
            if( conf != null && conf.containsKey("secure") && conf.containsKey( "hostname") && conf.containsKey( "port" )) {

                URL = Integer.parseInt(conf.get("secure")) == 1 ? "https" : "http";
                URL = URL + "://" + conf.get("hostname") + ":" + conf.get("port") + "/WebServer/password.jsp?state=1&auth=" + token;
                logger.info( "Redirection link correctly generated: " + URL );

            }else {

                URL = "http://localhost:8080/WebServer/password.jsp?state=1&auth=" + token;
                logger.severe("Error, unable to load the configuration, generated default link: " + URL);

            }
            return email.replaceFirst("--placeholder--", URL);

        }
}



