
function createDevice(type,name) {

    let device = document.createElement('div');
    let header_wrapper = document.createElement('div');
    let header = document.createElement("h1");
    let status_icon = document.createElement("i");
    let expand_icon = document.createElement("i");
    let type_info = document.createElement("input");

    device.id = "device_" + name;
    device.className = "device";

    header_wrapper.className = "device_header_wrapper";

    header.className = "device_title";
    header.textContent = name + "[" + type + "]";
    status_icon.className = "fa fa-circle";
    expand_icon.className = "fa fa-arrows-alt";

    type_info.type = "hidden";
    type_info.className = "type";
    type_info.value = type;

    header_wrapper.appendChild(header);
    header_wrapper.appendChild(status_icon);
    header_wrapper.appendChild(expand_icon);
    header_wrapper.appendChild(type_info);

    device.appendChild(header_wrapper);

    switch (type) {
        case "Light":
            device.appendChild(createLight(name));
            break;
        case "Fan":
            device.appendChild(createFan(name));
            break;
        case "Door":
            device.appendChild(createDoor(name));
            break;
        case "Thermostat":
            device.appendChild(createThermostat(name));
            break;
        case "Conditioner":
            device.appendChild(createConditioner(name));
            break;
        default:

    }
    return device;
}

//////////// Light DEVICE

function createLight(dID){

    let wrapper = document.createElement("div");
    let buttons_container = document.createElement("div");

    let pic = document.createElement("img");
    let pic_container = document.createElement("div");
    let light_ball = document.createElement("div");

    let brightness_title = document.createElement("p");
    let bright_wrapper = document.createElement("div");
    let bright_input = document.createElement("input");
    let bright_value = document.createElement("p");

    let color_title = document.createElement("p");
    let color_input = document.createElement("input");
    let on_off = document.createElement("input");
    let color = document.createElement("input");
    let brightness = document.createElement("input");

    wrapper.className = "device_body_wrapper";
    pic_container.className = "img_container";
    buttons_container.className = "device_buttons";

    pic.alt = "Light-off";
    pic.className = "brightness_pic";
    pic.src = "resources/pics/devices/light-off.png";
    light_ball.className = "light_ball";
    pic_container.appendChild(pic);
    pic_container.appendChild(light_ball);
    pic_container.addEventListener("click", function () {
        requestPowerDevice(dID);
    })

    brightness_title.textContent = "Brightness:";
    bright_input.className = "brightness_input";
    bright_input.type = "range";
    bright_input.min = "0";
    bright_input.max = "100";
    bright_input.value = "0";
    bright_input.addEventListener("change", function () {
        requestDeviceBrightness(dID, this.value);
    });

    bright_value.className = "brightness_value";
    bright_value.textContent = "0";
    bright_wrapper.className = "brightness_wrapper";

    bright_wrapper.appendChild(bright_input);
    bright_wrapper.appendChild(bright_value);

    color_title.textContent = "Color:";
    color_input.className = "color_input";
    color_input.type = "color";
    color_input.value = "#ECFF00";

    color_input.addEventListener("change", function () {
        requestDeviceColor(dID, this.value);
    })

    on_off.type = "hidden";
    on_off.className = "on_off";
    on_off.value = "0";

    color.type = "hidden";
    color.className = "color";
    color.value = "#ECFF00";

    brightness.type = "hidden";
    brightness.className = "brightness";
    brightness.value = "0";

    buttons_container.appendChild(brightness_title);
    buttons_container.appendChild(bright_wrapper);
    buttons_container.appendChild(color_title);
    buttons_container.appendChild(color_input);
    buttons_container.appendChild(on_off);
    buttons_container.appendChild(brightness);
    buttons_container.appendChild(color);

    wrapper.appendChild(pic_container);
    wrapper.appendChild(buttons_container);
    return wrapper;
}

function requestDeviceBrightness(dID,value){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let brightness = device.getElementsByClassName("brightness")[0];
    if( brightness === undefined )
        return;

    if( serverLightBrightnessRequest(dID, value) &&  changeDeviceBrightness(dID, value))
        return;

    device.getElementsByClassName("brightness_input")[0].value = brightness.value;

}

