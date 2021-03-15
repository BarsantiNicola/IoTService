package utils.mail.interfaces;

import javax.ejb.Local;

@Local
public interface EmailServiceLocal {

    boolean sendMailPasswordChange(String email, String token);
    boolean sendMailLoginConfirm(String email, String token);

}
