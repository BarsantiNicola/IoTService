package db.beans;

import config.interfaces.ConfigurationInterface;
import db.interfaces.DBinterface;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.SmarthomeDevice;
import iot.SmarthomeManager;
import iot.User;
import org.bson.types.ObjectId;
import rabbit.msg.DeviceUpdate;
import statistics.Statistics;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Date;


@Stateless
public class databaseConnector implements DBinterface {
    MongoClientProvider mongoClientProvider;

    @EJB
    private ConfigurationInterface configuration;   //  gives the configuration for the rest interface


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
                                      SmarthomeDevice.DeviceType device_type) {
        return mongoClientProvider.addElementManager(username, type, id, location, address, port, subLocation,
                device, device_type);
    }

    @Override
    public ObjectId moveDevice(String username, String location, String sublocation, String device) {
        return mongoClientProvider.moveDevice(username, location, sublocation, device);
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
        return mongoClientProvider.mailPresent(email);
    }

    @Override
    public SmarthomeManager getSmarthome(String username) {
        return mongoClientProvider.getUserByUsername(username).getHomeManager();
    }

    @Override
    public Statistics getStatistics(String dID, String action, Date startTime, Date endTime) {
        return mongoClientProvider.getStatistics(dID, action, startTime, endTime);

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
