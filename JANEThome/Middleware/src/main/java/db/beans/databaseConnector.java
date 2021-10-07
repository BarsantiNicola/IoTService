package db.beans;

import db.interfaces.DBinterface;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.SmarthomeManager;
import iot.User;
import statistics.Statistics;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.Date;

@Stateless
public class databaseConnector implements DBinterface {
    MongoClientProvider mongoClientProvider;

    @Override
    @PostConstruct
    public void connectDB() {
        mongoClientProvider = new MongoClientProvider();
    }

    @Override
    public boolean login(String username, String password) {
        return mongoClientProvider.checkUserByUserAndPass(username, password);
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
        return mongoClientProvider.getStatistics(dID,action,startTime,endTime);

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
