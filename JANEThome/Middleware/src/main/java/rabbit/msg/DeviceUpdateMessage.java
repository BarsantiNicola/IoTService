package rabbit.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//  Class used to define an update message that can be sent on rabbitMQ. The update consist of
//  a mandatory destination(this will be used as the destination of the message) and a list of
//  DeviceUpdate instances, each instance will correspond to an update to be send to the target
public class DeviceUpdateMessage extends Message {

    //  an update message can contain several device updates
    private final List<DeviceUpdate> updates = new ArrayList<>();

    //  throws an exception in case of invalid target
    public DeviceUpdateMessage( String target ) throws InvalidMessageException{

        super(target);
    }

    //  throws an exception in case of invalid target
    public DeviceUpdateMessage( String target, List<DeviceUpdate> updates ) throws InvalidMessageException{

        super(target);
        this.updates.addAll(updates);

    }

    //  converts a json string into the corresponding DeviceUpdateMessage class
    public static DeviceUpdateMessage convertMessage( String serialization ){

        try {

            return converter.fromJson(serialization, DeviceUpdateMessage.class);

        }catch(Exception e){

            e.printStackTrace();
            return null;

        }

    }

    //  adds one or more deviceUpdates to the message
    public void addUpdates( DeviceUpdate... updates ){

        this.updates.addAll(Arrays.asList(updates));

    }

    // adds a list of deviceUpdates to the message
    public void addUpdates( List<DeviceUpdate> updates ){

        this.updates.addAll(updates);

    }

    //  gives back the list of DeviceUpdate classes
    public List<DeviceUpdate> getAllDeviceUpdate(){
        return updates;
    }

}
