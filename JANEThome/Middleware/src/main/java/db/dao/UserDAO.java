package db.dao;

import com.mongodb.WriteResult;
import db.interfaces.IUserDAO;
import iot.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

public class UserDAO extends BasicDAO<User, String> implements IUserDAO {

    public UserDAO(Class<User> entityClass, Datastore ds) {
        super(entityClass, ds);
    }

    public User get(ObjectId objectId) {
        return this.ds.get(this.entityClazz, objectId);
    }

}
