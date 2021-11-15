package config.beans;

//  internal services
import config.interfaces.IConfiguration;

//  ejb3.0
import javax.ejb.Singleton;

//  utils
import java.io.*;
import java.util.*;

//  exceptions
import java.io.IOException;

@Singleton
public class Configuration implements IConfiguration {

    private final HashMap<String, Properties> configurations = new HashMap<>();
    private static final String[] files = {
            "db.properties",
            "rabbit.properties",
            "rest.properties",
            "mail.properties"
    };

    public Configuration() throws IOException{

        Properties prop;

        for( String file : files ){

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream( "META-INF/" + file );
            if( inputStream != null ){

                prop = new Properties();
                prop.load( inputStream );
                configurations.put( file.replace( ".properties", "" ), prop );

            }
        }
    }

    /**
     * Returns a specific parameter taken from the specified configuration
     * @param configFile Name of the configuration to retrieve(name of the configuration file removing .properties)
     * @param parameter Name of the parameter to retrieve
     * @return The value of the parameter or null
     */
    @Override
    public String getParameter( String configFile, String parameter ){

        if( configurations.containsKey( configFile ))
            return configurations.get( configFile ).getProperty( parameter );

        return null;

    }

    /**
     * Returns the specified configuration
     * @param configFile Name of the configuration to retrieve(name of the configuration file removing .properties)
     * @return {@link Properties} Returns Properties class containing all the parameters available for that configuration
     */
    public Properties getConfiguration( String configFile ){

        if( configurations.containsKey( configFile ))
            return configurations.get( configFile );

        return null;

    }
}
