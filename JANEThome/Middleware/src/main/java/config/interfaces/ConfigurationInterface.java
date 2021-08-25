package config.interfaces;

import javax.ejb.Remote;

@Remote
public interface ConfigurationInterface {

    String getParameter( String conf_file, String parameter );

}
