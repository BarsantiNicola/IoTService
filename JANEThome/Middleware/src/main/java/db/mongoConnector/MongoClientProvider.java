package db.mongoConnector;

import com.mongodb.*;
import db.dao.SmartHomeManagerDAO;
import db.dao.UserDAO;
import db.interfaces.ISmartHomeManagerDAO;
import db.interfaces.IUserDAO;
import iot.SmarthomeManager;
import iot.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

import static db.interfaces.IGenericDao.DB_NAME;

/**
 * This class have all methods for the access of DB
 * <p>
 * Before to call a methods you must use the method connectDB that connect the db
 * This method must be called only one time on the lifetime of the program
 */
public final class MongoClientProvider {
    private static MongoClient mc;
    private static Morphia morphia;
    private static Datastore datastore;
    private static SmartHomeManagerDAO managerDao;
    private static UserDAO userDAO;

    /**
     * Open DB connection
     */
    public static boolean connectDB() {
        try {
//            mc = new MongoClient(new MongoClientURI(DB_HOST));
            mc = new MongoClient("localhost", 27017);
            morphia = new Morphia();
            morphia.map(SmarthomeManager.class);
            morphia.map(User.class);
            datastore = morphia.createDatastore(mc, DB_NAME);
//            datastore.ensureIndexes();
            managerDao = new SmartHomeManagerDAO(SmarthomeManager.class, datastore);
            userDAO = new UserDAO(User.class, datastore);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /////MANAGER METHODS//////

    /**
     * Write a Manager on MongoDB
     *
     * @param manager a {@link SmarthomeManager} class
     * @return the ObjectId of element on DB
     */
    public static ObjectId writeManager(SmarthomeManager manager) {
        return (ObjectId) managerDao.save(manager).getId();
    }

    /**
     * Get a Manager by ID
     *
     * @param id the string of the key (ObjectId)
     * @return a {@link SmarthomeManager}
     */
    public static SmarthomeManager getManagerById(String id) {
        return managerDao.get(new ObjectId(id));
    }

    /**
     * Get a Manager by Username
     *
     * @param username the username
     * @return a {@link SmarthomeManager}
     */
    public static SmarthomeManager getManagerByUsername(String username) {
        return managerDao.findOne(ISmartHomeManagerDAO.USERNAME, username);
    }

    /**
     * get all Managers
     *
     * @return a list of {@link SmarthomeManager}
     */
    public static List<SmarthomeManager> getAllManagers() {
        return managerDao.find().asList();
    }

    /**
     * Delete all Managers with the same username
     *
     * @param username the username of Manager
     * @return true/false
     */
    public static boolean deleteManager(String username) {
        final Query<SmarthomeManager> query = datastore.createQuery(SmarthomeManager.class)
                .filter(ISmartHomeManagerDAO.USERNAME, username);
        return managerDao.deleteByQuery(query).wasAcknowledged();
    }


    ///////USER METHODS/////////

    /**
     * Write a User on MongoDB
     *
     * @param user a {@link User} class
     * @return the ObjectId of element on DB
     */
    public static ObjectId writeUser(User user) {
        return (ObjectId) userDAO.save(user).getId();
    }

    /**
     * Get the User by Id
     *
     * @param id the string of the key (ObjectId)
     * @return the ObjectId of element on DB
     */
    public static User getUserById(String id) {
        return userDAO.get(new ObjectId(id));
    }

    /**
     * Get all users
     *
     * @return a list of {@link User}
     */
    public static List<User> getAllUsers() {
        return userDAO.find().asList();
    }

    /**
     * Delete of users with the same username
     *
     * @param username a string of username
     * @return true/false
     */
    public static boolean deleteUser(String username) {
        final Query<User> query = datastore.createQuery(User.class)
                .filter(IUserDAO.USERNAME, username);
        return userDAO.deleteByQuery(query).wasAcknowledged();
    }

    /**
     * Get user
     * Return the ObjectId of element on DB
     *
     * @param username a sring of username
     * @return the {@link User} class
     */
    public static User getUserByUsername(String username) {
        final Query<User> query = datastore.createQuery(User.class).filter(IUserDAO.USERNAME, username);
        return userDAO.findOne(query);
    }

    /**
     * Check if exist a user with the username and password on DB
     *
     * @param username a sring of username
     * @param password a sring of password
     * @return true/false
     */
    public static boolean checkUserByUserAndPass(String username, String password) {
        final Query<User> query = datastore.createQuery(User.class).filter(IUserDAO.USERNAME, username)
                .filter(IUserDAO.PASS, password);
        return userDAO.exists(query);
    }

    /**
     * Check if exist the email on DB
     *
     * @param email the string of email
     * @return true/false
     */
    public static boolean mailPresent(String email) {
        return userDAO.exists(IUserDAO.EMAIL, email);
    }

    /**
     * Update a fild of user
     *
     * @param username the string of username
     * @param field    the field that you want change
     * @param value    the new value of the field
     * @return true/false
     */
    public static boolean updateFieldOfUser(String username, String field, String value) {
        final UpdateOperations<User> op = datastore.createUpdateOperations(User.class).set(field, value);
        final Query<User> query = datastore.createQuery(User.class).filter(IUserDAO.USERNAME, username);
        return userDAO.updateFirst(query, op).getUpdatedExisting();

    }


}
