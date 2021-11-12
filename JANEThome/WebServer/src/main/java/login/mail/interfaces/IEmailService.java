package login.mail.interfaces;

import javax.ejb.Remote;

/**
 * EJB interface to send precompiled email to users
 * The service is accesible remotely to be used both by the WebService and the GoogleHomeService
 */
@Remote
public interface IEmailService {

    boolean sendMailPasswordChange( String email, String content, String token ); //  send an email for password change
    boolean sendMailLoginConfirm( String email, String content, String token );  //  send an email for confirm the registration

}
