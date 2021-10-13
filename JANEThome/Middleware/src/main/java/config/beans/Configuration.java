package config.beans;

import config.interfaces.ConfigurationInterface;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.io.*;
import java.util.*;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


@Singleton
public class Configuration implements ConfigurationInterface {
    private final HashMap<String, Properties> configurations = new HashMap<>();
    private static final String[] files = {"db.properties", "rabbit.properties", "rest.properties", "mail.properties", "tokens.properties"};
    private static final String config = "rabbit.properties";

    private Logger logger;


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
    public String getParameter(String configFile, String parameter) {
        if (configurations.containsKey(configFile))
            return configurations.get(configFile).getProperty(parameter);
        return null;
    }

    public Properties getConfiguration(String configFile) {
        if (configurations.containsKey(configFile))
            return configurations.get(configFile);
        return null;
    }

    public boolean setParameter(String configFile, String param, String value) {
        Properties tempProp;
        String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource(configFile + ".properties")).getPath();
        if (configurations.containsKey(configFile) && path != null) {
            tempProp = configurations.get(configFile);
            tempProp.setProperty(param, value);
            try {
                FileOutputStream out = new FileOutputStream(path);
                tempProp.store(out, null);
                out.close();
                return true;
            } catch (IOException e) {
                logger.severe(e.getLocalizedMessage());
            }
        }
        return false;
    }

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    @PostConstruct
    private void initLogger() {

        if (this.logger != null)
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if (this.logger.getHandlers().length == 0) { //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }
    }
}
