package rabbit.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//  Class used to define an update message that can be sent on rabbitMQ
@SuppressWarnings("unused")
public class DeviceUpdateMessage extends Message {

    //  an update message can contain several device updates
    private final List<DeviceUpdate> updates = new ArrayList<>();

    public DeviceUpdateMessage( String userID ){
        super(userID);
    }

    public DeviceUpdateMessage( String userID, List<DeviceUpdate> updates ){

        super(userID);
        this.updates.addAll(updates);

    }

    //  generates a json representation of the class
    public String buildMessage(){
        return converter.toJson(this );
    }

    //  converts a json string into the class
    public static DeviceUpdateMessage convertMessage( String serialization ) {
        try {
            return converter.fromJson(serialization, DeviceUpdateMessage.class);
        }catch(Exception e){
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
