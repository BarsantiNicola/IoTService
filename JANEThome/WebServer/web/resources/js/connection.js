

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

function updateThermostatTemperature(temp, dID){
    let device = document.getElementById("device_"+dID);
    let temp_value = device.getElementsByClassName("temperature_sensor")[0];
    let temp_bar = device.getElementsByClassName("temperature_icon")[0];
    let thermostat = device.getElementsByClassName("thermostat")[0];
    let temp_set = parseInt(device.getElementsByClassName("temp_set")[0].value);
    temp_value.textContent = temp;
    temp_bar.style.paddingBottom = (15+parseInt(temp)*70/40)+"px";
    temp_bar.style.top = (115 - parseInt(temp)*70/40)+"px";
    if( temp_set > parseInt(temp)) {
        thermostat.src = "resources/pics/devices/thermostat-hot.png";
        thermostat.alt = temp;

    }else {
        thermostat.src = "resources/pics/devices/thermostat-off.png";
        thermostat.alt = temp;
    }

}

function updateConditionerTemperature(temp, dID){

    let device = document.getElementById("device_"+dID);
    let temp_value = device.getElementsByClassName("temperature_sensor")[0];
    let temp_bar = device.getElementsByClassName("temperature_icon")[0];
    let thermostat = device.getElementsByClassName("thermostat")[0];
    let temp_set = parseInt(device.getElementsByClassName("temp_set")[0].value);
    temp_value.textContent = temp;
    temp_bar.style.paddingBottom = (15+parseInt(temp)*70/40)+"px";
    temp_bar.style.top = (115 - parseInt(temp)*70/40)+"px";
    if( temp_set > parseInt(temp)) {
        thermostat.src = "resources/pics/devices/thermostat-hot.png";
        thermostat.alt = temp;

    }else {
        thermostat.src = "resources/pics/devices/thermostat-freeze.png";
        thermostat.alt = temp;
    }

}