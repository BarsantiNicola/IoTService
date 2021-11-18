package db.mongoConnector;

import config.interfaces.IConfiguration;

import db.model.Operation;
import db.model.Statistic;
import db.model.User;
import iot.*;
import org.bson.types.ObjectId;


import rabbit.msg.DeviceUpdate;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * This class have all methods for the access of DB
 * <p>
 * Before to call a methods you must use the method connectDB that connect the db
 * This method must be called only one time on the lifetime of the program
 */
public class MongoClientProvider {

    private StatisticsProvider statistics = null;
    private UserProvider users = null;
    private SmarthomeProvider smarthomes = null;

    /**
     * Open DB connection
     */
    public MongoClientProvider( IConfiguration configuration ) {

        try {

            Properties properties = configuration.getConfiguration("db");

            if( statistics == null ) {

                statistics = new StatisticsProvider(properties);
                users = new UserProvider(properties);
                smarthomes = new SmarthomeProvider(properties);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /////MANAGER METHODS//////

    /**
     * Write a Manager on MongoDB
     *
     * @param manager a {@link SmarthomeManager} class
     * @return the ObjectId of element on DB
     */
    public boolean addManager(SmarthomeManager manager) {
        return this.smarthomes.addSmarthome( manager );
    }

    public void addOperation(Operation operation){
        this.statistics.writeOperation(operation);
    }

    public void removeAllStatistics(String dID){
        this.statistics.removeAllStatistics(dID);
    }

    public SmarthomeManager getHomeManager( String username ){
        return this.smarthomes.getSmarthome( username );
    }

    public boolean updateManager( SmarthomeManager manager ){
        return this.smarthomes.updateSmarthome( manager );
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
    public boolean renameElementManager(String username, DeviceUpdate.UpdateType op, String oldName, String newName,
                                         String location) {
        SmarthomeManager manager = this.smarthomes.getSmarthome( username );
        assert manager != null;

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
                return false;
        }
        return this.smarthomes.updateSmarthome(manager);
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
    @SuppressWarnings( "unused" )
    public boolean moveDevice(String username, String location, String sublocation, String newSublocation, String device){

        SmarthomeManager manager = this.smarthomes.getSmarthome( username );
        if (manager == null) {
            return false;
        }

        manager.addSmartHomeMutex(new Semaphore(1));
        String name  = manager.giveDeviceNameById(device);
        manager.changeDeviceSubLocation(location, name, newSublocation, false);
        return this.smarthomes.updateSmarthome(manager);
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
     * @param device_type The device type {@link DeviceType}
     * @return a {@link ObjectId}
     */
    public boolean addElementManager(String username, DeviceUpdate.UpdateType op, String id, String location,
                                      String address, int port, String subLocation, String device,
                                      DeviceType device_type) {
        SmarthomeManager manager = this.smarthomes.getSmarthome( username );
        if (manager == null) {
            return false;
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
                return false;
        }
        return this.smarthomes.updateSmarthome(manager);
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
    public boolean removeElementIntoManager(String username, DeviceUpdate.UpdateType type, String removeName, String location) {
        SmarthomeManager manager = this.smarthomes.getSmarthome( username );
        if (manager == null) {
            return false;
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
                return false;
        }
        return this.smarthomes.updateSmarthome(manager);
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
    public boolean performAction(String username, String device, String action, String value) {
        SmarthomeManager manager = this.smarthomes.getSmarthome( username );
        if (manager == null) {
            return false;
        }

        manager.addSmartHomeMutex(new Semaphore(1));
        manager.performAction(manager.giveDeviceNameById(device), action, value, new Date(), false);

        return this.smarthomes.updateSmarthome(manager);
    }


    /**
     * Get a Manager by Username
     *
     * @param username the username
     * @return a {@link SmarthomeManager}
     */
    public SmarthomeManager getManagerByUsername(String username) {
        SmarthomeManager manager = this.smarthomes.getSmarthome( username );
        manager.relink();
        return manager;
    }



    ///////USER METHODS/////////

    /**
     * Write a User on MongoDB
     *
     * @param user a {@link User} class
     * @return the ObjectId of element on DB
     */
    public boolean writeUser(User user) {
        return this.users.addUser( user );
    }

    /**
     * Check if exist a user with the username and password on DB
     *
     * @param username a sring of username
     * @param password a sring of password
     * @return true/false
     */
    public boolean checkUserByUserAndPass(String username, String password) {
        return this.users.login( username, password );
    }

    /**
     * Check if exist the email on DB
     *
     * @param email the string of email
     * @return true/false
     */
    public boolean mailPresent(String email) {
        return this.users.emailPresent( email );
    }

    public String[] getFirstAndLastName( String username ){
        return this.users.getUserFirstAndLastName( username );
    }

    public boolean changePassword( String username, String password ){
        return this.users.changePassword( username, password );
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
     * @return The {@link Statistic} that it contains a sorted list of {@link Statistic}
     */
    public List<Statistic> getStatistics(String dID, DeviceType type, String stat_name, Date startTime, Date endTime) {

        return this.statistics.getStatistic(stat_name, dID, type, startTime, endTime);

    }


    public void close(){

        this.smarthomes.close();
        this.users.close();
        this.statistics.close();

    }

}
