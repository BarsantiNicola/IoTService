package db.dao;

import com.mongodb.WriteResult;
import db.interfaces.ISmartHomeManagerDAO;
import iot.SmarthomeManager;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

public class SmartHomeManagerDAO extends BasicDAO<SmarthomeManager, String> implements ISmartHomeManagerDAO {

    public SmartHomeManagerDAO(Class<SmarthomeManager> entityClass, Datastore ds) {
        super(entityClass, ds);
    }

    public SmarthomeManager get(ObjectId objectId) {
        return this.ds.get(this.entityClazz, objectId);
    }

    public WriteResult deleteById(ObjectId id) {
        return this.ds.delete(this.entityClazz, id);
    }

}
