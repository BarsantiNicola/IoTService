package db.mongoConnector;

//  internal services
import db.model.Operation;
import db.model.Statistic;
import iot.DeviceType;

//  mongoDB management
import com.mongodb.*;
import org.bson.conversions.Bson;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import static com.mongodb.client.model.Filters.eq;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

//  utils
import java.util.*;


/**
 * Class for statistics on devices data
 */
@SuppressWarnings( "all" )
public class StatisticsProvider {

    private final MongoClient client;                  //  connection with mongo
    private final MongoCollection<Operation> archive;  //  connection to insert data
    private final MongoCollection<Statistic> stats;    //  connection to extract statistics

    private enum StatisticType{
        DEVICE_USAGE,
        TEMP_USAGE,
        BRIGHTNESS,
        FAN_SPEED,
        OPENING,
        LOCKING,
        TEMPERATURE,
        UNKNOWN
    }

    StatisticsProvider( Properties context ){

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

        this.archive = this.client.getDatabase( "IoTServiceDB" ).getCollection( "Statistics", Operation.class );
        this.stats = this.client.getDatabase( "IoTServiceDB" ).getCollection( "Statistics", Statistic.class );
    }


    ////////--  UTILITIES  --////////


    /**
     * Used to add raw devices data
     * @param operation Operation executed
     */
    public void writeOperation( Operation operation ){

        this.archive.insertOne( operation );

    }

    /**
     * Removes all the data of a device
     * @param dID Unique device identifier
     */
    public void removeAllStatistics( String dID ){

        this.archive.deleteMany( eq( "dID", dID ));

    }

    /**
     * Returns the statistic obtained from the collected devices data
     * @param statistic_name Name of the statistic according to the webClient
     * @param dID            Unique identifier of the device
     * @param type           Type fo the device
     * @param start          Start timestamp for statistic aggregation
     * @param end            End timestamp for statistic aggregation
     * @return A list of Statistic classes printable by the webClients
     */
    public List<Statistic> getStatistic( String statistic_name, String dID, DeviceType type, Date start, Date end ){

        switch( this.getStatisticType( statistic_name, type )){

            case DEVICE_USAGE:
                return this.getDeviceUsageStatistic( dID, start, end, 20 );

            case BRIGHTNESS:
                return this.getBrightnessStatistic( dID, start, end, 20 );

            case FAN_SPEED:
                return this.getFanSpeedStatistic( dID, start, end, 20 );

            case OPENING:
                return this.getOpeningStatistic( dID, start, end, 20 );

            case LOCKING:
                return this.getLockingStatistic( dID, start, end, 20 );

            case TEMP_USAGE:
                return this.getTempUsageStatistic( dID, start, end, 20 );

            case TEMPERATURE:
                return this.getTemperatureStatistic( dID, start, end, 20 );

            default:
                return new ArrayList<>();
        }

    }

    /**
     * Converts the statistic name into an enumerator
     * @param stat_name Name of the statistic
     * @param type      Device type
     * @return An enumerator {@link StatisticType} corresponding to the statistic
     */
    private StatisticType getStatisticType(String stat_name, DeviceType type ){
        switch( type ){
            case LIGHT:
                return stat_name.compareTo("Device Usage") == 0? StatisticType.DEVICE_USAGE : StatisticType.BRIGHTNESS;
            case FAN:
                return stat_name.compareTo("Device Usage") == 0? StatisticType.DEVICE_USAGE : StatisticType.FAN_SPEED;
            case DOOR:
                return stat_name.compareTo("N.Door Usage") == 0? StatisticType.OPENING : StatisticType.LOCKING;
            case THERMOSTAT:
            case CONDITIONER:
                return stat_name.compareTo("Device Usage") == 0? StatisticType.TEMP_USAGE : StatisticType.TEMPERATURE;
            default:
                return StatisticType.UNKNOWN;
        }
    }

