package config.interfaces;

import javax.ejb.Remote;
import java.util.HashMap;

@Remote
public interface ConfigurationInterface {

    String getParameter( String conf_file, String parameter );
    HashMap<String,String> getConfiguration(String conf_file );
}
