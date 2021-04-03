package utils.token.interfaces;

import javax.ejb.Remote;

@Remote
public interface TokenManagerRemote {

    String addPermission(String email);
    boolean verify(String token, String email);
    void refresh(String token);
    void updatePermissions();

}