function changeDeviceBrightness(dID, value){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return false;

    let bright_input = device.getElementsByClassName("brightness_input")[0];
    let bright_value = device.getElementsByClassName("brightness_value")[0];
    let brightness_icon = device.getElementsByClassName("light_ball")[0];
    let brightness = device.getElementsByClassName("brightness")[0];

    if( brightness_icon === undefined || bright_value === undefined || bright_input === undefined )
        return false;

    bright_input.value = value;
    bright_value.textContent = value;
    brightness_icon.style.opacity = "" + (parseInt(value) / 100);
    brightness.value = value;
    return true;

}

function requestDeviceColor(dID,value){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let color = device.getElementsByClassName("color")[0];
    if( serverLightColorRequest(dID, value) &&  changeDeviceColor(dID, value))
        return;

    device.getElementsByClassName("color_input")[0].value = color.value;

}

function changeDeviceColor( dID, color ){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return false;

    device.getElementsByClassName("light_ball")[0].style.boxShadow = "0 0 60px 40px " + color;
    device.getElementsByClassName("color")[0].value = color;

}


//////////// Fan DEVICE


function createFan(dID){

    let wrapper = document.createElement("div");
    let buttons_container = document.createElement("div");

    let pic = document.createElement("img");
    let pic_container = document.createElement("div");

    let speed_title = document.createElement("p");
    let speed_wrapper = document.createElement("div");
    let speed_input = document.createElement("input");
    let speed_value = document.createElement("p");

    let on_off = document.createElement("input");
    let speed = document.createElement("input");

    wrapper.className = "device_body_wrapper";
    pic_container.className = "img_container";
    buttons_container.className = "device_buttons";

    pic.alt = "Fan-off";
    pic.className = "fan_pic";
    pic.src = "resources/pics/devices/fan-off.png";
    pic.style.rotate = "0deg";

    pic_container.appendChild(pic);
    pic_container.addEventListener("click", function () {
        requestPowerDevice(dID);
    })


    speed_title.textContent = "Speed:"
    speed_input.className = "speed_input";
    speed_input.type = "range";
    speed_input.min = "0";
    speed_input.max = "100";
    speed_input.value = "0";
    speed_input.addEventListener("change", function () {
        requestDeviceSpeed(dID, this.value);
    });

    speed_value.className = "speed_value";
    speed_value.textContent = "0";
    speed_wrapper.className = "speed_wrapper";

    speed_wrapper.appendChild(speed_input);
    speed_wrapper.appendChild(speed_value);

    on_off.type = "hidden";
    on_off.className = "on_off";
    on_off.value = "0";

    speed.type = "hidden";
    speed.className = "speed";
    speed.value = "0";

    buttons_container.appendChild(speed_title);
    buttons_container.appendChild(speed_wrapper);
    buttons_container.appendChild(on_off);
    buttons_container.appendChild(speed);

    wrapper.appendChild(pic_container);
    wrapper.appendChild(buttons_container);
    setTimeout(function(){fanRotate(wrapper)}, 1000);
    return wrapper;

}

function requestDeviceSpeed(dID,value){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let speed = device.getElementsByClassName("speed")[0];
    if( serverFanSpeedRequest(dID, value) &&  changeDeviceSpeed(dID, value))
        return;

    device.getElementsByClassName("speed_input")[0].value = speed.value;

}

function changeDeviceSpeed(dID, value){
    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return false;

    let speed_input = device.getElementsByClassName("speed_input")[0];
    let speed_value = device.getElementsByClassName("speed_value")[0];
    let speed = device.getElementsByClassName("speed")[0];

    if( speed_value === undefined || speed_input === undefined || speed === undefined )
        return false;

    speed_input.value = value;
    speed_value.textContent = value;
    speed.value = value;
    return true;
}

