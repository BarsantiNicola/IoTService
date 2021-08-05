package utils.device;

import jms.DeviceUpdate;

import java.util.ArrayList;
import java.util.List;

public class DeviceContainer {

    private String userID;
    private List<DeviceUpdate> updates = new ArrayList<>();

    public DeviceContainer(String id ){
        userID = id;
    }
}