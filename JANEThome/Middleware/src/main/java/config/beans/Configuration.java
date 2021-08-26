package config.beans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.interfaces.ConfigurationInterface;
import javax.ejb.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Singleton
public class Configuration implements ConfigurationInterface {

    private final HashMap<String,HashMap<String,String>> conf = new HashMap<>();
    private static final String[] files = { "rabbit.conf", "rest.conf" };

    public Configuration(){

        Gson gson = new Gson();
        try{

            for( String file : files ) {
                String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("META-INF/" + file)).getPath();
                if (path != null)
                    conf.put(file.replace(".conf", ""), gson.fromJson(new String(Files.readAllBytes(Paths.get(path))), new TypeToken<HashMap<String, String>>() {
                    }.getType()));
            }

        }catch( IOException e ){
            e.printStackTrace();
        }

    }

    @Override
    public String getParameter(String conf_file, String parameter) {
        if( this.conf.containsKey(conf_file))
            return this.conf.get(conf_file).get(parameter);
        return null;
    }

    public HashMap<String,String> getConfiguration(String conf_file){
        if( this.conf.containsKey(conf_file))
            return this.conf.get(conf_file);
        return null;
    }

}
