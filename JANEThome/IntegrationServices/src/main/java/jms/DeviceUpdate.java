package jms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class DeviceUpdate implements Serializable{

    private final String id;
    private final HashMap<String, String> updates = new HashMap<>();

    DeviceUpdate( String id ){
        this.id = id;
    }

    DeviceUpdate( String id, HashMap<String,String> parameters ){
        this(id);
        updates.putAll(parameters);
    }

    String getId(){
        return id;
    }

    void setParameter(String name, String value){
        if( updates.containsKey(name))
            updates.replace(name, value);
        else
            updates.put(name, value);
    }

    String getParameter(String name){
        return updates.get(name);
    }

    Set<String> getAllParametersName(){
        return this.updates.keySet();
    }
}
