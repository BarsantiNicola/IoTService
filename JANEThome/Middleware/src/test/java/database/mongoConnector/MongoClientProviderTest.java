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

class mongoClientProviderTest {
    private static SmarthomeManager manager;
    private static User user;
    private static MongoClientProvider mongoClientProvider;

    @BeforeAll
    static void setUp() {
        try{
            Configuration configuration = new Configuration();
            mongoClientProvider = new MongoClientProvider(configuration);
            user = new User("pluto", "federico", "lapenna", "f.lapenna@studenti.unipi.it", "test");
            manager = SmarthomeManager.createTestingEnvironment("test", true, configuration);
            initStatisticsTest(SmarthomeDevice.DeviceType.DOOR);
            initStatisticsTest(SmarthomeDevice.DeviceType.FAN);
            initStatisticsTest(SmarthomeDevice.DeviceType.THERMOSTAT);
            initStatisticsTest(SmarthomeDevice.DeviceType.CONDITIONER);

            user.setHomeManager(manager);
        }catch (Exception e){}
//        mongoClientProvider.connectDB();
    }

    @Test
    void testDeleteManager() {
        mongoClientProvider.writeManager(manager);
        assertTrue(mongoClientProvider.deleteManager(manager.getUsername()));
    }

    @Test
    void testWriteManager() {
        assertNotNull(mongoClientProvider.writeManager(manager));
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetManagerById() {
        mongoClientProvider.writeManager(manager);
        assertNotNull(mongoClientProvider.getManagerById(manager.getKey().toString()));
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetManagerByUsername() {
        mongoClientProvider.writeManager(manager);
        assertNotNull(mongoClientProvider.getManagerByUsername(manager.getUsername()));
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetAllManager() {
        mongoClientProvider.writeManager(manager);
        manager.setKey(new ObjectId());
        mongoClientProvider.writeManager(manager);
        List<SmarthomeManager> ms;
        ms = mongoClientProvider.getAllManagers();
        assertTrue(ms.size() >= 2);
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testdeleteUser() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertTrue(mongoClientProvider.deleteUser(user.getUsername()));
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testWriteUser() {
        mongoClientProvider.writeManager(manager);
        assertNotNull(mongoClientProvider.writeUser(user));
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void getUserById() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertNotNull(mongoClientProvider.getUserById(user.getKey().toString()));
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetAllUsers() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        manager.setKey(new ObjectId());
        user.setKey(new ObjectId());
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        List<User> us;
        us = mongoClientProvider.getAllUsers();
        assertTrue(us.size() >= 2);
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testReferenceUserManager() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertNotNull(mongoClientProvider.getUserById(user.getKey().toString()).getHomeManager());
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetUserByUser() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertNotNull(mongoClientProvider.getUserByUsername(user.getUsername()));
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testCheckUserByUserAndPass() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertTrue(mongoClientProvider.checkUserByUserAndPass(user.getUsername(), user.getPassword()));
        assertFalse(mongoClientProvider.checkUserByUserAndPass("paperino", user.getPassword()));
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testMailPresent() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertTrue(mongoClientProvider.mailPresent(user.getEmail()));
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }


    @Test
    void testUpdateFieldOfUser() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);
        assertTrue(mongoClientProvider.updateFieldOfUser(user.getUsername(), IUserDAO.PASS, "ciaociao"));
        User u = mongoClientProvider.getUserById(user.getKey().toString());
        assertNotEquals(u.getPassword(), user.getPassword());
        mongoClientProvider.deleteUser(user.getUsername());
        mongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetStatistics() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.deleteManager(manager.getUsername());
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