function fanRotate(device){

    let on_off = device.getElementsByClassName("on_off")[0];
    let speed = device.getElementsByClassName("speed_value")[0];
    let fan_pic = device.getElementsByClassName("fan_pic")[0];

    if( on_off === undefined || speed === undefined || fan_pic === undefined)
        return;

    on_off = on_off.value;
    speed = speed.textContent;

    if( on_off !== "0" && speed !== "0"){
        let rotation = fan_pic.style.rotate.replace("deg","");
        rotation = parseInt(rotation)+speed/4;
        fan_pic.style.transform = "rotate("+ rotation+"deg)";
        fan_pic.style.rotate = rotation+"deg";

    }
    setTimeout(function(){fanRotate(device);}, 250);

}


//////////// Door DEVICE


function createDoor( dID ){

    let wrapper = document.createElement("div");
    let buttons_container = document.createElement("div");

    let pic = document.createElement("img");
    let pic_container = document.createElement("div");

    let lock_title = document.createElement("p");
    let lock_input = document.createElement("i");

    let open_close = document.createElement("input");
    let lock_unlock = document.createElement("input");

    wrapper.className = "device_body_wrapper";
    pic_container.className = "img_container";
    buttons_container.className = "device_buttons";

    pic.alt = "Door-close";
    pic.className = "door_pic";
    pic.src = "resources/pics/devices/door-closed.png";

    pic_container.appendChild(pic);
    pic_container.addEventListener("click", function () {
        requestDeviceDoor(dID);
    })

    lock_title.textContent = "Lock:";
    lock_input.className = "fa fa-unlock lock_icon";
    lock_input.addEventListener("click", function () {
        requestDeviceLock(dID);
    });

    open_close.type = "hidden";
    open_close.className = "open_close";
    open_close.value = "0";

    lock_unlock.type = "hidden";
    lock_unlock.className = "lock_unlock";
    lock_unlock.value = "0";

    buttons_container.appendChild(lock_title);
    buttons_container.appendChild(lock_input);
    buttons_container.appendChild(open_close);
    buttons_container.appendChild(lock_unlock);

    wrapper.appendChild(pic_container);
    wrapper.appendChild(buttons_container);
    return wrapper;

}

function requestDeviceDoor(dID){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let value;
    switch(device.getElementsByClassName("open_close")[0].value){
        case "0":
            value = "1";
            break;
        case "1":
            value = "0";
            break;
        default:
            return;
    }

    if( serverDoorOpenRequest(dID, value))
        openDoor(dID, value);

}

function openDoor(dID, open){

    let device = document.getElementById("device_" + dID);

    if( device === undefined )
        return false;

    let door_state = device.getElementsByClassName("open_close")[0];
    let door_pic = device.getElementsByClassName("door_pic")[0];

    if( door_state === undefined || door_pic === undefined)
        return false;

    if( open === "0" ) {

        door_pic.src = "resources/pics/devices/door-closed.png";
        door_pic.alt = "Door Closed";
        door_state.value = "0";

    }else {

        door_pic.src = "resources/pics/devices/door-open.png";
        door_pic.alt = "Door Open";
        door_state.value = "1";

    }
    return true;

}

function requestDeviceLock(dID){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let value;
    switch(device.getElementsByClassName("lock_unlock")[0].value){
        case "0":
            value = "1";
            break;
        case "1":
            value = "0";
            break;
        default:
            return;
    }

    if( serverDoorLockRequest(dID, value))
        lockDoor(dID, value);

}

function lockDoor(dID, lock){

    let device = document.getElementById("device_" + dID);

    if( device === undefined )
        return false;

    let lock_state = device.getElementsByClassName("lock_unlock")[0];
    let lock_icon = device.getElementsByClassName( "lock_icon")[0];
    if( lock_state === undefined )
        return false;

    if( lock === "0" ) {

        lock_icon.className = "fa fa-unlock lock_icon";
        lock_icon.alt = "Door-Unlock";
        lock_state.value = lock;

    }else {

        lock_icon.className = "fa fa-lock lock_icon";
        lock_icon.alt = "Door-Lock";
        lock_state.value = lock;

    }

    return true;
}


