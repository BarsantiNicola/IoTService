package rest.in;

import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.UpdateRequest;
import rest.out.UpdateMessage;

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

            DeviceUpdateMessage message = new DeviceUpdateMessage(data.getUser());
            for(UpdateRequest request : data.getRequests())
                message.addUpdates( DeviceUpdate.buildDeviceUpdate(request.getdID(), request.getAction(), request.getValue()));

            if( sender.sendMessage( message) < 0 )
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error has occurred during the forwarding of the request")
                        .build();

            return Response
                    .status(Response.Status.OK)
                    .entity("Request correctly managed")
                    .build();

        }catch( InvalidMessageException e ){

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Error invalid username")
                    .build();

        }

    }
}