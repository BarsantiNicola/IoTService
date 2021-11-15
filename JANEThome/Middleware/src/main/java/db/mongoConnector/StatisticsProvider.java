package db.mongoConnector;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import db.model.Operation;
import db.model.Statistic;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import iot.DeviceType;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class StatisticsProvider {

    private MongoClient client;
    private MongoCollection<Operation> archive;
    private MongoCollection<Statistic> statistics;

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

        ServerAddress server = new ServerAddress( context.getProperty("hostname") , Integer.parseInt(context.getProperty("port")));
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        this.client = new MongoClient( server , MongoClientOptions.builder().readConcern(ReadConcern.LOCAL).readPreference(ReadPreference.nearest()).writeConcern(WriteConcern.W1).codecRegistry(pojoCodecRegistry).build());

        this.archive = this.client.getDatabase("IoTServiceDB").getCollection("Statistics",Operation.class);
        this.statistics = this.client.getDatabase("IoTServiceDB").getCollection("Statistics",Statistic.class);
    }

    StatisticsProvider(){
        ServerAddress server = new ServerAddress( "localhost" , 27017);
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        this.client = new MongoClient( server , MongoClientOptions.builder().readConcern(ReadConcern.LOCAL).readPreference(ReadPreference.nearest()).writeConcern(WriteConcern.W1).codecRegistry(pojoCodecRegistry).build());

        this.archive = this.client.getDatabase("IoTServiceDB").getCollection("Statistics",Operation.class);
        this.statistics = this.client.getDatabase("IoTServiceDB").getCollection("Statistics",Statistic.class);
    }

    public void writeOperation( Operation operation ){
        this.archive.insertOne(operation);
    }

    public void removeAllStatistics( String dID ){
        this.archive.deleteMany(eq("dID", dID));
    }

    public List<Statistic> getStatistic(String statistic_name, String dID, DeviceType type, Date start, Date end){

        switch( this.getStatisticType( statistic_name, type)){

            case DEVICE_USAGE:
                return this.getDeviceUsageStatistic( dID, start, end );

            case BRIGHTNESS:
                return this.getBrightnessStatistic( dID, start, end );

            case FAN_SPEED:
                return this.getFanSpeedStatistic( dID, start, end );

            case OPENING:
                return this.getOpeningStatistic( dID, start, end );

            case LOCKING:
                return this.getLockingStatistic( dID, start, end );

            case TEMP_USAGE:
                return this.getTempUsageStatistic( dID, start, end );

            case TEMPERATURE:
                return this.getTemperatureStatistic( dID, start, end );

            default:
                return new ArrayList<>();
        }

    }

    public static void main(String[] args){
        StatisticsProvider stat = new StatisticsProvider();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.add(Calendar.DAY_OF_MONTH, -5);
        Date dateBefore30Days = cal.getTime();
        Gson gson = new Gson();
        System.out.println("START: " + gson.toJson(cal.getTime()) + " END: " +gson.toJson(new Date(System.currentTimeMillis())));
        System.out.println("STATS: " + gson.toJson(stat.getDeviceUsageStatistic("100", cal.getTime(), new Date(System.currentTimeMillis()))));

    }

    private List<Statistic> getDeviceUsageStatistic( String dID, Date start, Date end ){


        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.OnOff").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date"))) stats.add(new Statistic(op.date, op.value));
        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));
        return stats;

    }

    private List<Statistic> getTempUsageStatistic( String dID, Date start, Date end ){

        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.TemperatureSetting").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date")))
            stats.add(new Statistic(op.date, op.value));

        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));

        return stats.subList(0,2);

    }

    private List<Statistic> getBrightnessStatistic( String dID, Date start, Date end ) {

        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.Brightness").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date")))
            stats.add(new Statistic(op.date, op.value));

        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));

        return stats.subList(0,2);

    }

    private List<Statistic> getFanSpeedStatistic( String dID, Date start, Date end ){
        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.FanSpeed").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date")))
            stats.add(new Statistic(op.date, op.value));

        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));

        return stats.subList(0,2);
    }

    private List<Statistic> getOpeningStatistic( String dID, Date start, Date end ){
        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.OpenClose").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date")))
            stats.add(new Statistic(op.date, op.value));

        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));

        return stats.subList(0,2);
    }

    private List<Statistic> getLockingStatistic( String dID, Date start, Date end ){

        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.LockUnlock").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date")))
            stats.add(new Statistic(op.date, op.value));

        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));

        return stats.subList(0,2);

    }

    private List<Statistic> getTemperatureStatistic( String dID, Date start, Date end ){
        List<Statistic> stats = new ArrayList<>();

        BasicDBObject select = new BasicDBObject("dID", dID).append("action", "action.devices.traits.Temperature").append("date", new BasicDBObject("$gte", start).append("$lt", end));
        for (Operation op : this.archive.find(select).sort(Sorts.ascending("date")))
            stats.add(new Statistic(op.date, op.value));

        if( stats.size() == 0 )
            stats.addAll( Arrays.asList( new Statistic(start,"0"), new Statistic(end,"0")));

        return stats.subList(0,2);
    }

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

}
