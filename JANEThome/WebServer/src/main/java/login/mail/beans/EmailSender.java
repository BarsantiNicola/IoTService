package login.mail.beans;

//  internal services
import login.mail.interfaces.IEmailService;
import config.interfaces.IConfiguration;
import java.util.Properties;

//  logger
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//  ejb3.0
import javax.enterprise.context.ApplicationScoped;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;

//  mail session
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Class in charge of sending email to the users. The class is based on the Glassfish JavaMail Session
 * which is linked with a Gmail account
 * <p>
 * The class gives two static methods to send precompiled mails(change password/confirm registration) to a destination
 */
@Stateless
@Named("mailService")
public class EmailSender implements IEmailService {

    //  connection with the stored mail session by its jndi name
    @ApplicationScoped
    @Resource(lookup = "mail/mailSession")
    private Session mailSession;

    //  resource manager of the system configurations
    @EJB
    private IConfiguration configuration;

    //////////--   PUBLIC METHODS   --//////////

    /**
     * Send an email of "password change" to a given user
     *
     * @param email the destination email
     * @param content the content of the email
     * @param token an authorized token to be included into the email for the password change
     * @return the result of the operation as a boolean
     */
    public boolean sendMailPasswordChange( String email, String content, String token ){

        String object = "JANEThome Confirm Request";

        return email != null && content != null &&  token != null &&                     //  verification of params presence
                email.length()>0 && content.length()>0 && token.length() > 0 &&          //  verification of tainted data
                sendEmail( email, object, createEmailPasswordContent(content, token) );  //  email generation and sending

    }


    /**
     * Send an email of "confirm registration" to a given user
     *
     * @param email the destination email
     * @param content the content of the email
     * @param token an authorized token to be included into the email for the email confirmation
     * @return the result of the operation as a boolean
     */
    public boolean sendMailLoginConfirm( String email, String content, String token ){

        String object = "JANEThome Confirm Registration";

        return email != null && content != null &&  token != null &&                       //  verification of params presence
                email.length()>0 && content.length()>0 && token.length() > 0 &&            //  verification of tainted data
                sendEmail( email, object, createEmailRegistrationContent( content, token ));  //  email generation and sending

    }


    //////////--   UTILITIES   --//////////


    /**
     * Method to send an email to a destination
     *
     * @param email the destination email
     * @param subject the subject field of the email
     * @param content the content of the email
     * @return the result of the operation as a boolean
     */
    private boolean sendEmail( String email, String subject, String content ){

        Logger logger = LogManager.getLogger( getClass().getName() );
        logger.info("Sending an email to ["+email+"] for ["+subject+"]" );

        Message msg = new MimeMessage( mailSession );

        try{

            msg.setSubject( subject );
            msg.setRecipient( Message.RecipientType.TO, new InternetAddress( email ));
            msg.setContent( content,"text/html; charset=utf-8" );

            Transport.send( msg );
            logger.info( "Email correctly sent to [" + email + "]" );
            return true;

        }catch( MessagingException me ) {

            logger.error( "An error as occurred during the send of an email to [" + email + "] for [" + subject +"]" );
            me.printStackTrace();
            return false;

        }
    }


    /**
     * Method to generate an email content for the account confirmation
     *
     * @param email the destination email
     * @param token an authorized token to be included into the email for the email confirmation
     * @return a string containing the precompiled content to be sent
     */
    private String createEmailRegistrationContent( String email, String token ){

        //  generation of the email
        return email.replaceFirst("--placeholder--", this.URLgenerator() + "registration?token=" + token );

    }


    /**
     * Method to generate an email content for the password change
     *
     * @param email the destination email
     * @param token authorized token to be included into the email for the password change
     * @return a string containing the precompiled content to be sent
     * */
    private String createEmailPasswordContent( String email, String token ){

        //  generation of the email
        return email.replaceFirst("--placeholder--", this.URLgenerator() + "password.jsp?state=1&auth=" + token );

    }


    /**
     * Method to generate the URL of the service
     *
     * @return a string containing the precompiled base URL of the service
     * */
    private String URLgenerator(){

        Properties conf = null;
        if( configuration != null )
            conf = configuration.getConfiguration( "mail" );  //  getting the configuration of the email

        //  secure parameter defines if the service is using http(0) or https(1) default http
        boolean secure = conf != null && conf.containsKey( "secure" ) && conf.getProperty( "secure" ).compareTo( "1" ) == 0;
        //  hostname assigned to the service(valid URLs/IPS/hostnames default localhost)
        String hostname = conf != null && conf.containsKey( "hostname" )? conf.getProperty( "hostname" ) : "localhost";
        //  the name of the deployed service(name of the deployed jar, default WebServer)
        String service = conf != null && conf.containsKey( "service" )? conf.getProperty( "service" ) : "WebServer";
        //  port used by the service for the incoming requests(default 8080)
        String port = conf != null && conf.containsKey( "port" )? conf.getProperty("port") : "8080";

        //  generation of the email
        return ( secure? "https" : "http" ) + "://" + hostname + ":" + port + "/"+ service + "/";

    }
}