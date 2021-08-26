package utils.mail.interfaces;

import javax.ejb.Local;

@Local
//  EJB interface for sending email to users
public interface EmailServiceLocal {

    boolean sendMailPasswordChange( String email, String content, String token );
    boolean sendMailLoginConfirm( String email, String content, String token );

}
