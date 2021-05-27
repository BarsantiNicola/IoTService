
let websocket = null;

function requestServerLocation(location, address){

    if( websocket == null ) return false;

    let request = {
        "type": "ADD_LOCATION",
        "data": {
            "location": location,
            "address": address
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
    alert("send: " + dID);
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
    alert("sent");
    return true;
}

function serverLightBrightnessRequest(dID, value){
    let request = {
        "type": "UPDATE",
        "data": {
            "device_name": dID,
            "action": "brightness",
            "value": value,
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
            "action": "color",
            "value": value,
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
            "action": "speed",
            "value": value,
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
            "action": "openClose",
            "value": value,
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
            "action": "lockUnlock",
            "value": value,
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
            "action": "temperature",
            "value": value,
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
            "action": "onOff",
            "value": value,
        }
    }
    websocket.send(JSON.stringify(request));
    return true;
}

let enableUpdate = false;
function messageManager(update){

    if( update.type === "START_SESSION" && !enableUpdate){
        createSmarthome(update);
        enableUpdate = true;
        return;
    }

    if( !enableUpdate)
        return;

    switch( update.type ){
        case "RENAME_LOCATION":
            renameLocationReaction(update.data.old_name,update.data.new_name);
            break;
        case "RENAME_SUBLOCATION":
            renameSublocationReaction(update.data.location,update.data.old_name,update.data.new_name);
            break;
        case "RENAME_DEVICE":
            renameDeviceReaction(update.data.old_name, update.data.new_name);
            break;
        case "ADD_LOCATION":
            addLocationReaction(update.data.location);
            break;
        case "ADD_SUBLOCATION":
            addSublocationReaction(update.data.location,update.data.sublocation);
            break;
        case "ADD_DEVICE":
            addDeviceReaction(update.data.location,update.data.sublocation, update.data.name, update.data.type);
            break;
        case "CHANGE_SUBLOC":
            changeDeviceSublocationAct(update.data.name, update.data.location, update.data.sublocation);
            break;
        case "REMOVE_LOCATION":
            deleteLocationReaction(update.data.location);
            break;
        case "REMOVE_SUBLOC":
            deleteSublocationReaction(update.data.location,update.data.sublocation);
            break;
        case "REMOVE_DEVICE":
            deleteDeviceAct(update.data.name);
            break;
        case "STATISTIC":
            let data = JSON.parse(update.data.values);
            for( let a = 0; a<data.length; a++)
                data[a].x = new Date(Date.parse(data[a].x));
            updateStatistic( update.data.name, update.data.statistic, data);
            break;
        case "UPDATE":
            updateDevice(update.data.name, update.data.action, update.data.value);
        default: break;
    }
}

$(document).ready(function(){
    //let websocket = new WebSocket("ws://janethome.asuscomm.com:8080/WebServer/controller");
    websocket = new WebSocket("ws://localhost:8080/WebServer/controller");
    if( websocket === null || websocket === undefined ) return;

    websocket.onmessage = function (event) {
        message = event.data.substr(event.data.indexOf("{"))

        alert(message);
        messageManager(JSON.parse(message));
    }

});

function createSmarthome(smarthomeDefinition){

    for( let location of smarthomeDefinition.locations){
        alert(location.name);
        addLocation(location.name);
        for( let sublocation of location.sublocations) {
            addSublocationAct(location.name, sublocation.name);
            for (let device of sublocation.devices)
                addDeviceAct(location.name, sublocation.name, device.name, device.type);
        }
    }

}

