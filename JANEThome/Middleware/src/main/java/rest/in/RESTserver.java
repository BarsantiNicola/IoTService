package rest.in;

//  internal services
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.out.interfaces.IRabbitSender;
import rest.DeviceBridge;
import rest.msg.in.UpdateRequest;

//  exceptions
import rabbit.msg.InvalidMessageException;

//  ejb3.0
import javax.ejb.EJB;

//  jersey REST management
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//  collections
import java.util.List;


/**
 * REST server to manage asynchronous updates from the smartHomes and propagate them
 * to the internal service after performing a bridging of the requests to their service representation.
 * The server is available on http[s]://serviceHostname:servicePort/deviceUpdate
 */
@Path("/")
public class RESTserver{

    @EJB
    IRabbitSender sender;

    /**
     * POST request to update a device
     * LINK: POST http[s]://serviceHostname:servicePort/deviceUpdate
     * BODY example:
     *
     * [
     *  {
     *    "dev_id" : 123,
     *    "timestamp" : ",
     *    "user" : "example@service.it",
     *    "actions" : {
     *        "onOff" : "on",
     *        "brightness" : 57,
     *        "colorSetting": "#a523d6"
     *    }
     *  }
     * ]
     *
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response UpdateDevice( List<UpdateRequest> data ){


        int errorState = 0;

        for( UpdateRequest request: data ){

            //  verification that the request is valid(all the needed fields present and well formatted)
            if( !verifyRequest( request )){
                errorState++;
                continue;
            }

            try {

                //  generation of message, from field irrelevant(REST server will not receive any message from rabbitMQ)
                DeviceUpdateMessage message = new DeviceUpdateMessage( request.getUser(), "async_devices");

                //  a single request to a user can contain many update requests
                request.getActions().forEach( (key,value) -> {

                    message.addUpdates(
                            DeviceUpdate.buildDeviceUpdate(
                                    request.giveConvertedTimestamp(),
                                    request.getDev_id(),
                                    DeviceBridge.controllerToServiceTrait( key ),
                                    DeviceBridge.controllerToServiceValue( String.valueOf( value ))));

                    sender.sendMessage( message);

                });

            }catch( InvalidMessageException e ){

                //  invalid username field cause the raise of an Exception into DeviceUpdateMessage constructor
                errorState++;

            }

        }

        //  basing on the failure we decides the response to give back
        if( errorState == 0 )  //  no errors, all request well forwarded
            return Response
                    .status( Response.Status.OK )
                    .build();
            else
                if( errorState == data.size() )  //  all requests make an erroR
                    return Response
                            .status( Response.Status.BAD_REQUEST )
                            .build();
                else //  not all the requests made an error
                    return Response
                            .status( Response.Status.ACCEPTED )
                            .build();
    }


    ////////--  UTILITIES  --////////


    /**
     * Method to verify that a received message has all the mandatory information
     * @param request {@link UpdateRequest} Request to be verified
     * @return True in case of success otherwise false
     */
    private boolean verifyRequest( UpdateRequest request ){

        String id = request.getDev_id();
        String timestamp = request.getTimestamp();
        String user = request.getUser();

        try{

            //  verification of timestamp correctly formatted
            request.giveConvertedTimestamp();

        }catch( ClassCastException e ){

            return false;

        }

        //  verification of fields presence
        return id!= null && id.length() > 0 &&
                    timestamp != null && timestamp.length() > 0 &&
                        user != null && user.length() > 0;

    }

}