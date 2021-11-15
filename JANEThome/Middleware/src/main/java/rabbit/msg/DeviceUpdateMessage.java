package rabbit.msg;

//  collections
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class used to generate an update message that can be sent on rabbitMQ.
 * The class consists in a container for DeviceUpdate request and a set of fields
 * to permit to the rabbitMQ system to forward the message
 */
public class DeviceUpdateMessage extends Message {

    //  a message can contain several device updates
    private final List<DeviceUpdate> updates = new ArrayList<>();

    /**
     * Constructor for a DeviceUpdateMessage class
     * @param target Destination for the message, must be an email
     * @param from Source of the message, must be an unique identifier of the sender
     * @throws InvalidMessageException Received in case of invalid target/from
     */
    public DeviceUpdateMessage( String target, String from ) throws InvalidMessageException{

        super( target, from );

    }

    public DeviceUpdateMessage( String target, String from, List<DeviceUpdate> updates ) throws InvalidMessageException{

        super( target, from );
        this.updates.addAll( updates );

    }

    ////////--  UTILITIES  --////////


    /**
     * Convert a serialized version of the message into the corresponding DeviceUpdateMessage object
     * @param serialization Gson string of the object
     * @return {@link DeviceUpdateMessage} returns the corresponding object or null
     */
    public static DeviceUpdateMessage convertMessage( String serialization ){

        try {

            return converter.fromJson( serialization, DeviceUpdateMessage.class );

        }catch( Exception e ){

            return null;

        }
    }

    public void addUpdates( DeviceUpdate... updates ){

        this.updates.addAll(Arrays.asList(updates));

    }

    public void addUpdates( List<DeviceUpdate> updates ){

        this.updates.addAll(updates);

    }

    public boolean removeUpdate( DeviceUpdate update ){
        return this.updates.remove(update);
    }


    public List<DeviceUpdate> getAllDeviceUpdate(){
        return updates;
    }

}
