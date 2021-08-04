package jms;

import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.ConnectionFactory;

///// EndPoint
//
//  Description: class designed as a base class to generate endpoints to
//               the rabbitMQ message exchanger

public abstract class EndPoint{

    protected Channel channel;
    protected Connection connection;

    public EndPoint(){

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setRequestedHeartbeat(60);

        try {

            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare("DeviceUpdate","topic");

        } catch (TimeoutException | IOException e) {

            e.printStackTrace();
            connection = null;
            channel = null;

        }

    }

    // Close channel and connection. Not necessary as it happens implicitly any way.
    public void close(){
        if( channel != null )
            try{

                this.channel.close();

            }catch(TimeoutException | IOException e){

                e.printStackTrace();

            }

        if( connection != null )
            try{

                this.connection.close();

            }catch( IOException e ){

                e.printStackTrace();

            }
    }

}