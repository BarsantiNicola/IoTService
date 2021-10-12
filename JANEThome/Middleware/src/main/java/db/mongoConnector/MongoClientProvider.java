package db.mongoConnector;

import com.mongodb.*;
import config.beans.Configuration;
import config.interfaces.ConfigurationInterface;
import db.dao.SmartHomeManagerDAO;
import db.dao.UserDAO;
import db.interfaces.IGenericDao;
import db.interfaces.ISmartHomeManagerDAO;
import db.interfaces.IUserDAO;
import iot.Action;
import iot.SmarthomeManager;
import iot.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.aggregation.*;

import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.aggregation.Group.*;

import statistics.Statistic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import statistics.Statistics;

import javax.ejb.EJB;
import java.util.*;

/**
 * This class have all methods for the access of DB
 * <p>
 * Before to call a methods you must use the method connectDB that connect the db
 * This method must be called only one time on the lifetime of the program
 */
public class MongoClientProvider {
    private static MongoClient mc;
    private static Morphia morphia;
    private static Datastore datastore;
    private static SmartHomeManagerDAO managerDao;
    private static UserDAO userDAO;
    private transient Logger logger;

    @EJB
    ConfigurationInterface configuration;   //  gives the configuration for the rest interface

    /**
     * Open DB connection
     */
    public MongoClientProvider() {
        this.logger = LogManager.getLogger(getClass());
        init();
    }

    //TODO: vedi per conf
    private void init() {
        try {
            String db, hostname;
            Integer port;

//            configuration = new Configuration();
//            Map<String,String> conf = configuration.getConfiguration("db");
//            hostname = conf.get("hostname");
//            port = Integer.parseInt(conf.get("port"));
//            db =  conf.get("db_name");

//            mc = new MongoClient(new MongoClientURI(DB_HOST));

            hostname = IGenericDao.DB_HOST;
            port = IGenericDao.DB_PORT;
            db = IGenericDao.DB_NAME;

            mc = new MongoClient(hostname, port);
            morphia = new Morphia();
            morphia.map(SmarthomeManager.class);
            morphia.map(User.class);
            datastore = morphia.createDatastore(mc, db);
            datastore.ensureIndexes();
            managerDao = new SmartHomeManagerDAO(SmarthomeManager.class, datastore);
            userDAO = new UserDAO(User.class, datastore);
        } catch (Exception e) {
            logger.error(e);
        }
    }


    /**
     * Method to test connection: Open DB connection
     */
    public MongoClientProvider(Configuration configuration) {
        try {
            this.logger = LogManager.getLogger(getClass());
            Properties conf = configuration.getConfiguration("db");
//            mc = new MongoClient(new MongoClientURI(DB_HOST));
            mc = new MongoClient(conf.getProperty("hostname"), Integer.parseInt(conf.getProperty("port")));
            morphia = new Morphia();
            morphia.map(SmarthomeManager.class);
            morphia.map(User.class);
            datastore = morphia.createDatastore(mc, conf.getProperty("db_name"));
            datastore.ensureIndexes();
            managerDao = new SmartHomeManagerDAO(SmarthomeManager.class, datastore);
            userDAO = new UserDAO(User.class, datastore);
        } catch (Exception e) {
            assert logger != null;
            logger.error(e);
        }
    }


    /////MANAGER METHODS//////

    /**
     * Write a Manager on MongoDB
     *
     * @param manager a {@link SmarthomeManager} class
     * @return the ObjectId of element on DB
     */
    public ObjectId writeManager(SmarthomeManager manager) {
        return (ObjectId) managerDao.save(manager).getId();
    }

    /**
     * Get a Manager by ID
     *
     * @param id the string of the key (ObjectId)
     * @return a {@link SmarthomeManager}
     */
    public SmarthomeManager getManagerById(String id) {
        return managerDao.get(new ObjectId(id));
    }

    /**
     * Get a Manager by Username
     *
     * @param username the username
     * @return a {@link SmarthomeManager}
     */
    public SmarthomeManager getManagerByUsername(String username) {
        return managerDao.findOne(ISmartHomeManagerDAO.USERNAME, username);
    }

    /**
     * get all Managers
     *
     * @return a list of {@link SmarthomeManager}
     */
    public List<SmarthomeManager> getAllManagers() {
        return managerDao.find().asList();
    }

