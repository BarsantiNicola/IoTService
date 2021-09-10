package db.interfaces;

import iot.SmarthomeManager;
import iot.User;
import statistics.Statistic;
import statistics.Statistics;

import javax.ejb.Remote;
import java.util.Date;
import java.util.List;

@Remote
public interface DBinterface {

    boolean connectDB();
    boolean login( String email, String password );
    boolean addUser(User user);
    boolean emailPresent( String email );

    String getUserFirstName( String email );
    String getUserLastName( String email );

    String generateNewSmartID();
    SmarthomeManager getSmarthome( String email );
    boolean changePassword( String email, String new_password );
    List<Statistic> getStatistics(String dID, String action, Date startTime, Date endTime );

}
