package db.mongoConnector;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import config.interfaces.IConfiguration;
import db.interfaces.GeneratorInterface;
import db.model.IDcontainer;
import db.model.Operation;
import db.model.Statistic;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.util.Iterator;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Singleton
public class IDgenerator implements GeneratorInterface {

    private MongoClient client;
    private MongoCollection<IDcontainer> archive;

    @EJB
    IConfiguration configuration;

    @PostConstruct
    private void inizialize(){

        Properties context = configuration.getConfiguration( "db" );
        ServerAddress server = new ServerAddress( context.getProperty( "hostname" ) , Integer.parseInt( context.getProperty( "port" )));
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                fromProviders(
                        PojoCodecProvider
                                .builder()
                                .automatic( true )
                                .build()));

        this.client = new MongoClient( server , MongoClientOptions.builder().readConcern(ReadConcern.LOCAL).readPreference(ReadPreference.nearest()).writeConcern(WriteConcern.W1).codecRegistry(pojoCodecRegistry).build());

        this.archive = this.client.getDatabase("IoTServiceDB").getCollection("Security", IDcontainer.class );

    }

    @PreDestroy
    private void close(){

        this.client.close();

    }

    @Override
    @Lock( LockType.WRITE )
    public String generateDID() {

        return this.extractAndUpdateID("DID" );

    }

    @Override
    @Lock( LockType.WRITE )
    public String generateLID() {

        return this.extractAndUpdateID("LID" );

    }

    private String extractAndUpdateID( String type ){

        Bson filter = new BasicDBObject("type", type);
        Iterator<IDcontainer> iterator = this.archive.find(filter).iterator();
        int dID;
        IDcontainer container;
        if (iterator.hasNext()) {

            container = iterator.next();
            dID = container.getValue();
            container.incrementValue();
            this.archive.findOneAndReplace(filter, container);

        } else{
            this.archive.insertOne( new IDcontainer( type, 2 ));
            dID = 1;
        }

        return String.valueOf(dID);
    }
}
