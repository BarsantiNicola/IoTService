package db.interfaces;

import iot.*;
import org.bson.types.ObjectId;
import rabbit.msg.DeviceUpdate;

import javax.ejb.Remote;
import java.util.Date;
import java.util.List;

@Remote
public interface DBinterface {

    void connectDB();

    boolean login(String email, String password);

    ObjectId addManager(SmarthomeManager manager);

    boolean updateManager(String username, String field, String value);

    ObjectId renameElementManager(String username, DeviceUpdate.UpdateType op, String oldName, String newName,
                                  String location);

    ObjectId performAction(String username, String device, String action, String value);

    ObjectId removeElementIntoManager(String username, DeviceUpdate.UpdateType type,
                                      String location, String subLocation);

    ObjectId addElementManager(String username, DeviceUpdate.UpdateType type, String id, String location,
                               String address, int port, String subLocation, String device,
                               DeviceType device_type);

    ObjectId moveDevice(String username, String location, String sublocation, String device);

    void addOperation(Operation operation);

    boolean addUser(User user);

    boolean deleteManager(ObjectId objectId);

    boolean emailPresent(String email);

    String[] getUserFirstAndLastName(String email);

    SmarthomeManager getSmarthome(String email);

    boolean changePassword(String email, String new_password);

    List<Statistic> getStatistics(String dID, DeviceType type, String action, Date startTime, Date endTime);

    void removeAllStatistics(String dID);
}