    /**
     * Used for closing the connection before destroying the object
     */
    public void close(){

        this.client.close();

    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic counts the number of OnOff executions into the interval
     * @param dID    Unique identifier of the device
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of 10 {@link Statistic}
     */
    private List<Statistic> getDeviceUsageStatistic( String dID, Date start, Date end, int split ){

        if( end.getTime() < start.getTime() || split < 2 )
            return new ArrayList<>();

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add(
                    this.getCountStatistic(
                            dID,
                            "action.devices.traits.OnOff",
                            new Date( start.getTime()+ interval*a ),
                            new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );


        return data;

    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic evaluate the mean value of the Temperature Setted into the interval
     * @param dID    Unique identifier of the device
     * @param action Device action to consider during aggregation
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of aggregated {@link Statistic}
     */
    private List<Statistic> getTempUsageStatistic( String dID, Date start, Date end, int split ){

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add( this.getAvgStatistic(
                    dID,
                    "action.devices.traits.TemperatureSetting",
                    new Date( start.getTime()+ interval*a ),
                    new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );

        return data;

    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic evaluate the mean value of the brightness into the interval
     * @param dID    Unique identifier of the device
     * @param action Device action to consider during aggregation
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of aggregated {@link Statistic}
     */
    private List<Statistic> getBrightnessStatistic( String dID, Date start, Date end, int split ) {

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add(
                    this.getAvgStatistic(
                            dID,
                            "action.devices.traits.Brightness",
                            new Date( start.getTime()+ interval*a ),
                            new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );

        return data;

    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic evaluate the mean value of FanSpeed into the interval
     * @param dID    Unique identifier of the device
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of 10 {@link Statistic}
     */
    private List<Statistic> getFanSpeedStatistic( String dID, Date start, Date end, int split ){

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add(
                    this.getAvgStatistic(
                            dID,
                            "action.devices.traits.FanSpeed",
                            new Date( start.getTime()+ interval*a ),
                            new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );

        return data;
    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic counts the number of OpenClose executions into the interval
     * @param dID    Unique identifier of the device
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of 10 {@link Statistic}
     */
    private List<Statistic> getOpeningStatistic( String dID, Date start, Date end, int split ){

        if( end.getTime() < start.getTime() || split < 2 )
            return new ArrayList<>();

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add(
                    this.getCountStatistic(
                            dID,
                            "action.devices.traits.OpenClose",
                            new Date( start.getTime()+ interval*a ),
                            new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );

        return data;

    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic counts the number of LockUnlock executions into the interval
     * @param dID    Unique identifier of the device
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of 10 {@link Statistic}
     */
    private List<Statistic> getLockingStatistic( String dID, Date start, Date end, int split ){

        if( end.getTime() < start.getTime() || split < 2 )
            return new ArrayList<>();

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add(
                    this.getCountStatistic(
                            dID,
                            "action.devices.traits.LockUnlock",
                            new Date( start.getTime()+ interval*a ),
                            new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );

        return data;

    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic evaluate the mean value of the Environment Temperature into the interval
     * @param dID    Unique identifier of the device
     * @param action Device action to consider during aggregation
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A list of aggregated {@link Statistic}
     */
    private List<Statistic> getTemperatureStatistic( String dID, Date start, Date end, int split ){

        List<Statistic> data = new ArrayList<>();

        long interval = ( end.getTime() - start.getTime() )/split;

        for( int a = 0; a<split; a++ )
            data.add(
                    this.getAvgStatistic(
                            dID,
                            "action.devices.traits.Temperature",
                            new Date( start.getTime()+ interval*a ),
                            new Date( start.getTime()+ interval*( a + 1 ))
                    )
            );

        return data;
    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic evaluate the mean value of the target field into the interval
     * @param dID    Unique identifier of the device
     * @param action Device action to consider during aggregation
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A single aggregated {@link Statistic}
     */
    private Statistic getAvgStatistic( String dID, String action, Date start, Date end ){

        BasicDBObject idMatch = new BasicDBObject( "$eq", dID );
        BasicDBObject actionMatch = new BasicDBObject( "$eq", action );
        BasicDBObject timeMatch =  new BasicDBObject( "$gte", start ).append( "$lte", end );
        BasicDBObject match = new BasicDBObject( "date", timeMatch ).append( "dID", idMatch ).append( "action", actionMatch );

        BasicDBObject avgAggregate = new BasicDBObject( "$avg", new BasicDBObject( "$toInt", "$value" ));
        BasicDBObject timeAggregate = new BasicDBObject( "$avg", new BasicDBObject( "$toDecimal", "$date" ));

        BasicDBObject group = new BasicDBObject( "_id", "dID" );
        BsonField aggregateY = new BsonField( "y", avgAggregate );
        BsonField aggregateX = new BsonField( "x", timeAggregate );

        List<Statistic> stats = new ArrayList<>();

        for (Statistic statistic : this.stats.aggregate(Arrays.asList(
                Aggregates.match(match),
                Aggregates.group(group, aggregateX, aggregateY )
        )))
            stats.add( new Statistic( new Date( (long)(((float)start.getTime() + end.getTime())/2)), String.valueOf( statistic.getY()) ));

        if( !stats.isEmpty())
            return stats.get(0);

        return new Statistic( new Date( (long)(((float)start.getTime() + end.getTime())/2)), "0.0" );
    }

    /**
     * Generate an aggregation of the value, splitting the giving interval in subranges
     * The statistic counts the number of action executions into the interval
     * @param dID    Unique identifier of the device
     * @param action Device action to consider during aggregation
     * @param start  {@link Date} Start time of the statistic
     * @param end    {@link Date} End time of the statistic
     * @return       A single aggregated {@link Statistic}
     */
    private Statistic getCountStatistic( String dID, String action, Date start, Date end ){

        Bson filter = new BasicDBObject(
                "date" , new BasicDBObject( "$gte", start ).append( "$lte", end ))
                .append( "dID", dID)
                .append("action", action);

        return new Statistic(
                        new Date( (long)(((float)start.getTime() + end.getTime())/2)),
                        String.valueOf( (float) this.stats.countDocuments( filter )));

    }

}
