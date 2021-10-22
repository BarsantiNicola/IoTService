package database.mongoConnector;

import config.beans.Configuration;
import config.interfaces.ConfigurationInterface;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rabbit.msg.DeviceUpdate;

import java.security.SecureRandom;
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
            manager = createTestingEnvironmentManager("test", true, configuration);
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
        ObjectId id = mongoClientProvider.writeManager(manager);
        assertTrue(mongoClientProvider.deleteManager(manager.getUsername()));
    }

    @Test
    void testDeleteManagerByID() {
        mongoClientProvider.writeManager(manager);
        assertTrue(mongoClientProvider.deleteManager(manager.getKey()));
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

        SmarthomeLocation location = manager.giveLocations().iterator().next();
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
        String device = sublocation.giveDevices().iterator().next().giveDeviceName();

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

        SmarthomeLocation location = manager.giveLocations().iterator().next();
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

        SmarthomeLocation location = manager.giveLocations().iterator().next();
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
        String device = sublocation.giveDevices().iterator().next().giveDeviceName();

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
        SmarthomeManager temp = mongoClientProvider.getManagerById(manager.getKey().toString());
        assertNotNull(temp);
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
        for (SmarthomeLocation l : manager.giveLocations()) {
            for (SmarthomeSublocation s : l.getSublocations().values()) {
                for (SmarthomeWebDevice d : s.giveDevices()) {
                    if (d.getType().equals(type)) {
                        return d.getId();
                    }
                }
            }
        }
        return null;
    }
    private String getRandomNameByType(String type, SmarthomeManager manager) {
        for (SmarthomeLocation l : manager.giveLocations()) {
            for (SmarthomeSublocation s : l.getSublocations().values()) {
                for (SmarthomeWebDevice d : s.giveDevices()) {
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
        for (SmarthomeLocation sm : manager.giveLocations()) {
            for (SmarthomeSublocation sublocation : sm.getSublocations().values()) {
                for (SmarthomeWebDevice d : sublocation.giveDevices()) {
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


    ///////TEST ENV////////

    public static SmarthomeManager createTestingEnvironmentManager(String username, boolean connected, ConfigurationInterface configuration) {
        return new SmarthomeManager(username, connected, configuration, createTestingEnvironmentLocation());
    }

    static List<SmarthomeLocation> createTestingEnvironmentLocation(){
        Random random = new SecureRandom();
        List<SmarthomeLocation> locations = new ArrayList<>();
        int nLocations = random.nextInt(3)+1;
        for( int a = 0;a<nLocations; a++) {
            String name = createRandomString();
            locations.add( new SmarthomeLocation(
                    name,
                    String.valueOf(a+1),
                    "8.8.8.8",
                    Math.abs(random.nextInt()),
                    createTestingEnvironmentSubLoc(name)));
        }
        return locations;
    }

    public static List<SmarthomeSublocation> createTestingEnvironmentSubLoc(String location){
        Random random = new SecureRandom();
        List<SmarthomeSublocation> sublocations = new ArrayList<>();
        int nSublocations = random.nextInt(2)+1;
        for( int a = 0;a<nSublocations; a++) {
            String name = createRandomString();
            sublocations.add(new SmarthomeSublocation( name, String.valueOf(a+1), createTestingEnvironmentDevice(location, name)));
        }
        return sublocations;
    }

    private static List<SmarthomeWebDevice> createTestingEnvironmentDevice(String location, String sub_location) {
        Random random = new SecureRandom();
        List<SmarthomeWebDevice> devices = new ArrayList<>();
        int nDevices = random.nextInt(5) + 2;

        for (int a = 0; a < nDevices; a++) {
            String name = createRandomString();
            devices.add(setParameters(new SmarthomeWebDevice(name, name, location, sub_location, SmarthomeDevice.DeviceType.values()[new Random().nextInt(SmarthomeDevice.DeviceType.values().length - 1)])));
        }
        return devices;

    }

    private static String createRandomString() {
        Random random = new SecureRandom();
        char[] allAllowed = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789".toCharArray();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 10; i++)
            token.append(allAllowed[random.nextInt(allAllowed.length)]);

        return token.toString().toLowerCase();

    }

    private static SmarthomeWebDevice setParameters(SmarthomeWebDevice device) {
        HashMap<String, String> param = new HashMap<>();
        HashMap<String, Date> exp = new HashMap<>();
        Date last = new Date(System.currentTimeMillis());
        switch (SmarthomeDevice.convertType(device.getType())) {
            case LIGHT:
                param.put("action", Action.ONOFF);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param);
                param.replace("action", Action.BRIGHNESS);
                device.setParam(param);
                param.replace("action", Action.COLORSET);
                param.replace("value", "#ECFF00");
                device.setParam(param);
                exp.put(Action.ONOFF, last);
                exp.put(Action.BRIGHNESS, last);
                exp.put(Action.COLORSET, last);
                break;

            case FAN:
                param.put("action", Action.ONOFF);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param);
                param.replace("action", Action.FANSPEED);
                device.setParam(param);
                exp.put(Action.ONOFF, last);
                exp.put(Action.FANSPEED, last);
                break;

            case DOOR:
                param.put("action", Action.LOCKUNLOCK);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param);
                param.replace("action", Action.OPENCLOSE);
                device.setParam(param);
                exp.put(Action.LOCKUNLOCK, last);
                exp.put(Action.OPENCLOSE, last);
                break;

            case CONDITIONER:
                param.put("action", Action.ONOFF);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "0");
                device.setParam(param);
                param.replace("action", Action.FANSPEED);
                device.setParam(param);
                param.replace("action", Action.TEMPSET);
                param.replace("value", "6.0");
                device.setParam(param);
                exp.put(Action.ONOFF, last);
                exp.put(Action.FANSPEED, last);
                exp.put(Action.TEMPSET, last);
                break;

            case THERMOSTAT:
                param.put("action", Action.TEMPSET);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "6.0");
                device.setParam(param);
                param.put("action", Action.TEMP);
                param.put("device_name", device.giveDeviceName());
                param.put("value", "7.0");
                device.setParam(param);
                exp.put(Action.TEMP, last);
                exp.put(Action.TEMPSET, last);
                break;

            default:
        }

        exp.put(Action.CONNECT, last);
        device.setExpires(exp);
        return device;
    }
}