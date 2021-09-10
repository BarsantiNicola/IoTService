package db.interfaces;

import java.util.Collection;

public interface IGenericDao<T> {
    //String DB_HOST = "mongodb+srv://Miucio:qyDfo1-pohdek-fygboz@smarthomelocations.uwuk3.mongodb.net/IoTServiceDB?retryWrites=true&w=majority";
    String DB_HOST = "mongodb://127.0.0.1:27017/start?compressors=disabled&gssapiServiceName=mongodb";
    int DB_PORT = 27017;
    String DB_NAME = "IoTServiceDB";
}
