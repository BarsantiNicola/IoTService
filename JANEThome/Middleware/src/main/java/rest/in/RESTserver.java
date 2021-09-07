package rest.in;

import config.interfaces.GeneratorInterface;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.msg.UpdateRequest;
import rest.msg.UpdateMessage;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class RESTserver{

    @EJB
    SenderInterface sender;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response UpdateDevice(UpdateMessage data){
        try {

            if( data.getUser() == null || data.getUser().length() == 0 )
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Error invalid request")
                        .build();


            DeviceUpdateMessage message = new DeviceUpdateMessage( data.getUser(), "async_devices" );
            int count = 0;
            for(UpdateRequest request : data.getRequests()) {
                request.setTimestamp( "\"" + request.getTimestamp() + "\"" );
                if (verifyRequest(request)) {

                    message.addUpdates(DeviceUpdate.buildDeviceUpdate(request.giveConvertedTimestamp(), request.getdID(), request.getAction(), request.getValue()));
                    count++;
                }
            }

            if( sender.sendMessage( message) < 1 )
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error has occurred during the forwarding of the request")
                        .build();

            return Response
                    .status(Response.Status.OK)
                    .entity("Request correctly managed. Forwarded " + count + " of " + data.getRequests().size() + " updates" )
                    .build();

        }catch( InvalidMessageException e ){

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Error invalid request")
                    .build();

        }
    }

    private boolean verifyRequest( UpdateRequest request ){

        String id = request.getdID();
        String timestamp = request.getTimestamp();
        String action = request.getAction();
        String value = request.getValue();

        try{
            request.giveConvertedTimestamp();
        }catch( ClassCastException e ){
            return false;
        }

        return id!= null && id.length() > 0 && timestamp != null && timestamp.length() > 0 &&
                action != null && action.length() > 0 && value != null && value.length() > 0;
    }
}