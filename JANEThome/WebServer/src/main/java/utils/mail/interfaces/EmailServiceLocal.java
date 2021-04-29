package utils.mail.interfaces;

import javax.ejb.Remote;

@Remote
public interface EmailServiceLocal {

    boolean sendMailPasswordChange(String email, String content, String token);
    boolean sendMailLoginConfirm(String email, String content, String token);

}
