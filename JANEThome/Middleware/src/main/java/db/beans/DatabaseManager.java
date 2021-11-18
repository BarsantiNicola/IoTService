package db.beans;

import config.interfaces.IConfiguration;
import db.interfaces.IDatabase;
import db.model.Operation;
import db.model.User;
import db.mongoConnector.MongoClientProvider;
import db.model.Statistic;
import iot.*;
import rabbit.msg.DeviceUpdate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;


@Stateless
public class DatabaseManager implements IDatabase {

    MongoClientProvider mongoClientProvider;

    @EJB
    private IConfiguration configuration;   //  gives the configuration for the rest interface

    @Override
    @PostConstruct
    public void connectDB(){

        mongoClientProvider = new MongoClientProvider( configuration );

    }

    @PreDestroy
    public void close(){ this.mongoClientProvider.close(); }

    @Override
    public boolean login(String username, String password) {
        return mongoClientProvider.checkUserByUserAndPass(username, password);
    }

    @Override
    public boolean addManager(SmarthomeManager manager) {
        return mongoClientProvider.addManager(manager);
    }


    @Override
    public boolean updateManager(SmarthomeManager manager) {
        return mongoClientProvider.updateManager( manager );
    }

    @Override
    public boolean renameElementManager(String username, DeviceUpdate.UpdateType op, String oldName, String newName, String location) {
        return mongoClientProvider.renameElementManager(username, op, oldName, newName, location);
    }

    @Override
    public boolean performAction(String username, String device, String action, String value) {
        return mongoClientProvider.performAction(username, device, action, value);
    }

    @Override
    public boolean addElementManager(String username, DeviceUpdate.UpdateType type, String id, String location,
                                      String address, int port, String subLocation, String device,
                                      DeviceType device_type) {
        return mongoClientProvider.addElementManager(username, type, id, location, address, port, subLocation,
                device, device_type);
    }

    @Override
    public boolean moveDevice(String username, String location, String sublocation, String newSublocation, String device) {
        return mongoClientProvider.moveDevice(username, location, sublocation, newSublocation, device);
    }

    @Override
    public void addOperation(Operation operation) {
        mongoClientProvider.addOperation( operation );
    }

    @Override
    public boolean removeElementIntoManager(String username, DeviceUpdate.UpdateType type,
                                             String location, String subLocation) {
        return mongoClientProvider.removeElementIntoManager(username, type, location, subLocation);
    }

    @Override
    public boolean addUser(User user) {
        return mongoClientProvider.writeUser(user);
    }

    @Override
    public boolean emailPresent(String email) {
        return mongoClientProvider.mailPresent(email);
    }

    @Override
    public SmarthomeManager getSmarthome(String username) {
        return this.mongoClientProvider.getHomeManager( username );
    }

    @Override
    public List<Statistic> getStatistics(String dID, DeviceType type, String action, Date startTime, Date endTime) {
        return mongoClientProvider.getStatistics(dID, type, action, startTime, endTime);

    }

    @Override
    public void removeAllStatistics(String dID) {
        mongoClientProvider.removeAllStatistics(dID);
    }

    @Override
    public String[] getUserFirstAndLastName (String username ) {
        return mongoClientProvider.getFirstAndLastName(username);
    }


    @Override
    public boolean changePassword(String username, String new_password) {
        return mongoClientProvider.changePassword( username, new_password );
    }
}
