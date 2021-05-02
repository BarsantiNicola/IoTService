package iot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmarthomeDefinition implements Serializable {

    private final String username;
    private final List<SmarthomeLocation> locations;
    private final String type = "START_SESSION";

    //  For testing purpouse
    public SmarthomeDefinition(String username){
        this.username = username;
        locations = new ArrayList<>();
    }

    public SmarthomeDefinition(String username, List<SmarthomeLocation> locations){
        this(username);
        this.locations.addAll(locations);
    }

    public String getUsername(){ return username; };

    public List<SmarthomeLocation> getLocations() {
        return locations;
    }

    //  TODO To be removed
    public static SmarthomeDefinition createTestingEnvironment(String username){
        return new SmarthomeDefinition(username, SmarthomeLocation.createTestingEnvironment());

    }

}
