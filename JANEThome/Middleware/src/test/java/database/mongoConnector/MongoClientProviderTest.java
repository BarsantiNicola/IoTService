package database.mongoConnector;

import config.beans.Configuration;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rabbit.msg.DeviceUpdate;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class mongoClientProviderTest {
    private static SmarthomeManager manager;
    private static User user;
    private static MongoClientProvider mongoClientProvider;

    @BeforeAll
    static void setUp() {
        try {
            Configuration configuration = new Configuration();
            mongoClientProvider = new MongoClientProvider(configuration);
            user = new User("pluto", "federico", "lapenna", "f.lapenna@studenti.unipi.it", "test");
            manager = SmarthomeManager.createTestingEnvironment("test", true, configuration);
            initStatisticsTest(SmarthomeDevice.DeviceType.DOOR);
            initStatisticsTest(SmarthomeDevice.DeviceType.FAN);
            initStatisticsTest(SmarthomeDevice.DeviceType.THERMOSTAT);
            initStatisticsTest(SmarthomeDevice.DeviceType.CONDITIONER);

            user.setHomeManager(manager);
            mongoClientProvider.deleteUser(user.getUsername());
            mongoClientProvider.deleteManager(manager.getUsername());

        } catch (Exception e) {
        }
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
    void testModifyManager() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);

        SmarthomeSublocation sublocation = null;

        SmarthomeLocation location = manager.getLocations().iterator().next();
        if (location == null) {
            return;
        }

        for (SmarthomeSublocation sb : location.getSublocations().values()) {
            if (!sb.getSubLocation().matches("default")) {
                sublocation = sb;
                break;
            }
        }
        if (sublocation == null) {
            return;
        }

        String locations = location.getLocation();
        String subloc = sublocation.getSubLocation();
        String device = sublocation.getDevices().iterator().next().giveDeviceName();

        //rename device
        assertNotNull(mongoClientProvider.renameElementManager(user.getEmail(), DeviceUpdate.UpdateType.RENAME_DEVICE,
                device, "pluto", locations));
        //rename sublocation
        assertNotNull(mongoClientProvider.renameElementManager(user.getEmail(), DeviceUpdate.UpdateType.RENAME_SUB_LOCATION,
                subloc, "pippo", locations));
        //rename location
        assertNotNull(mongoClientProvider.renameElementManager(user.getEmail(), DeviceUpdate.UpdateType.RENAME_LOCATION,
                locations, "paperino", locations));

        mongoClientProvider.deleteManager(manager.getUsername());
        mongoClientProvider.deleteUser(user.getUsername());
    }

    @Test
    void testAddManager() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);

        String address = "168.456.78";
        int port = 7897;

        SmarthomeSublocation sublocation = null;

        SmarthomeLocation location = manager.getLocations().iterator().next();
        if (location == null) {
            return;
        }

        for (SmarthomeSublocation sb : location.getSublocations().values()) {
            if (!sb.getSubLocation().matches("default")) {
                sublocation = sb;
                break;
            }
        }
        if (sublocation == null) {
            return;
        }

        String locations = location.getLocation();
        String subloc = sublocation.getSubLocation();

        //add device
        assertNotNull(mongoClientProvider.addElementManager(user.getEmail(), DeviceUpdate.UpdateType.ADD_DEVICE,
                "qwer", locations, address, port, subloc, "pippo", SmarthomeDevice.DeviceType.CONDITIONER));
        //add subLocation
        assertNotNull(mongoClientProvider.addElementManager(user.getEmail(), DeviceUpdate.UpdateType.ADD_SUB_LOCATION,
                "asdf", locations, address, port, "pluto", "pippo", SmarthomeDevice.DeviceType.CONDITIONER));
        //add location
        assertNotNull(mongoClientProvider.addElementManager(user.getEmail(), DeviceUpdate.UpdateType.ADD_LOCATION,
                "poio", "lol", address, port, "pluto", "pippo", SmarthomeDevice.DeviceType.CONDITIONER));

        mongoClientProvider.deleteManager(manager.getUsername());
        mongoClientProvider.deleteUser(user.getUsername());
    }

    @Test
    void testRemoveElementManager() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);

        SmarthomeSublocation sublocation = null;

        SmarthomeLocation location = manager.getLocations().iterator().next();
        if (location == null) {
            return;
        }

        for (SmarthomeSublocation sb : location.getSublocations().values()) {
            if (!sb.getSubLocation().matches("default")) {
                sublocation = sb;
                break;
            }
        }
        if (sublocation == null) {
            return;
        }

        String locations = location.getLocation();
        String subloc = sublocation.getSubLocation();
        String device = sublocation.getDevices().iterator().next().giveDeviceName();

        //remove device
        assertNotNull(mongoClientProvider.removeElementIntoManager(user.getEmail(), DeviceUpdate.UpdateType.REMOVE_DEVICE,
                device, locations));
        //remove sublocation
        assertNotNull(mongoClientProvider.removeElementIntoManager(user.getEmail(), DeviceUpdate.UpdateType.REMOVE_SUB_LOCATION,
                subloc, locations));
        //remove location
        assertNotNull(mongoClientProvider.removeElementIntoManager(user.getEmail(), DeviceUpdate.UpdateType.REMOVE_LOCATION,
                locations, locations));

        mongoClientProvider.deleteManager(manager.getUsername());
        mongoClientProvider.deleteUser(user.getUsername());
    }

    @Test
    void testPerformAction() {
        mongoClientProvider.writeManager(manager);
        mongoClientProvider.writeUser(user);

        String action = Action.ONOFF;
        String type = Action.FAN_ACTION;
        String id ;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.performAction(user.getEmail(), id,action,"1"));
        } else {
            System.out.print("id null FAN SPEED");
        }
        mongoClientProvider.deleteManager(manager.getUsername());
        mongoClientProvider.deleteUser(user.getUsername());
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
        assertTrue(MongoClientProvider.mailPresent(user.getEmail()));
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

        String id;
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.FEBRUARY, 1);

        Date startDate = calendar.getTime();
        Date endDate = new Date();
        String action;
        String type;

        // TEST ONOFF of FAN
        action = Action.ONOFF;
        type = Action.FAN_ACTION;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.getStatistics(id, action, startDate, endDate));
        } else {
            System.out.print("id null FAN ONOFF");
        }


        // TEST FANSPEED of FAN
        action = Action.FANSPEED;
        type = Action.FAN_ACTION;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.getStatistics(id, action, startDate, endDate));
        } else {
            System.out.print("id null FAN SPEED");
        }


        // TEST OPENCLOSE of DOOR
        action = Action.OPENCLOSE;
        type = Action.DOOR_ACTION;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.getStatistics(id, action, startDate, endDate));
        } else {
            System.out.print("id null DOOR OPEN");
        }


        // TEST ONOFF of THERM
        action = Action.ONOFF;
        type = Action.AC_ACTION;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.getStatistics(id, action, startDate, endDate));
        } else {
            System.out.print("id null AC ONOFF");
        }


        // TEST TEMPERATURE of THERN
        action = Action.TEMPSET;
        type = Action.AC_ACTION;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.getStatistics(id, action, startDate, endDate));
        } else {
            System.out.print("id null AC TEMP SET");
        }


        // TEST TEMPERATURE of THERN
        action = Action.TEMPSET;
        type = Action.THERM_ACTION;
        id = getRandomIdByType(type, manager);
        if (id != null) {
            assertNotNull(mongoClientProvider.getStatistics(id, action, startDate, endDate));
        } else {
            System.out.print("id null THERM TEMPSET");
        }

        mongoClientProvider.deleteManager(manager.getUsername());
    }

    private String getRandomIdByType(String type, SmarthomeManager manager) {
        for (SmarthomeLocation l : manager.getLocations()) {
            for (SmarthomeSublocation s : l.getSublocations().values()) {
                for (SmarthomeWebDevice d : s.getDevices()) {
                    if (d.getType().equals(type)) {
                        return d.getId();
                    }
                }
            }
        }
        return null;
    }
    private String getRandomNameByType(String type, SmarthomeManager manager) {
        for (SmarthomeLocation l : manager.getLocations()) {
            for (SmarthomeSublocation s : l.getSublocations().values()) {
                for (SmarthomeWebDevice d : s.getDevices()) {
                    if (d.getType().equals(type)) {
                        return d.getName().get("name");
                    }
                }
            }
        }
        return null;
    }

    private static void initStatisticsTest(SmarthomeDevice.DeviceType ty) {
        String type = SmarthomeDevice.DeviceType.typeToString(ty);
        List<String> devices = getNameDev(type);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.FEBRUARY, 1);

        Date startDate = calendar.getTime();
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