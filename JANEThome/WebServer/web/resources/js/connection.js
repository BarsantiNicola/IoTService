
let websocket = null;

function requestServerLocation(location, address, port){

    if( websocket == null ) return false;

    let request = {
        "type": "ADD_LOCATION",
        "data": {
            "location": location,
            "address": address,
            "port": port
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function requestServerSublocation(location, sublocation){
    if( websocket == null ) return false;

    let request = {
        "type": "ADD_SUBLOCATION",
        "data": {
            "location": location,
            "sublocation": sublocation
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function renameServerLocation(old_name, new_name){

    if( websocket == null ) return false;

    let request = {
        "type": "RENAME_LOCATION",
        "data": {
            "old_name": old_name,
            "new_name": new_name
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function renameServerSublocation(location, old_name, new_name){
    let request = {
        "type": "RENAME_SUBLOCATION",
        "data": {
            "location": location,
            "old_name": old_name,
            "new_name": new_name
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function renameServerDevice(old_name, new_name){
    let request = {
        "type": "RENAME_DEVICE",
        "data": {
            "old_name": old_name,
            "new_name": new_name
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function addServerDevice(location, sublocation, dID, type){
    let request = {
        "type": "ADD_DEVICE",
        "data": {
            "location": location,
            "sublocation": sublocation,
            "name": dID,
            "type": type
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverChangeDeviceSublocation(dID, location, new_sublocation){
    let request = {
        "type": "CHANGE_SUBLOC",
        "data": {
            "location": location,
            "sublocation": new_sublocation,
            "name": dID,
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverRemoveLocation(location){
    let request = {
        "type": "REMOVE_LOCATION",
        "data": {
            "location": location
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverRemoveSublocation(location, sublocation){
    let request = {
        "type": "REMOVE_SUBLOCATION",
        "data": {
            "location": location,
            "sublocation": sublocation
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverDeleteDevice(name){
    let request = {
        "type": "REMOVE_DEVICE",
        "data": {
            "name": name
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}
function serverStatRequest( dID, stat, start_time, end_time){

    let request = {
        "type": "STATISTIC",
        "data": {
            "device_name": dID,
            "statistic": stat,
            "start": start_time,
            "stop": end_time
        }
    }
    websocket.send(JSON.stringify(request));

    return true;
}

function serverLightBrightnessRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.Brightness",
            "value": value
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverLightColorRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.ColorSetting",
            "value": value
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverFanSpeedRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.FanSpeed",
            "value": value
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverDoorOpenRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.OpenClose",
            "value": value
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverDoorLockRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.LockUnlock",
            "value": value
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverThermostatTemperatureRequest(dID, temperature){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.TemperatureSetting",
            "value": temperature
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function serverDevicePowerRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "action.devices.traits.OnOff",
            "value": value
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

function logout(){
    let request = {
        "type": "LOGOUT"
    }
    websocket.send(JSON.stringify(request));
    return true;
}

let enableUpdate = false;
function messageManager(update){

    if(!enableUpdate){
        try{
            if( update.type ==="EXPIRED_AUTH" ) {
                window.location.replace("login.jsp");
                return;
            }

        }catch(e){}
        createSmarthome(update);
        enableUpdate = true;
        return;
    }

    if( !enableUpdate)
        return;

    switch( update.type ){
        case "RENAME_LOCATION":
            renameLocationReaction(update.data.old_name.toLowerCase(),update.data.new_name.toLowerCase());
            break;

        case "RENAME_SUBLOCATION":
            renameSublocationReaction(update.data.location.toLowerCase(),update.data.old_name.toLowerCase(),update.data.new_name.toLowerCase());
            break;

        case "RENAME_DEVICE":
            renameDeviceReaction(update.data.old_name.toLowerCase(), update.data.new_name.toLowerCase());
            break;

        case "ADD_LOCATION":
            addLocationReaction(update.data.location.toLowerCase());
            break;

        case "ADD_SUBLOCATION":
            addSublocationReaction(update.data.location.toLowerCase(),update.data.sublocation.toLowerCase());
            break;

        case "ADD_DEVICE":
            addDeviceReaction(update.data.location.toLowerCase(),update.data.sublocation.toLowerCase(), update.data.name.toLowerCase(), update.data.type.charAt(0).toUpperCase() + update.data.type.slice(1));
            break;

        case "CHANGE_SUBLOC":
            changeDeviceSublocationAct(update.data.name.toLowerCase(), update.data.location.toLowerCase(), update.data.sublocation.toLowerCase());
            break;

        case "REMOVE_LOCATION":
            deleteLocationReaction(update.data.location.toLowerCase());
            break;

        case "REMOVE_SUBLOCATION":
            deleteSublocationReaction(update.data.location.toLowerCase(),update.data.sublocation.toLowerCase());
            break;

        case "REMOVE_DEVICE":
            deleteDeviceAct(update.data.name.toLowerCase());
            break;

        case "STATISTIC":

            let data = update.data;

            data.values = JSON.parse(data.values);
            for( let a = 0; a<data.values.length; a++) {
                data.values[a].x = new Date(Date.parse(data.values[a].x));
                data.values[a].y =  parseFloat(data.values[a].y);
            }
            updateStatistic( update.data.device_name, update.data.statistic, data.values);
            break;

        case "UPDATE":
            enableDevice( update.data.device_name.toLowerCase(), "1" );
            updateDevice(update.data.device_name.toLowerCase(), update.data.action, update.data.value);
            break;

        case "ERROR_LOCATION":
            errorAddLocation();
            break;

        case "EXPIRED_AUTH":
            window.location.replace("login.jsp");
            break;
        default:
            break;
    }
}

$(document).ready(function(){

    websocket = new WebSocket(((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/WebServer/controller");
    if( websocket === null || websocket === undefined ) return;

    websocket.onmessage = function (event) {
        messageManager(JSON.parse(event.data));
    }

});

function createSmarthome(smarthomeDefinition){

    for( let location of smarthomeDefinition){
        addLocation(location.location.toLowerCase());
        for( let sublocation of location.sublocations) {
            addSublocationAct(location.location.toLowerCase(), sublocation.sublocation.toLowerCase());
            for (let device of sublocation.devices) {
                addDeviceAct(location.location.toLowerCase(), sublocation.sublocation.toLowerCase(), device.name.toLowerCase(), device.type);
                switch(device.type){
                    case "Light":
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.OnOff", device.param.OnOff);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.ColorSetting", device.param.ColorSetting);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.Brightness", device.param.Brightness);
                        break;
                    case "Fan":
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.OnOff", device.param.OnOff);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.FanSpeed", device.param.FanSpeed);
                        break;
                    case "Door":
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.OpenClose", device.param.OpenClose);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.LockUnlock", device.param.LockUnlock);
                        break;
                    case "Conditioner":
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.OnOff", device.param.OnOff);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.TemperatureSetting", device.param.TemperatureSetting);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.FanSpeed", device.param.FanSpeed);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.Temperature", device.param.Temperature);
                        break;
                    case "Thermostat":
                        alert("TEMP: " + device.param.TemperatureSetting);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.TemperatureSetting", device.param.TemperatureSetting);
                        updateDevice(device.name.toLowerCase(), "action.devices.traits.Temperature", device.param.Temperature);
                        break;
                }
                enableDevice(device.name.toLowerCase(), device.connectivity);
            }
        }
    }

}

