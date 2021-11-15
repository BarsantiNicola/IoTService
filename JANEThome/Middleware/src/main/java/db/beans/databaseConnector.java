package db.beans;

import config.interfaces.IConfiguration;
import db.interfaces.DBinterface;
import db.interfaces.IUserDAO;
import db.model.Operation;
import db.model.User;
import db.mongoConnector.MongoClientProvider;
import db.model.Statistic;
import iot.*;
import org.bson.types.ObjectId;
import rabbit.msg.DeviceUpdate;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;


@Stateless
public class databaseConnector implements DBinterface {
    MongoClientProvider mongoClientProvider;

    @EJB
    private IConfiguration configuration;   //  gives the configuration for the rest interface


    @Override
    @PostConstruct
    public void connectDB() {
        mongoClientProvider = new MongoClientProvider(configuration);
    }

    @Override
    public boolean login(String username, String password) {
        return mongoClientProvider.checkUserByUserAndPass(username, password);
    }

    @Override
    public ObjectId addManager(SmarthomeManager manager) {
        return mongoClientProvider.writeManager(manager);
    }


    @Override
    public boolean updateManager(String username, String field, String value) {
        return mongoClientProvider.updateManager(username, field, value);
    }

    @Override
    public boolean deleteManager(ObjectId objectId){
        return mongoClientProvider.deleteManager(objectId);
    }

    @Override
    public ObjectId renameElementManager(String username, DeviceUpdate.UpdateType op, String oldName, String newName, String location) {
        return mongoClientProvider.renameElementManager(username, op, oldName, newName, location);
    }

    @Override
    public ObjectId performAction(String username, String device, String action, String value) {
        return mongoClientProvider.performAction(username, device, action, value);
    }

    @Override
    public ObjectId addElementManager(String username, DeviceUpdate.UpdateType type, String id, String location,
                                      String address, int port, String subLocation, String device,
                                      DeviceType device_type) {
        return mongoClientProvider.addElementManager(username, type, id, location, address, port, subLocation,
                device, device_type);
    }

    @Override
    public ObjectId moveDevice(String username, String location, String sublocation, String device) {
        return mongoClientProvider.moveDevice(username, location, sublocation, device);
    }

    @Override
    public void addOperation(Operation operation) {
        mongoClientProvider.writeOperation( operation );
    }

    @Override
    public ObjectId removeElementIntoManager(String username, DeviceUpdate.UpdateType type,
                                             String location, String subLocation) {
        return mongoClientProvider.removeElementIntoManager(username, type, location, subLocation);
    }

    @Override
    public boolean addUser(User user) {
        return mongoClientProvider.writeUser(user) != null;
    }

    @Override
    public boolean emailPresent(String email) {
        return MongoClientProvider.mailPresent(email);
    }

    @Override
    public SmarthomeManager getSmarthome(String username) {
        return mongoClientProvider.getUserByUsername(username).getHomeManager();
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
    public String[] getUserFirstAndLastName(String username) {
        User s = mongoClientProvider.getUserByUsername(username);
        return new String[]{s.getFirstName(), s.getLastName()};
    }


    @Override
    public boolean changePassword(String username, String new_password) {
        return mongoClientProvider.updateFieldOfUser(username, IUserDAO.PASS, new_password);
    }
}
