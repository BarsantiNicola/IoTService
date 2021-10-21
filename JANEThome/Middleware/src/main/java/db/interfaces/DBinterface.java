package db.interfaces;

import iot.SmarthomeDevice;
import iot.SmarthomeManager;
import iot.User;
import org.bson.types.ObjectId;
import rabbit.msg.DeviceUpdate;
import statistics.Statistics;

import javax.ejb.Remote;
import java.util.Date;

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
                               SmarthomeDevice.DeviceType device_type);

    ObjectId moveDevice(String username, String location, String sublocation, String device);

    boolean addUser(User user);

    boolean deleteManager(ObjectId objectId);

    boolean emailPresent(String email);

    String[] getUserFirstAndLastName(String email);

    SmarthomeManager getSmarthome(String email);

    boolean changePassword(String email, String new_password);

    Statistics getStatistics(String dID, String action, Date startTime, Date endTime);

}
