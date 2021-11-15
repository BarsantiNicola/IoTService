package rabbit;

//  internal services
import config.interfaces.IConfiguration;

//  exceptions
import java.io.IOException;
import java.util.concurrent.TimeoutException;


//  rabbitMQ
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

//  utils
import java.util.Properties;

/**
 * Class designed as the base class for the rabbitMQ endpoint generation.
 * It generates an endpoint for sending/receiving messages with the DeviceUpdate topic exchange
 */
public class EndPoint{

    protected Connection connection;  //  connection with the rabbitMQ exchange

    //  channel for communication(good practice, use it only for sending or receiving messages, for both use two
    //  distinct channels allocated over the same connection)
    protected Channel channel;

    /**
     * Inizialization of the Endpoint.class, cannot be performed during the constructor because it's incompatible with
     * Federico's classes(ConfigurationInterface not already present during constructor inizialization)
     * @param configuration Context configuration of the rabbitMQ endpoint
     * @return True if the endpoint is correctly connected false otherwise
     */
    @SuppressWarnings("all")
    protected boolean inizialize( IConfiguration configuration ){

        Properties rabbitConf = configuration.getConfiguration( "rabbit" );
        ConnectionFactory factory = new ConnectionFactory();

        //  rabbitMQ will automatically close the clients which not receive messages for some times, inserting an heartbeat
        //  will prevent this behaviour maintaining active all the clients that are still reachable by the message exchange
        factory.setRequestedHeartbeat( 60 );

        //  without any configuration the parameters will assume as default values the following values:
        //  - username: guest
        //  - password: guest
        //  - hostname: localhost
        //  - port: 5672
        if( rabbitConf != null ){

            if( rabbitConf.containsKey( "username" ))
                factory.setUsername( rabbitConf.getProperty( "username" ));

            if( rabbitConf.containsKey( "password" ))
                factory.setPassword( rabbitConf.getProperty( "password" ));

            if( rabbitConf.containsKey( "hostname" ))
                factory.setHost( rabbitConf.getProperty( "hostname" ));

            if( rabbitConf.containsKey( "port" ))
                factory.setPort( Integer.parseInt( rabbitConf.getProperty( "port" )));
        }

        try {

            connection = factory.newConnection();
            channel = connection.createChannel();

            //  for the supported service we have to use only the DeviceUpdate topic exchange
            channel.exchangeDeclare( "DeviceUpdate","topic" );
            return true;

        }catch( TimeoutException | IOException e ){

            e.printStackTrace();
            connection = null;
            channel = null;
            return false;

        }
    }

    /**
     * Close the used channel and connection. Not mandatory as it happens anyway but good for improving reactivity
     * and reduce the resource usage
     */
    public void close(){

        if( channel != null )
            try{

                this.channel.close();

            }catch( IllegalStateException | TimeoutException | IOException ignored ){}

        if( connection != null )
            try{

                this.connection.close();

            }catch( IllegalStateException | IOException ignored ){}
    }
}