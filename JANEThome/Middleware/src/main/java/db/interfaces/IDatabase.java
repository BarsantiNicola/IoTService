package db.interfaces;

import db.model.Operation;
import db.model.User;
import db.model.Statistic;
import iot.*;
import org.bson.types.ObjectId;
import rabbit.msg.DeviceUpdate;

import javax.ejb.Remote;
import java.util.Date;
import java.util.List;

@Remote
public interface IDatabase {

    void connectDB();

    boolean login(String email, String password);

    boolean addManager(SmarthomeManager manager);

    boolean updateManager(SmarthomeManager manager);

    boolean renameElementManager(String username, DeviceUpdate.UpdateType op, String oldName, String newName,
                                  String location);

    boolean performAction(String username, String device, String action, String value);

    boolean removeElementIntoManager(String username, DeviceUpdate.UpdateType type,
                                      String location, String subLocation);

    boolean addElementManager(String username, DeviceUpdate.UpdateType type, String id, String location,
                               String address, int port, String subLocation, String device,
                               DeviceType device_type);

    boolean moveDevice(String username, String location, String sublocation, String newSublocation, String device);

    void addOperation(Operation operation);

    boolean addUser(User user);

    boolean emailPresent(String email);

    String[] getUserFirstAndLastName(String email);

    SmarthomeManager getSmarthome(String email);

    boolean changePassword(String email, String new_password);

    List<Statistic> getStatistics(String dID, DeviceType type, String action, Date startTime, Date endTime);

    void removeAllStatistics(String dID);
}
