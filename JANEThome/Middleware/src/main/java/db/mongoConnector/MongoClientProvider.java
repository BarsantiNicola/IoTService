package db.mongoConnector;

import com.mongodb.*;
import config.beans.Configuration;
import config.interfaces.ConfigurationInterface;
import db.dao.SmartHomeManagerDAO;
import db.dao.UserDAO;
import db.interfaces.ISmartHomeManagerDAO;
import db.interfaces.IUserDAO;
import iot.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import static org.mongodb.morphia.aggregation.Group.grouping;

import rabbit.msg.DeviceUpdate;
import iot.Statistic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.concurrent.Semaphore;

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
    private static StatisticsProvider statistics;
    private static UserDAO userDAO;
    private transient Logger logger;

    /**
     * Open DB connection
     */
    public MongoClientProvider(ConfigurationInterface configuration) {
        this.logger = LogManager.getLogger(getClass());
        init(configuration);
    }

    private void init(ConfigurationInterface configuration) {
        try {
            Properties properties = configuration.getConfiguration("db");

            mc = new MongoClient(properties.getProperty("hostname"), Integer.parseInt(properties.getProperty("port")));
            morphia = new Morphia();
            morphia.map(SmarthomeManager.class);
            morphia.map(User.class);
            datastore = morphia.createDatastore(mc, properties.getProperty("db_name"));
            datastore.ensureIndexes();
            managerDao = new SmartHomeManagerDAO(SmarthomeManager.class, datastore);
            userDAO = new UserDAO(User.class, datastore);
            statistics = new StatisticsProvider( properties );

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

    public void writeOperation(Operation operation){
        MongoClientProvider.statistics.writeOperation(operation);
    }

    public void removeAllStatistics(String dID){
        MongoClientProvider.statistics.removeAllStatistics(dID);
    }

    /**
     * Update element on Manager
     *
     * @param username the username of manager
     * @param field    the field to change
     * @param value    the value of field
     * @return a {@link Boolean}
     */
    public boolean updateManager(String username, String field, String value) {
        final Query<SmarthomeManager> query = datastore.createQuery(SmarthomeManager.class).filter(ISmartHomeManagerDAO.USERNAME, username);
        UpdateOperations<SmarthomeManager> ops = datastore.createUpdateOperations(SmarthomeManager.class).set(field, value);
        return datastore.update(query, ops).getUpdatedExisting();
    }

    /**
     * Rename element on Manager
     *
     * @param username the username of manager
     * @param op       It is the operation to perform (see {@link DeviceUpdate.UpdateType})
     * @param oldName  old name of element
     * @param newName  new name of element
     * @param location name of location if want change sub location name
     * @return a {@link ObjectId}
     */
    public ObjectId renameElementManager(String username, DeviceUpdate.UpdateType op, String oldName, String newName,
                                         String location) {
        SmarthomeManager manager = getManagerByUser(username);
        assert manager != null;
        manager.addSmartHomeMutex(new Semaphore(1));
        switch (op) {
            case RENAME_LOCATION:
                manager.changeLocationName(oldName, newName, false);
                break;

            case RENAME_DEVICE:
                manager.changeDeviceName(oldName, newName, false);
                break;

            case RENAME_SUB_LOCATION:
                manager.changeSublocationName(location, oldName, newName, false);
                break;

            default:
                return null;
        }
        return writeManager(manager);
    }

    /**
     * Move a device from a sub location to the other
     *
     * @param username    the username of manager
     * @param location    The location name
     * @param sublocation The sublocation name
     * @param device      new name of device
     * @return a {@link ObjectId}
     */
    public ObjectId moveDevice(String username, String location, String sublocation,
                               String device) {
        SmarthomeManager manager = managerDao.findOne(ISmartHomeManagerDAO.USERNAME, username);
        if (manager == null) {
            return null;
        }
        manager.addSmartHomeMutex(new Semaphore(1));
        manager.changeDeviceSubLocation(location, sublocation, device, false);
        return writeManager(manager);
    }

    /**
     * Add element on Manager
     *
     * @param username    The username of manager
     * @param op          It is the operation to perform (see {@link DeviceUpdate.UpdateType})
     * @param id          The id of element
     * @param location    The location name
     * @param address     The address of element
     * @param port        The port of element
     * @param subLocation The subLocation name
     * @param device      The device name
     * @param device_type The device type {@link SmarthomeDevice.DeviceType}
     * @return a {@link ObjectId}
     */
    public ObjectId addElementManager(String username, DeviceUpdate.UpdateType op, String id, String location,
                                      String address, int port, String subLocation, String device,
                                      SmarthomeDevice.DeviceType device_type) {
        SmarthomeManager manager = getManagerByUser(username);
        if (manager == null) {
            return null;
        }
        manager.addSmartHomeMutex(new Semaphore(1));
        switch (op) {
            case ADD_LOCATION:
                manager.addLocation(location, id, address, port, false);
                break;

            case ADD_DEVICE:
                manager.addDevice(location, subLocation, id, device, device_type, false);
                break;

            case ADD_SUB_LOCATION:
                manager.giveNextSublocID(location);
                manager.addSubLocation(location, subLocation, id, false);
                break;

            default:
                return null;
        }
        return writeManager(manager);
    }

    /**
     * Rename element on Manager
     *
     * @param username   The username of manager
     * @param type       It is the operation to perform (see {@link DeviceUpdate.UpdateType})
     * @param removeName The name of element to remove
     * @param location   The name of Lacation where find the subLocation
     * @return a {@link ObjectId}
     */
    public ObjectId removeElementIntoManager(String username, DeviceUpdate.UpdateType type, String removeName, String location) {
        SmarthomeManager manager = getManagerByUser(username);
        if (manager == null) {
            return null;
        }
        manager.addSmartHomeMutex(new Semaphore(1));
        switch (type) {
            case REMOVE_LOCATION:
                manager.removeLocation(removeName, false);
                break;

            case REMOVE_DEVICE:
                manager.removeDevice(removeName, false);
                break;

            case REMOVE_SUB_LOCATION:
                manager.removeSublocation(removeName, location, false);  //  TODO parametri invertiti, hot fix con scambio dei parametri
                break;

            default:
                return null;
        }
        return writeManager(manager);
    }

    /**
     * add a action on device
     *
     * @param username the username of manager
     * @param device   the name of the device
     * @param action   the action to perform
     * @param value    the value of field
     * @return a {@link ObjectId}
     */
    public ObjectId performAction(String username, String device, String action, String value) {
        SmarthomeManager manager = getManagerByUser(username);
        if (manager == null) {
            return null;
        }

        manager.addSmartHomeMutex(new Semaphore(1));
        manager.performAction(manager.giveDeviceNameById(device), action, value, new Date(), false);

        return writeManager(manager);
    }

    /**
     * Get a Manager by ID
     *
     * @param id the string of the key (ObjectId)
     * @return a {@link SmarthomeManager}
     */
    public SmarthomeManager getManagerById(String id) {
        SmarthomeManager manager = managerDao.get(new ObjectId(id));
        manager.relink();
        return manager;
    }

    /**
     * Get a Manager by Username
     *
     * @param username the username
     * @return a {@link SmarthomeManager}
     */
    public SmarthomeManager getManagerByUsername(String username) {
        SmarthomeManager manager = managerDao.findOne(ISmartHomeManagerDAO.USERNAME, username);
        manager.relink();
        return manager;
    }

    /**
     * get all Managers
     *
     * @return a list of {@link SmarthomeManager}
     */
    public List<SmarthomeManager> getAllManagers() {
        List<SmarthomeManager> managers = managerDao.find().asList();
        managers.forEach(SmarthomeManager::relink);
        return managers;
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

    /**
     * Delete a manager by the ObjectID
     *
     * @param objectId the id of Manager
     * @return true/false
     */
    public boolean deleteManager(ObjectId objectId) {
        return managerDao.deleteById(objectId).wasAcknowledged();
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
     * @param stat_name    the action that do you want the statistics
     * @param startTime start time of range
     * @param endTime   end time of range
     * @return The {@link List<Statistic>} that it contains a sorted list of {@link Statistic}
     */
    public List<Statistic> getStatistics(String dID, SmarthomeDevice.DeviceType type, String stat_name, Date startTime, Date endTime) {

        return MongoClientProvider.statistics.getStatistic(stat_name, dID, type, startTime, endTime);
        /*
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
        return statistics;*/
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

    private SmarthomeManager getManagerByUser(String mail) {
        final Query<User> query = datastore.createQuery(User.class).filter(IUserDAO.EMAIL, mail);
        User user = userDAO.findOne(query);
        SmarthomeManager manager = user.getHomeManager();
        if (manager == null) {
            return null;
        }
        manager.relink();
        return manager;
    }

}
