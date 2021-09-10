package database.mongoConnector;

import config.beans.Configuration;
import db.interfaces.IUserDAO;
import db.mongoConnector.MongoClientProvider;
import iot.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MongoClientProviderTest {
    private static SmarthomeManager manager;
    private static User user;

    @BeforeAll
    static void setUp() {
        Configuration configuration = new Configuration();
        user = new User("pluto","federico","lapenna","f.lapenna@studenti.unipi.it","test");
        manager = new SmarthomeManager("test",false,configuration);
        manager.addLocation("casa","2","via pippo",8888,false);
        manager.addSubLocation("casa","bagno","1",false);
        manager.addDevice("casa","bagno","45456","porta",SmarthomeDevice.DeviceType.DOOR,false);
        user.setHomeManager(manager);

        MongoClientProvider.connectDB();
    }

    @Test
    void testDeleteManager(){
        MongoClientProvider.writeManager(manager);
        assertTrue(MongoClientProvider.deleteManager(manager.getUsername()));
    }

    @Test
    void testConnectDB() {
        assertTrue(MongoClientProvider.connectDB());
    }

    @Test
    void testWriteManager(){
        assertNotNull(MongoClientProvider.writeManager(manager));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetManagerById(){
        MongoClientProvider.writeManager(manager);
        assertNotNull(MongoClientProvider.getManagerById(manager.getKey().toString()));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetManagerByUsername(){
        MongoClientProvider.writeManager(manager);
        assertNotNull(MongoClientProvider.getManagerByUsername(manager.getUsername()));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetAllManager(){
        MongoClientProvider.writeManager(manager);
        manager.setKey(new ObjectId());
        MongoClientProvider.writeManager(manager);
        List<SmarthomeManager> ms;
        ms = MongoClientProvider.getAllManagers();
        assertTrue(ms.size() >= 2);
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testdeleteUser(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.deleteUser(user.getUsername()));
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testWriteUser(){
        MongoClientProvider.writeManager(manager);
        assertNotNull(MongoClientProvider.writeUser(user));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void getUserById(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertNotNull(MongoClientProvider.getUserById(user.getKey().toString()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetAllUsers(){
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
    void testReferenceUserManager(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertNotNull(MongoClientProvider.getUserById(user.getKey().toString()).getHomeManager());
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testGetUserByUser(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertNotNull(MongoClientProvider.getUserByUsername(user.getUsername()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testCheckUserByUserAndPass(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.checkUserByUserAndPass(user.getUsername(), user.getPassword()));
        assertFalse(MongoClientProvider.checkUserByUserAndPass("paperino", user.getPassword()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }

    @Test
    void testMailPresent(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.mailPresent(user.getEmail()));
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }


    @Test
    void testUpdateFieldOfUser(){
        MongoClientProvider.writeManager(manager);
        MongoClientProvider.writeUser(user);
        assertTrue(MongoClientProvider.updateFieldOfUser(user.getUsername(), IUserDAO.PASS, "ciaociao"));
        User u = MongoClientProvider.getUserById(user.getKey().toString());
        assertNotEquals(u.getPassword(), user.getPassword());
        MongoClientProvider.deleteUser(user.getUsername());
        MongoClientProvider.deleteManager(manager.getUsername());
    }
}