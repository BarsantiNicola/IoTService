package config.beans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.interfaces.GeneratorInterface;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Singleton
public class IDgenerator implements GeneratorInterface {

    private HashMap<String, String> tokens;
    private Logger logger;

    private void saveData(){

        Gson gson = new Gson();
        String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("META-INF/tokens.conf")).getPath();
        try {
            Files.write(Paths.get(path), gson.toJson(this.tokens).getBytes());
        }catch( IOException e ){
            e.printStackTrace();
        }

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
        Gson gson = new Gson();
        this.initializeLogger();
        try{

            String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("META-INF/tokens.conf")).getPath();
            if (path != null){
                this.tokens = gson.fromJson(new String(Files.readAllBytes(Paths.get(path))), new TypeToken<HashMap<String, String>>() {}.getType());

                if( this.tokens == null )
                    this.tokens = new HashMap<>();

                if( !this.tokens.containsKey( "dID") || !this.tokens.containsKey( "lID") ){

                    this.tokens.put("dID", "0" );
                    this.tokens.put( "lID", "0" );
                    this.saveData();

                }
            }
            this.logger.info( "Token data correctly generated" );

        }catch( IOException | NullPointerException e ){

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
        return value;

    }
}
