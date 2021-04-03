package utils.token;

import java.time.LocalDateTime;

public class UserCredential {

    private final String email;
    private LocalDateTime expire;
    private final int expireTime;

    public UserCredential(String email, int hourToExpire){

        this.email = email;
        this.expireTime = hourToExpire;
        this.expire = LocalDateTime.now().plusHours(this.expireTime);

    }

    public String getEmail(){
        return this.email;
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(this.expire);
    }

    public void refresh(){
        this.expire = LocalDateTime.now().plusHours(this.expireTime);
    }

}