//////////// Thermostat DEVICE


function createThermostat( dID ){

    let wrapper = document.createElement("div");
    let buttons_container = document.createElement("div");

    let pic = document.createElement("img");
    let pic_container = document.createElement("div");

    let temperature_title = document.createElement("p");
    let temperature_input = document.createElement("input");
    let temperature_bar = document.createElement("div");
    let temperature_sensor = document.createElement("p");

    let env_temperature = document.createElement("input");
    let temperature = document.createElement("input");

    wrapper.className = "device_body_wrapper";
    pic_container.className = "img_container";
    buttons_container.className = "device_buttons";

    pic.alt = "Thermostat-off";
    pic.className = "thermostat_pic";
    pic.src = "resources/pics/devices/thermostat-hot.png";

    temperature_bar.className = "temperature_bar";
    temperature_sensor.textContent = "6";
    temperature_sensor.className = "temperature_sensor";

    pic_container.appendChild(temperature_sensor);
    pic_container.appendChild(temperature_bar);
    pic_container.appendChild(pic);

    temperature_title.textContent = "Temperature Set:";

    temperature_input.className = "set_temperature";
    temperature_input.type = "number";
    temperature_input.min = "6";
    temperature_input.max = "40";
    temperature_input.value = "6";
    temperature_input.step = "0.1";
    temperature_input.addEventListener("change", function(){requestDeviceTemperature(dID, this.value);})

    env_temperature.type = "hidden";
    env_temperature.className = "env_temperature";
    env_temperature.value = "6";

    temperature.type = "hidden";
    temperature.className = "temperature";
    temperature.value = "6";

    buttons_container.appendChild(temperature_title);
    buttons_container.appendChild(temperature_input);
    buttons_container.appendChild(env_temperature);
    buttons_container.appendChild(temperature);

    wrapper.appendChild(pic_container);
    wrapper.appendChild(buttons_container);
    return wrapper;

}

function requestDeviceTemperature(dID, temperature){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let temp = device.getElementsByClassName("temperature")[0];

    if( temp === undefined )
        return;

    if( serverThermostatTemperatureRequest(dID, temperature) &&  changeTemperature(dID, temperature))
        return;

    device.getElementsByClassName("set_temperature")[0].value = temp.value;

}

function changeTemperature(dID, temperature){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return false;

    let temp = device.getElementsByClassName("temperature")[0];
    let env = device.getElementsByClassName("env_temperature")[0];
    let thermostat = device.getElementsByClassName("thermostat_pic")[0];
    let type = device.getElementsByClassName("type")[0];

    if( temp === undefined || thermostat === undefined || type === undefined )
        return false;

    temp.value = temperature;
    alert(parseFloat(temp.value));
    alert(parseFloat(env.value));
    switch( type.value ){
        case "Thermostat":
            alert("therm");
            if( parseFloat(temp.value) < parseFloat(env.value)){
                thermostat.src = "resources/pics/devices/thermostat-hot.png";
                thermostat.alt = "Thermostat-on";
            }else{
                thermostat.src = "resources/pics/devices/thermostat-off.png";
                thermostat.alt = "Thermostat-off";
            }
            break;

        case "Conditioner":
            alert("cond");
            if( parseFloat(temp.value) < parseFloat(env.value)){
                thermostat.src = "resources/pics/devices/thermostat-hot.png";
                thermostat.alt = "Conditioner-hot";
            }else{
                thermostat.src = "resources/pics/devices/thermostat-freeze.png";
                thermostat.alt = "Conditioner-off";
            }
            break;

        default:
            alert(type);
            return false;
    }

    return true;

}

