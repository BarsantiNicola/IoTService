package config.interfaces;

import javax.ejb.Remote;
import java.util.Properties;

@Remote
public interface ConfigurationInterface {

    String getParameter( String conf_file, String parameter );
    Properties getConfiguration(String conf_file );
}
