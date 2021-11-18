package db.mongoConnector;

//  internal services
import db.model.User;

//  mongoDB management
import com.mongodb.*;
import org.bson.conversions.Bson;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

//  utils
import java.util.*;

public class UserProvider {

    private final MongoClient client;                  //  connection with mongo
    private final MongoCollection<User> archive;  //  connection to insert data

    public UserProvider( Properties context ){

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

        this.archive = this.client.getDatabase( "IoTServiceDB" ).getCollection( "Users", User.class );

    }

    public void close(){

        this.client.close();

    }

    public boolean addUser( User user ){

        Bson filter = eq( "username", user.getEmail());
        if( this.archive.countDocuments(filter) > 0 )
            return false;

        this.archive.insertOne( user );
        return true;

    }

    public boolean login( String username, String password ){

        BasicDBObject filter = new BasicDBObject( "username", username ).append( "password" , password );
        return this.archive.countDocuments( filter ) != 0;

    }

    public boolean emailPresent( String email ){

        Bson filter = eq( "email", email );
        return this.archive.countDocuments( filter ) > 0;

    }

    public String[] getUserFirstAndLastName( String username ){

        Bson filter = eq( "username", username );
        Iterator<User>  iterator = this.archive.find( filter ).iterator();
        if( iterator.hasNext() ) {
            User user = iterator.next();
            return new String[]{ user.getFirstName(), user.getLastName() };
        }
        return null;
    }

    public boolean changePassword( String username, String password ){

        Bson filter = eq( "username", username );
        Iterator<User>  iterator = this.archive.find( filter ).iterator();
        if( iterator.hasNext() ) {
            User user = iterator.next();
            user.setPassword( password );
            this.archive.findOneAndReplace( filter, user );
            return true;
        }
        return false;

    }

}
