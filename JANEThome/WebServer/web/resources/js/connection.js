

function requestServerLocation(location, address){
    return true;
}

function requestServerSublocation(location, sublocation){
    return true;
}

function renameServerLocation(old_name, new_name){
    return true;
}

function renameServerSublocation(old_name, new_name){
    return true;
}

function addServerDevice(location, sublocation, dID, type){
    return true;
}

function serverLightBrightnessRequest(dID, value){
    return true;
}

function serverLightColorRequest(dID, value){
    return true;
}

function serverFanSpeedRequest(dID, value){
    return true;
}

function serverDoorOpenRequest(dID, value){
    return true;
}

function serverDoorLockRequest(dID, value){
    return true;
}

function serverThermostatTemperatureRequest(dID, temperature){
    return true;
}

function serverDevicePowerRequest(dID, value){
    return true;
}

function serverRenameDevice(old_name, new_name){
    return true;
}

function serverChangeDeviceSublocation(dID, location, new_sublocation){
    return true;
}

function serverStatRequest( dID, stat, start_time, end_time){
    return true;
}

$(document).ready(function(){
    alert("sending");
    //let websocket = new WebSocket("ws://janethome.asuscomm.com:8080/WebServer/controller");
    let websocket = new WebSocket("ws://localhost:8080/WebServer/controller");
    alert("closing");
    websocket.close();

});

