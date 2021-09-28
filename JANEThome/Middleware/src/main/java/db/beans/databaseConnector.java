package db.beans;

import db.interfaces.DBinterface;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.SmarthomeManager;
import iot.User;
import statistics.Statistic;
import statistics.Statistics;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

@Stateless
public class databaseConnector implements DBinterface {

    @Override
    @PostConstruct
    public void connectDB() {
        MongoClientProvider.connectDB();
    }

    @Override
    public boolean login(String username, String password) {
        return MongoClientProvider.checkUserByUserAndPass(username, password);
    }

    @Override
    public boolean addUser(User user) {
        return MongoClientProvider.writeUser(user) != null;
    }

    @Override
    public boolean emailPresent(String email) {
        return MongoClientProvider.mailPresent(email);
    }

    @Override
    public SmarthomeManager getSmarthome(String username) {
        return MongoClientProvider.getUserByUsername(username).getHomeManager();
    }

    @Override
    public List<Statistic> getStatistics(String dID, String action, Date startTime, Date endTime) {

        return Statistics.buildTestEnvironment();

    }

    @Override
    public String[] getUserFirstAndLastName(String username) {
        User s = MongoClientProvider.getUserByUsername(username);
        return new String[]{s.getFirstName(), s.getLastName()};
    }


    @Override
    public boolean changePassword(String username, String new_password) {
        return MongoClientProvider.updateFieldOfUser(username, IUserDAO.PASS, new_password);
    }
}
