package config.beans;

import config.interfaces.ConfigurationInterface;

import javax.ejb.Singleton;
import java.io.*;
import java.util.*;
import java.io.IOException;


@Singleton
public class Configuration implements ConfigurationInterface {
    private final HashMap<String, Properties> configurations = new HashMap<>();
    private static final String[] files = {"db.properties", "rabbit.properties", "rest.properties", "mail.properties", "tokens.properties"};
    private static final String config = "rabbit.properties";


    public Configuration() throws IOException {
        Properties prop;

        for (String file : files) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
            if (inputStream != null) {
                prop = new Properties();
                prop.load(inputStream);
                configurations.put(file.replace(".properties", ""), prop);
            } else {
                throw new FileNotFoundException("property file '" + config + "' not found in the classpath");
            }
        }


    }

    @Override
    public String getParameter(String conf_file, String parameter) {
        if (configurations.containsKey(conf_file))
            return configurations.get(conf_file).getProperty(parameter);
        return null;
    }

    public Properties getConfiguration(String conf_file) {
        if (configurations.containsKey(conf_file))
            return configurations.get(conf_file);
        return null;
    }

}
