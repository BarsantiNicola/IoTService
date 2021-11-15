package config.beans;

import config.interfaces.IConfiguration;
import config.interfaces.GeneratorInterface;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Singleton
public class IDgenerator implements GeneratorInterface {

    private HashMap<String, String> tokens;
    private Logger logger;

    @EJB
    IConfiguration configuration;

    private void saveData(){

        Properties generatorConf = configuration.getConfiguration("tokens");
        generatorConf.setProperty("dID", this.tokens.get("dID"));
        generatorConf.setProperty("lID", this.tokens.get("lID"));

    }

    //  Singleton function to obtain a logger preventing the usage of more than one logger handler.
    private void initializeLogger(){

        if( this.logger != null )
            return;

        this.logger = Logger.getLogger(getClass().getName());

        //  verification of the number of instantiated handlers
        if( this.logger.getHandlers().length == 0 ){ //  first time the logger is created we generate its handler

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

        }

    }

    @PostConstruct
    private void init(){

        this.initializeLogger();
        try{

            Properties generatorConf = configuration.getConfiguration("tokens");

            if( this.tokens == null )
                this.tokens = new HashMap<>();

            this.tokens.put("dID", generatorConf.getProperty("dID"));
            this.tokens.put( "lID", generatorConf.getProperty("lID"));

            this.logger.info( "Token data correctly generated" );

        }catch( NullPointerException e ){

            this.logger.severe( "Error, unable to find the data" );
            this.tokens = new HashMap<>();
            this.tokens.put("dID", "0" );
            this.tokens.put( "lID", "0" );
            this.saveData();

        }
    }

    @Override
    @Lock( LockType.WRITE )
    public String generateDID() {

        if( this.tokens == null || !this.tokens.containsKey( "dID") )
            return "";

        this.initializeLogger();

        String value =  this.tokens.get( "dID" );
        this.tokens.replace( "dID" , String.valueOf(Integer.parseInt(value)+1));
        this.logger.info( "Generation of new DID completed: " + value );
        this.saveData();
        return value;

    }

    @Override
    @Lock( LockType.WRITE )
    public String generateLID() {

        if( this.tokens == null || !this.tokens.containsKey( "lID") )
            return "";

        this.initializeLogger();

        String value =  this.tokens.get( "lID" );
        this.tokens.replace( "lID" , String.valueOf(Integer.parseInt(value)+1));
        this.logger.info( "Generation of new LID completed: " + value );
        this.saveData();
        return value;

    }
}
