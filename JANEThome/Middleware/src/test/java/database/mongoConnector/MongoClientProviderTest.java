package database.mongoConnector;

import config.beans.Configuration;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class MongoClientProviderTest {
    private static SmarthomeManager manager;
    private static User user;

    @BeforeAll
    static void setUp() {
        Configuration configuration = new Configuration();
        user = new User("pluto", "federico", "lapenna", "f.lapenna@studenti.unipi.it", "test");
        manager = SmarthomeManager.createTestingEnvironment("test", true, configuration);
        initStatisticsTest(SmarthomeDevice.DeviceType.DOOR);
        initStatisticsTest(SmarthomeDevice.DeviceType.FAN);
        initStatisticsTest(SmarthomeDevice.DeviceType.THERMOSTAT);
        initStatisticsTest(SmarthomeDevice.DeviceType.CONDITIONER);

        user.setHomeManager(manager);

        MongoClientProvider.connectDB();
    }

    @Test
    void testDeleteManager() {
        MongoClientProvider.writeManager(manager);
        assertTrue(MongoClientProvider.deleteManager(manager.getUsername()));
    }

    @Test
    void testConnectDB() {
        assertTrue(MongoClientProvider.connectDB());
    }

    @Test
    void testWriteManager() {
        assertNotNull(MongoClientProvider.writeManager(manager));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetManagerById() {
        MongoClientProvider.writeManager(manager);
        assertNotNull(MongoClientProvider.getManagerById(manager.getKey().toString()));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetManagerByUsername() {
        MongoClientProvider.writeManager(manager);
        assertNotNull(MongoClientProvider.getManagerByUsername(manager.getUsername()));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetAllManager() {
        MongoClientProvider.writeManager(manager);
        manager.setKey(new ObjectId());
        MongoClientProvider.writeManager(manager);
        List<SmarthomeManager> ms;
        ms = MongoClientProvider.getAllManagers();
        assertTrue(ms.size() >= 2);
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testdeleteUser() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.deleteUser(user.getUsername()));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testWriteUser() {
        MongoClientProvider.writeManager(manager);
        assertNotNull(MongoClientProvider.writeUser(user));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void getUserById() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertNotNull(MongoClientProvider.getUserById(user.getKey().toString()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetAllUsers() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        manager.setKey(new ObjectId());
        user.setKey(new ObjectId());
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        List<User> us;
        us = MongoClientProvider.getAllUsers();
        assertTrue(us.size() >= 2);
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testReferenceUserManager() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertNotNull(MongoClientProvider.getUserById(user.getKey().toString()).getHomeManager());
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetUserByUser() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertNotNull(MongoClientProvider.getUserByUsername(user.getUsername()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testCheckUserByUserAndPass() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.checkUserByUserAndPass(user.getUsername(), user.getPassword()));
        assertFalse(MongoClientProvider.checkUserByUserAndPass("paperino", user.getPassword()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testMailPresent() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.mailPresent(user.getEmail()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }


    @Test
    void testUpdateFieldOfUser() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.updateFieldOfUser(user.getUsername(), IUserDAO.PASS, "ciaociao"));
        User u = MongoClientProvider.getUserById(user.getKey().toString());
        assertNotEquals(u.getPassword(), user.getPassword());
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetStatistics() {
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    private static void initStatisticsTest(SmarthomeDevice.DeviceType ty) {
        String type = SmarthomeDevice.DeviceType.typeToString(ty);
        List<String> devices = getNameDev(type);
        Date startDate = new GregorianCalendar(2020, Calendar.JANUARY, 1).getGregorianChange();
        Date endDate = new Date();
        int numTest = 10;

        switch (ty) {
            case FAN:
                for (String d : devices) {
                    for (int i = 0; i < numTest; i++) {
                        manager.performAction(d, Action.ONOFF,
                                String.valueOf(ThreadLocalRandom.current().nextInt(0, 1)),
                                between(startDate, endDate), true);
                        manager.performAction(d, Action.FANSPEED,
                                String.valueOf(ThreadLocalRandom.current().nextInt(0, 100)),
                                between(startDate, endDate), true);
                    }
                }
                break;

            case DOOR:
                for (String d : devices) {
                    for (int i = 0; i < numTest; i++) {
                        manager.performAction(d, Action.OPENCLOSE,
                                String.valueOf(ThreadLocalRandom.current().nextInt(0, 1)),
                                between(startDate, endDate), true);
                    }
                }
                break;

            case THERMOSTAT:
                for (String d : devices) {
                    for (int i = 0; i < numTest; i++) {
                        manager.performAction(d, Action.TEMPSET,
                                String.valueOf(ThreadLocalRandom.current().nextInt(0, 30)),
                                between(startDate, endDate), true);
                    }
                }
                break;

            case CONDITIONER:
                for (String d : devices) {
                    for (int i = 0; i < numTest; i++) {
                        manager.performAction(d, Action.ONOFF,
                                String.valueOf(ThreadLocalRandom.current().nextInt(0, 1)),
                                between(startDate, endDate), true);
                        manager.performAction(d, Action.TEMPSET,
                                String.valueOf(ThreadLocalRandom.current().nextInt(0, 30)),
                                between(startDate, endDate), true);
                    }
                }
                break;

            default:
                break;
        }


    }

    private static List<String> getNameDev(String type) {
        List<String> dev = new ArrayList<>();
        for (SmarthomeLocation sm : manager.getLocations()) {
            for (SmarthomeSublocation sublocation : sm.getSublocations().values()) {
                for (SmarthomeWebDevice d : sublocation.getDevices()) {
                    if (type.compareTo(d.getType()) == 0)
                        dev.add(d.getName().get("name"));
                }
            }
        }
        return dev;
    }

    public static Date between(Date startInclusive, Date endExclusive) {
        long startMillis = startInclusive.getTime();
        long endMillis = endExclusive.getTime();
        long randomMillisSinceEpoch = ThreadLocalRandom
                .current()
                .nextLong(startMillis, endMillis);

        return new Date(randomMillisSinceEpoch);
    }
}