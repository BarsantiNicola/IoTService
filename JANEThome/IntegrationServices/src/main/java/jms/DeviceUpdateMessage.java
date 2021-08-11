package jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class DeviceUpdateMessage extends Message{

    private final List<DeviceUpdate> updates = new ArrayList<>();

    public DeviceUpdateMessage( String userID, List<DeviceUpdate> updates ){
        super(userID);
        this.updates.addAll(updates);
    }

    public DeviceUpdateMessage(String userID){
        super(userID);
    }

    public static DeviceUpdateMessage buildClass(String serialization) {
        return converter.fromJson(serialization, DeviceUpdateMessage.class);
    }

    public void addUpdate( DeviceUpdate... update ){
        this.updates.addAll(Arrays.asList(update));
    }

    public void replaceUpdate(UnaryOperator<DeviceUpdate>... update ){
        for( UnaryOperator<DeviceUpdate> operation: update )
            this.updates.replaceAll(operation);
    }

    public void removeUpdate( DeviceUpdate... update ){
        this.updates.removeAll( Arrays.asList(update));
    }

    public List<String> getUpdatedDeviceList(){
        List<String> devices = new ArrayList<>();
        this.updates.forEach( (elem)-> devices.add(elem.getId()));
        return devices;
    }

    public DeviceUpdate getUpdateByDID( String dID ){
        for( DeviceUpdate update: this.updates )
            if( update.getId().compareTo(dID) == 0 )
                return update;
        return null;
    }

    public List<DeviceUpdate> getAllDeviceUpdate(){
        return updates;
    }
}
