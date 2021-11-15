package db.interfaces;

import db.model.User;

public interface IUserDAO extends IGenericDao<User>{
    String USERNAME = "username";
    String FIRSTNAME = "firstName";
    String LASTNAME = "lastName";
    String EMAIL = "email";
    String PASS = "password";
    String MANAGER = "homeManager";
}
