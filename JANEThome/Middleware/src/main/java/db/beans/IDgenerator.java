package db.beans;

//  mongodb management
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

//  internal services
import config.interfaces.IConfiguration;
import db.interfaces.IGenerator;
import db.model.IDcontainer;

//  ejb3.0
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

//  utils
import java.util.Iterator;
import java.util.Properties;


/**
 * Class developed for id generation(compatibility with Riccardo). We need to generate unique
 * identifiers over the entire service for devices and locations.
 */
@Singleton
public class IDgenerator implements IGenerator {

    private MongoClient client;                   //  client connection
    private MongoCollection<IDcontainer> archive; //  data collection

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

        this.client = new MongoClient(
                server ,
                MongoClientOptions
                        .builder()
                        .readConcern( ReadConcern.LOCAL )
                        .readPreference( ReadPreference.nearest() )
                        .writeConcern( WriteConcern.W1 )
                        .codecRegistry( pojoCodecRegistry).build() );

        this.archive = this.client.getDatabase( "IoTServiceDB" ).getCollection( "Security", IDcontainer.class );

    }

    @PreDestroy
    private void close(){

        this.client.close();

    }

    /**
     * Generation of unique identifier for devices
     * @return A stringed integer corresponding to the deviceID
     */
    @Override
    @Lock( LockType.WRITE )
    public String generateDID() {

        return this.extractAndUpdateID("DID" );

    }

    /**
     * Generation of unique identifier for location
     * @return A stringed integer corresponding to the locationID
     */
    @Override
    @Lock( LockType.WRITE )
    public String generateLID() {

        return this.extractAndUpdateID("LID" );

    }

    /**
     * Basic function for id generation
     * @param type Type of ID it has to generate
     * @return A stringed integer corresponding to an ID
     */
    private String extractAndUpdateID( String type ){

        int dID;
        IDcontainer container;
        Bson where = new BasicDBObject( "type", type );

        //  getting the values
        Iterator<IDcontainer> iterator = this.archive.find( where ).iterator();

        //  if not present the service is initializing(first ID generation)
        if( iterator.hasNext() ){

            container = iterator.next(); //  only one value present into the database
            dID = container.getValue();
            container.incrementValue();
            this.archive.findOneAndReplace( where, container );  //  updating the value

        } else{

            this.archive.insertOne( new IDcontainer( type, 2 ));
            dID = 1;

        }

        return String.valueOf( dID );
    }
}
