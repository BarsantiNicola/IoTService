package rabbit;

import java.util.HashMap;
import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.Properties;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.ConnectionFactory;
import config.interfaces.ConfigurationInterface;

///// EndPoint
//
//  Description: class designed as a base class to generate endpoints to
//               the rabbitMQ message exchanger

public class EndPoint{

    protected Channel channel;
    protected Connection connection;

    //  inizialization of EndPoint class, cannot be performed during the constructor because it's incompatible with
    //  other extended classes(EJB object, in particular ConfigurationInterface isn't available into constructors)
    protected boolean inizialize( ConfigurationInterface configuration ){

        Properties rabbitConf = configuration.getConfiguration("rabbit");
        ConnectionFactory factory = new ConnectionFactory();

        //  rabbitMQ will automatically close the clients which not receive messages for some times, inserting an heartbeat
        //  will prevent this behaviour maintaining active all the clients that are still reachable by the message exchange
        factory.setRequestedHeartbeat(60);

        //  without any configuration the parameters will assume as default values the following parameters:
        //  - username: guest
        //  - password: guest
        //  - hostname: localhost
        //  - port: 5672
        if( rabbitConf != null ){

            if( rabbitConf.containsKey("username"))
                factory.setUsername( rabbitConf.getProperty( "username" ));

            if( rabbitConf.containsKey("password"))
                factory.setPassword( rabbitConf.getProperty( "password" ));

            if( rabbitConf.containsKey("hostname"))
                factory.setHost( rabbitConf.getProperty( "hostname" ));

            if( rabbitConf.containsKey("port"))
                factory.setPort( Integer.parseInt(rabbitConf.getProperty( "port" )));
        }

        try {

            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare("DeviceUpdate","topic");
            return true;

        } catch (TimeoutException | IOException e) {

            e.printStackTrace();
            connection = null;
            channel = null;
            return false;

        }
    }

    // Close channel and connection. Not necessary as it happens implicitly any way.
    public void close(){

        if( channel != null )
            try{

                this.channel.close();

            }catch(IllegalStateException | TimeoutException | IOException e){

                return;

            }

        if( connection != null )
            try{

                this.connection.close();

            }catch( IOException e ){

                e.printStackTrace();

            }
    }
}