package db.beans;

import db.interfaces.DBinterface;
import iot.SmarthomeManager;
import statistics.Statistic;
import statistics.Statistics;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

@Stateless
public class databaseConnector implements DBinterface {

    @Override
    public boolean login(String username, String password) {
        return true;
    }

    @Override
    public boolean addUser(String name, String surname, String email, String password) {
        return true;
    }

    @Override
    public boolean emailPresent(String email) {
        return false;
    }

    @Override
    public SmarthomeManager getSmarthome(String username) {

        return null;
    }

    @Override
    public List<Statistic> getStatistics(String dID, String action, Date startTime, Date endTime ){

        return Statistics.buildTestEnvironment();

    }


    //////  TODO GET FIRST NAME AND LAST NAME WILL CALLED TOGETHER, IF U WANT U CAN COMBINE THEM IN A UNIQUE FUNCTION
    @Override
    public String getUserFirstName( String username ){
        return "testFirstName";
    }

    @Override
    public String getUserLastName( String username ){
        return "testLastName";
    }

    @Override
    public String generateNewSmartID() {
        return "0";
    }

    //////

    @Override
    public boolean changePassword(String username, String new_password) {

        return true;
    }
}
