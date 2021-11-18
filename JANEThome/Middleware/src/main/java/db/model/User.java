package db.model;

import java.io.Serializable;

public class User implements Serializable {


    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    public User() {}

    public User(String username, String firstName, String lastName, String email, String password ) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword( String password ){ this.password = password; }

    public void setFirstName( String name ){ this.firstName = name; }

    public void setLastName( String name ){ this.lastName = name; }

    public void setEmail( String email ){ this.email = email; }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}
