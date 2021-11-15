package db.model;

import com.google.gson.annotations.Expose;
import db.model.MongoEntity;
import iot.SmarthomeManager;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.io.Serializable;

@Entity(value = "User", noClassnameStored = true)
public class User extends MongoEntity implements Serializable {

    @Expose
    private String username;
    @Expose
    private String firstName;
    @Expose
    private String lastName;
    @Expose
    private String email;
    @Expose
    private String password;

    @Expose
    @Reference
    private SmarthomeManager homeManager;

    public User() {
        this.setKey(new ObjectId());
    }

    public User(String username, String firstName, String lastName, String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.setKey(new ObjectId());
    }

    public User(String username, String firstName, String lastName, String email, String password, SmarthomeManager homeManager) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.homeManager = homeManager;
        this.setKey(new ObjectId());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public SmarthomeManager getHomeManager() {
        return homeManager;
    }

    public void setHomeManager(SmarthomeManager homeManager) {
        this.homeManager = homeManager;
    }
}
