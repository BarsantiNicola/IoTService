package utils.mail.interfaces;

import javax.ejb.Remote;

@Remote
public interface EmailServiceLocal {

    boolean sendMailPasswordChange(String email, String token, String fragment1, String fragment2);
    boolean sendMailLoginConfirm(String email, String token, String fragment1, String fragment2);

}