function updateEnvironmentTemperature(dID, temp){

    let device = document.getElementById("device_"+dID);

    if( device === undefined )
        return false;

    let type = device.getElementsByClassName("type")[0];
    let env_temperature = device.getElementsByClassName("env_temperature")[0];
    let set_temperature = device.getElementsByClassName("set_temperature")[0];
    let temperature_bar = device.getElementsByClassName("temperature_bar")[0];
    let temperature_sensor = device.getElementsByClassName("temperature_sensor")[0];
    let thermostat = device.getElementsByClassName("thermostat_pic")[0];

    if( type === undefined || env_temperature === undefined || set_temperature === undefined
        || temperature_bar === undefined || temperature_sensor === undefined )
        return false;

    set_temperature = set_temperature.value;
    temperature_sensor.textContent = temp;
    env_temperature.value = temp;
    temperature_bar.style.paddingBottom = (15+parseInt(temp)*70/40)+"px";
    temperature_bar.style.top = (115 - parseInt(temp)*70/40)+"px";

    switch( type.value ){
        case "Thermostat":
            if( parseFloat(set_temperature) > parseFloat(temp)){
                thermostat.src = "resources/pics/devices/thermostat-hot.png";
                thermostat.alt = "Thermostat-on";
            }else{
                thermostat.src = "resources/pics/devices/thermostat-off.png";
                thermostat.alt = "Thermostat-off";
            }
            break;

        case "Conditioner":
            if( parseInt(set_temperature) > parseInt(temp)){
                thermostat.src = "resources/pics/devices/thermostat-hot.png";
                thermostat.alt = "Conditioner-hot";
            }else{
                thermostat.src = "resources/pics/devices/thermostat-freeze.png";
                thermostat.alt = "Conditioner-off";
            }
            break;

        default:
            alert(type);
            return false;
    }
    return true;

}

//////////// Conditioner DEVICE


function createConditioner( dID ){

    let wrapper = document.createElement("div");
    let buttons_container = document.createElement("div");

    let pic = document.createElement("img");
    let pic2 = document.createElement("img");
    let pic_container = document.createElement("div");

    let temperature_title = document.createElement("p");
    let temperature_input = document.createElement("input");
    let temperature_bar = document.createElement("div");
    let temperature_sensor = document.createElement("p");
    let temperature = document.createElement("input");

    let env_temperature = document.createElement("input");

    let speed_title = document.createElement("p");
    let speed_wrapper = document.createElement("div");
    let speed_input = document.createElement("input");
    let speed_value = document.createElement("p");

    let on_off = document.createElement("input");
    let speed = document.createElement("input");

    wrapper.className = "device_body_wrapper";
    pic_container.className = "img_container conditioner_img_container";
    buttons_container.className = "device_buttons";

    pic.alt = "Conditioner-freeze";
    pic.className = "thermostat_pic conditioner_pic";
    pic.src = "resources/pics/devices/thermostat-hot.png";

    pic2.alt = "Fan-off";
    pic2.className = "fan_pic condition_fan";
    pic2.src = "resources/pics/devices/fan-off.png";
    pic2.style.rotate = "0deg";

    temperature_bar.className = "temperature_bar conditioner_bar";
    temperature_sensor.textContent = "6";
    temperature_sensor.className = "temperature_sensor conditioner_sensor";

    pic_container.appendChild(temperature_sensor);
    pic_container.appendChild(temperature_bar);
    pic_container.appendChild(pic2);
    pic_container.appendChild(pic);
    pic_container.addEventListener("click", function () {
        requestPowerDevice(dID, "1");
    })

    temperature_title.textContent = "Temperature Set:";

    temperature_input.className = "set_temperature";
    temperature_input.type = "number";
    temperature_input.min = "6";
    temperature_input.max = "40";
    temperature_input.value = "6";
    temperature_input.step = "0.1";
    temperature_input.addEventListener("change", function(){requestDeviceTemperature(dID, this.value);})

    env_temperature.type = "hidden";
    env_temperature.className = "env_temperature";
    env_temperature.value = "6";

    temperature.type = "hidden";
    temperature.className = "temperature";
    temperature.value = "6";

    speed_title.textContent = "Speed:"
    speed_input.className = "speed_input";
    speed_input.type = "range";
    speed_input.min = "0";
    speed_input.max = "100";
    speed_input.value = "0";
    speed_input.addEventListener("change", function () {
        requestDeviceSpeed(dID, this.value);
    });

    speed_value.className = "speed_value";
    speed_value.textContent = "0";
    speed_wrapper.className = "speed_wrapper";

    speed_wrapper.appendChild(speed_input);
    speed_wrapper.appendChild(speed_value);

    on_off.type = "hidden";
    on_off.className = "on_off";
    on_off.value = "0";

    speed.type = "hidden";
    speed.className = "speed";
    speed.value = "0";

    buttons_container.appendChild(speed_title);
    buttons_container.appendChild(speed_wrapper);
    buttons_container.appendChild(on_off);
    buttons_container.appendChild(speed);
    buttons_container.appendChild(temperature_title);
    buttons_container.appendChild(temperature_input);
    buttons_container.appendChild(env_temperature);
    buttons_container.appendChild(temperature);

    wrapper.appendChild(pic_container);
    wrapper.appendChild(buttons_container);
    setTimeout(function(){fanRotate(wrapper)}, 1000);
    return wrapper;

}