    /**
     * Delete all Managers with the same username
     *
     * @param username the username of Manager
     * @return true/false
     */
    public boolean deleteManager(String username) {
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
    public ObjectId writeUser(User user) {
        return (ObjectId) userDAO.save(user).getId();
    }

    /**
     * Get the User by Id
     *
     * @param id the string of the key (ObjectId)
     * @return the ObjectId of element on DB
     */
    public User getUserById(String id) {
        return userDAO.get(new ObjectId(id));
    }

    /**
     * Get all users
     *
     * @return a list of {@link User}
     */
    public List<User> getAllUsers() {
        return userDAO.find().asList();
    }

    /**
     * Delete of users with the same username
     *
     * @param username a string of username
     * @return true/false
     */
    public boolean deleteUser(String username) {
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
    public User getUserByUsername(String username) {
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
    public boolean checkUserByUserAndPass(String username, String password) {
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
    public boolean updateFieldOfUser(String username, String field, String value) {
        final UpdateOperations<User> op = datastore.createUpdateOperations(User.class).set(field, value);
        final Query<User> query = datastore.createQuery(User.class).filter(IUserDAO.USERNAME, username);
        return userDAO.updateFirst(query, op).getUpdatedExisting();

    }

    /////////STATISTICS////////////

    /**
     * Return the statistics of a device by action and date range.
     * If the action is Action.ONOFF, Action.OPENCLOSE or Action.LOCKUNLOCK for every statistic it will return 3 samples
     * shifted by 5 second( {@link Action} )
     *
     * @param dID       the id of the device
     * @param action    the action that do you want the statistics
     * @param startTime start time of range
     * @param endTime   end time of range
     * @return The {@link Statistics} that it contains a sorted list of {@link Statistic}
     */
    public Statistics getStatistics(String dID, String action, Date startTime, Date endTime) {
        Statistics statistics = new Statistics();
        Statistic tempStat;
        Statistic tempStatPre;
        Statistic tempStatpost;

        try {
            Query<SmarthomeManager> query = datastore.getQueryFactory().createQuery(datastore);
            query.field("_id").greaterThanOrEq(startTime);
            query.field("_id").lessThanOrEq(endTime);
            query.field("values.action").equal(action);

            Projection devicesProjection = Projection.expression("devices", new BasicDBObject("$objectToArray", "$devices"));
            Projection operationsProjection = Projection.expression("operations", new BasicDBObject("$objectToArray", "$devices.v.historical"));
            Projection yProjection = Projection.expression("y", new BasicDBObject("$arrayElemAt", new Object[]{"$values.value", 0}));

            Iterator<Statistic> aggregate = datastore.createAggregation(SmarthomeManager.class)
                    .project(devicesProjection)
                    .unwind("devices")
                    .match(datastore.getQueryFactory().createQuery(datastore).field("devices.k").equal(dID))
                    .project(operationsProjection)
                    .unwind("operations")
                    .group("operations.v.date", grouping("values", addToSet("operations.v")))
                    .match(query)
                    .project(Projection.projection("_id").suppress(), Projection.projection("x", "_id"), yProjection)
                    .out(Statistic.class);

            while (aggregate.hasNext()) {
                tempStat = aggregate.next();
                if (action.matches(Action.ONOFF + "|" + Action.OPENCLOSE + "|" + Action.LOCKUNLOCK)) {
                    tempStatPre = new Statistic(shiftDateBackwards(tempStat.getX()), "0");
                    tempStatpost = new Statistic(shiftDateForward(tempStat.getX()), "0");
                    statistics.addStatistic(tempStatPre);
                    statistics.addStatistic(tempStatpost);
                }
                statistics.addStatistic(tempStat);
            }
            return statistics;
        } catch (Exception e) {
            logger.error(e);
        }
        return statistics;
    }

    private Date shiftDateForward(Date d) {
        Calendar calendar = dateToCalendar(d);
        calendar.add(Calendar.SECOND, 5);
        return calendar.getTime();
    }

    private Date shiftDateBackwards(Date d) {
        Calendar calendar = dateToCalendar(d);
        calendar.add(Calendar.SECOND, -5);
        return calendar.getTime();
    }

    //Convert Date to Calendar
    private Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }
}
