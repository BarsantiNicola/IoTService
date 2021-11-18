package db.mongoConnector;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import iot.SmarthomeManager;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class SmarthomeProvider {

    private final MongoClient client;                  //  connection with mongo
    private final MongoCollection<SmarthomeManager> archive;  //  connection to insert data


    public SmarthomeProvider( Properties context ){

        ServerAddress server = new ServerAddress(
                context.getProperty( "hostname" ) ,
                Integer.parseInt( context.getProperty( "port" )));

        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                fromProviders( PojoCodecProvider.builder().automatic( true ).build() ));

        this.client = new MongoClient(
                server ,
                MongoClientOptions
                        .builder()
                        .readConcern( ReadConcern.LOCAL )
                        .readPreference( ReadPreference.nearest() )
                        .writeConcern( WriteConcern.W1 )
                        .codecRegistry( pojoCodecRegistry ).build());

        this.archive = this.client.getDatabase( "IoTServiceDB" ).getCollection( "HomeManager2", SmarthomeManager.class );

    }

    public void close(){

        this.client.close();

    }

    public boolean addSmarthome( SmarthomeManager manager ){
        Bson filter = eq( "username", manager.getUsername());
        if( this.archive.countDocuments( filter ) > 0 )
            return false;
        this.archive.insertOne( manager );
        return true;
    }

    public SmarthomeManager getSmarthome( String username ){

        Bson filter = eq( "username", username );
        Iterator<SmarthomeManager> iterator = this.archive.find( filter ).iterator();
        if( iterator.hasNext() ){

            SmarthomeManager manager = iterator.next();
            manager.relink();
            manager.addSmartHomeMutex( new Semaphore( 1 ));
            return manager;

        }
        return null;
    }

    public boolean updateSmarthome( SmarthomeManager manager ){

        Bson filter = eq( "username", manager.getUsername() );
        if( this.archive.countDocuments( filter ) > 0 ) {
            this.archive.replaceOne(filter, manager);
            return true;
        }
        return false;
    }

}
