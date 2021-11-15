package config.interfaces;

//  ejb3.0
import javax.ejb.Remote;

//  utils
import java.util.Properties;

/**
 * Remote EJB class usable from all the components of the service to retrieve their
 * configuration. Configuration are stored inside Middleware/resources/META-INF into .properties files
 */
@Remote
public interface IConfiguration {

    /**
     * Returns a specific parameter taken from the specified configuration
     * @param config Name of the configuration to retrieve(name of the configuration file removing .properties)
     * @param parameter Name of the parameter to retrieve
     * @return The value of the parameter or null
     */
    String getParameter( String config, String parameter );

    /**
     * Returns the specified configuration
     * @param name Name of the configuration to retrieve(name of the configuration file removing .properties)
     * @return {@link Properties} Returns Properties class containing all the parameters available for that configuration
     */
    Properties getConfiguration( String name );

    /**
     * Update a configuration parameter(Used by token generators)
     * @param config Name of the configuration(name of the configuration file removing .properties)
     * @param param Name of the parameter to update into the configuration
     * @param value Value to be applied to the parameter
     * @return Return true in case of success otherwise false
     */
    boolean setParameter(String config, String param, String value);

}