//////////// GENERAL

function requestPowerDevice( dID ){

    let device = document.getElementById("device_"+dID);
    if( device === undefined )
        return;

    let value;
    let on_off = device.getElementsByClassName("on_off")[0];

    if( on_off === undefined )
        return;

    switch(on_off.value){
        case "0":
            value = "1";
            break;
        case "1":
            value = "0";
            break;
        default:
            return;
    }

    if( serverDevicePowerRequest(dID, value))
        powerDevice(dID, value);

}

function powerDevice(dID, power){
    let device = document.getElementById("device_" + dID );
    if( device === undefined || (power !== "0" && power !== "1"))
        return false;

    device.getElementsByClassName("on_off")[0].value = power;

    switch(device.getElementsByClassName("type")[0].value){
        case "Light":
            if( power === "0" ){
                device.getElementsByClassName("brightness_pic")[0].src = "resources/pics/devices/light-off.png";
                device.getElementsByClassName("brightness_pic")[0].alt = "Light-off";
                device.getElementsByClassName("light_ball")[0].style.display = "none";
            }else{
                device.getElementsByClassName("brightness_pic")[0].src = "resources/pics/devices/light-on.png";
                device.getElementsByClassName("brightness_pic")[0].alt = "Light-on";
                device.getElementsByClassName("light_ball")[0].style.display = "block";
            }
            break;
        case "Fan":
            if( power === "0" ){
                device.getElementsByClassName("fan_pic")[0].src = "resources/pics/devices/fan-off.png";
                device.getElementsByClassName("fan_pic")[0].alt = "Fan-off";
            }else{
                device.getElementsByClassName("fan_pic")[0].src = "resources/pics/devices/fan-on.png";
                device.getElementsByClassName("fan_pic")[0].alt = "Fan-on";
            }
            break;
        case "Conditioner":
            if( power === "0" ){
                device.getElementsByClassName("fan_pic")[0].src = "resources/pics/devices/fan-off.png";
                device.getElementsByClassName("fan_pic")[0].alt = "Conditioner-off";
            }else{
                device.getElementsByClassName("fan_pic")[0].src = "resources/pics/devices/fan-on.png";
                device.getElementsByClassName("fan_pic")[0].alt = "Conditioner-on";
            }
            break;
        default:
            return false;
    }
    return true;

}

function enableDevice(dID, status){

    let device = document.getElementById("device_"+dID);

    if( device === undefined )
        return false;

    switch(status){
        case "0":
            device.getElementsByClassName("fa-circle")[0].style.color = "red";
            device.getElementsByClassName("device_body_wrapper")[0].style.display = "none";
            break;
        case "1":
            device.getElementsByClassName("fa-circle")[0].style.color = "#57b846";
            device.getElementsByClassName("device_body_wrapper")[0].style.display = "none";
            break;
        default:
            return false;
    }
    return true;
}





