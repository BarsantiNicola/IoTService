package rest.in;

import com.google.gson.Gson;
import rabbit.msg.DeviceUpdate;
import rabbit.msg.DeviceUpdateMessage;
import rabbit.msg.InvalidMessageException;
import rabbit.out.interfaces.SenderInterface;
import rest.msg.in.UpdateRequest;
import rest.msg.in.StateResponse;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/")
public class RESTserver{

    @EJB
    SenderInterface sender;

    private static final String[] receivedTraits = {
            "onOff",
            "fanSpeed",
            "brightness",
            "color",
            "openClose",
            "lockUnlock",
            "tempTarget",
            "tempCurrent",
            "connectivity"
    };

    private static final String[] convertedTraits = {
            "action.devices.traits.OnOff",
            "action.devices.traits.FanSpeed",
            "action.devices.traits.Brightness",
            "action.devices.traits.ColorSetting",
            "action.devices.traits.OpenClose",
            "action.devices.traits.LockUnlock",
            "action.devices.traits.TemperatureSetting",
            "action.devices.traits.Temperature",
            "action.devices.traits.Connectivity"
    };

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response UpdateDevice(List<UpdateRequest> data){
        Gson gson = new Gson();
        int state = 0;
        List<StateResponse> responses = new ArrayList<>();
        try {

            for( UpdateRequest request: data ){

                String verify = request.getDev_id();
                if( verify == null || verify.length() == 0 ){
                    state++;
                    responses.add(StateResponse.buildError("0"));
                    continue;
                }

                verify = request.getUser();
                if( verify == null || verify.length() == 0 ){
                    state++;
                    responses.add(StateResponse.buildError(request.getDev_id()));
                    continue;
                }

                DeviceUpdateMessage message = new DeviceUpdateMessage(request.getUser(), "async_devices" );
                responses.add( StateResponse.buildSuccess(request.getDev_id()));
                request.setTimestamp( "\"" + request.getTimestamp() + "\"" );
                request.getActions().forEach( (key,value) -> {
                    if(verifyRequest(request)) {
                        message.addUpdates(DeviceUpdate.buildDeviceUpdate(request.giveConvertedTimestamp(), request.getDev_id(), this.bridgeTrait(key), value));
                        sender.sendMessage( message);
                    }
                });
            }

            if( state == 0 ){
                return Response
                        .status(Response.Status.OK)
                        .entity(gson.toJson(responses))
                        .build();
            }else{
                if( state == data.size())
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(gson.toJson(responses))
                            .build();
                else
                    return Response
                            .status(Response.Status.ACCEPTED)
                            .entity(gson.toJson(responses))
                            .build();
            }

        }catch( InvalidMessageException e ){

            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(gson.toJson(responses))
                    .build();

        }
    }

    private boolean verifyRequest( UpdateRequest request ){

        String id = request.getDev_id();
        String timestamp = request.getTimestamp();
        String user = request.getUser();

        try{
            request.giveConvertedTimestamp();
        }catch( ClassCastException e ){
            return false;
        }

        return id!= null && id.length() > 0 && timestamp != null && timestamp.length() > 0 &&
                user != null && user.length() > 0;
    }

    private String bridgeTrait( String trait ){

        int index = this.getIndex( trait );
        if( index == -1 )
            return "";
        else
            return RESTserver.convertedTraits[index];

    }

    private int getIndex( String trait ){

        for( int a = 0; a<RESTserver.receivedTraits.length; a++ )
            if( RESTserver.receivedTraits[a].compareTo(trait) == 0)
                return a;
        return -1;
    }
}