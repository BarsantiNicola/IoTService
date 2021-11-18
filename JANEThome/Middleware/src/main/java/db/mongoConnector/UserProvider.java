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

public class UserProvider{

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


    ////////--  UTILITIES  --////////


    /**
     * Closes the mongo connection
     */
    public void close(){

        this.client.close();

    }


    /**
     * Adds a User on MongoDB
     *
     * @param user a {@link User} class
     * @return True in case of success otherwise false
     */
    public boolean addUser( User user ){

        Bson filter = eq( "username", user.getEmail() );
        //  verification user not already registered
        if( this.archive.countDocuments( filter ) > 0 )
            return false;

        this.archive.insertOne( user );
        return true;

    }

    /**
     * Verifies username-password match
     *
     * @param username Email of the user to verify
     * @param password Password to verify
     * @return True in case of success otherwise false
     */
    public boolean login( String username, String password ){

        BasicDBObject filter = new BasicDBObject( "username", username ).append( "password" , password );
        return this.archive.countDocuments( filter ) != 0;

    }

    /**
     * Verifies email presence
     * @param email The email to check
     * @return Returns true in case of presence otherwise false
     */
    public boolean emailPresent( String email ){

        Bson filter = eq( "email", email );
        return this.archive.countDocuments( filter ) > 0;

    }

    /**
     * Returns an array containing the first name and the last name of the selected user
     * @param username Email of the user
     * @return A {@link String[]} instance containing [0]=firstName, [1]=lastName or null
     */
    public String[] getUserFirstAndLastName( String username ){

        Bson filter = eq( "username", username );
        Iterator<User>  iterator = this.archive.find( filter ).iterator();
        if( iterator.hasNext() ) {

            User user = iterator.next();
            return new String[]{ user.getFirstName(), user.getLastName() };

        }
        return null;
    }

    /**
     * Changes the password associated with the user
     * @param username Name of the user to which change the password
     * @param password New password to set to the user
     * @return Returns true in case of success otherwise false
     */
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
