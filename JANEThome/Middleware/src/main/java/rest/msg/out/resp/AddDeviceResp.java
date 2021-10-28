package rest.msg.out.resp;

import java.util.HashMap;

public class AddDeviceResp {

    private final String dev_id;
    private final HashMap<String,String> params;

    AddDeviceResp( String dev_id, HashMap<String,String> params){
        this.dev_id = dev_id;
        this.params = params;
    }

    public String getDev_id(){
        return this.dev_id;
    }

    public HashMap<String,String> getParams(){
        return this.params;
    }
}
