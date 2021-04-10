
function createDevice(type,name){

    let device = document.createElement('div');
    let header_wrapper = document.createElement('div');
    let wrapper = document.createElement("div");
    let range_wrapper = document.createElement("div");
    let col = document.createElement("div");
    let header = document.createElement("h1");
    let pic = document.createElement("img");
    let expand = document.createElement("div");
    let range_value = document.createElement("p");
    let expand_icon = document.createElement("i");
    let status_icon = document.createElement("i");
    let button = document.createElement("div");

    expand_icon.className="fa fa-arrows-alt";
    expand_icon.style.fontSize = "25px";
    expand_icon.style.fontWeight = "lighter";
    status_icon.className = "fa fa-circle";
    wrapper.className = "device_wrapper";
    device.className="device";
    header_wrapper.className="device_header_wrapper";


    device.id = "device_"+name;
    header.textContent = name +"["+type+"]";
    status_icon.style.color= "#FF4500";
    status_icon.style.fontSize ="15px";
    header_wrapper.appendChild(header);
    header_wrapper.appendChild(status_icon);

    header.className = "device_title";
    button.className = "img_container";
    expand.className = "device_expander";
    expand.appendChild(expand_icon);
    col.className = "device_buttons";
    col.appendChild(expand);
    device.appendChild(header_wrapper);

    let bright_title, range, color_title, color;
    let light_ball,icon,temperature, fan = null;
    switch(type){
        case "Light":
            bright_title = document.createElement("p");
            bright_title.textContent = "Brightness"
            light_ball = document.createElement("div");
            light_ball.className = "light_ball";
            range = document.createElement("input");
            range_wrapper.className = "device_range_wrapper";
            range_value.className = "device_range_value";
            range.style.width="130px";
            range.type = "range";
            range.min = "0";
            range.max = "100";
            range.value = "0";
            let on_off = document.createElement("input");
            on_off.type = "hidden";
            on_off.className="on_off";
            on_off.value = "0";
            range_value.textContent = "0";
            range_wrapper.appendChild(range);
            range_wrapper.appendChild(range_value);
            range.addEventListener("change", function(event){ event.preventDefault(); changeDeviceRange("Light",this);});

            color_title = document.createElement("p");
            color_title.textContent="Color";
            color = document.createElement("input");
            color.style.background = "none";
            color.style.width="40px";
            color.type = "color";
            color.value = "#ECFF00";
            color.addEventListener("change", function(event){ event.preventDefault(); changeDeviceColor(this);})
            pic.alt="light";
            pic.src = "resources/pics/devices/light-off.png";
            button.appendChild(light_ball);
            col.appendChild(bright_title);
            col.appendChild(range_wrapper);
            col.appendChild(color_title);
            col.appendChild(color);
            col.appendChild(on_off);
            button.addEventListener("click" , function(event){event.preventDefault(); changeDevicePicture("Light", this);})
            break;

        case "Door":
            bright_title = document.createElement("p");
            bright_title.textContent = "Lock"
            icon = document.createElement("i");
            icon.className = "fa fa-lock";
            icon.style.color= "DC143C";
            icon.style.fontSize = "40px";
            icon.style.marginTop = "0";
            icon.addEventListener( "click" , function(event){event.preventDefault(); openDoor(this);});
            pic.alt="door";
            pic.src = "resources/pics/devices/door-closed.png";
            let open_close = document.createElement("input");
            open_close.type = "hidden";
            open_close.className="on_off";
            open_close.value = "0";
            let lock = document.createElement("input");
            lock.type = "hidden";
            lock.className="on_off";
            lock.value = "0";
            col.appendChild(bright_title);
            col.appendChild(icon);
            col.appendChild(open_close);
            col.appendChild(lock);
            button.addEventListener("click" , function(event){event.preventDefault(); changeDevicePicture("Door", this);})
            break;

        case "Fan":
            bright_title = document.createElement("p");
            bright_title.textContent = "Speed"
            range = document.createElement("input");
            range_wrapper.className = "device_range_wrapper";
            range_value.className = "device_range_value";
            range.style.width="130px";
            range.type = "range";
            range.min = "0";
            range.max = "100";
            range.value = "0";
            range_value.textContent = "0";
            let on_off2 = document.createElement("input");
            on_off2.type = "hidden";
            on_off2.className="on_off";
            on_off2.value = "0";
            range_wrapper.appendChild(range);
            range_wrapper.appendChild(range_value);
            range.addEventListener("change", function(event){ event.preventDefault(); changeDeviceRange("Fan",this);});

            pic.alt="fan";
            pic.src = "resources/pics/devices/fan-off.png";
            pic.style.rotate="0deg";
            col.appendChild(bright_title);
            col.appendChild(range_wrapper);
            col.appendChild(on_off2);
            setTimeout(function(){fanRotate(wrapper)}, 5000);
            button.addEventListener("click" , function(event){event.preventDefault(); changeDevicePicture("Fan", this);})
            break;

        case "Thermostat":
            temperature = document.createElement("div");
            temperature.className = "temperature_icon";

            bright_title = document.createElement("p");
            bright_title.textContent = "Temperature Set"
            range = document.createElement("input");
            range_wrapper.className = "device_range_wrapper";
            range.className = "temp_set";
            range.type = "number";
            range.min = "6";
            range.max = "40";
            range.value = "6";
            range.step = "0.1";
            range.addEventListener("change", function(event){ event.preventDefault(); changeDeviceRange("Thermostat",this);});
            color = document.createElement("p");
            color.className = "temperature_sensor";
            color.textContent = "0";
            color.style.width="50px";

            pic.alt="thermostat";
            pic.className="thermostat";
            pic.src = "resources/pics/devices/thermostat-off.png";
            button.appendChild(color);
            button.appendChild(temperature);

            col.appendChild(bright_title);
            col.appendChild(range);
            break;

        case "Conditioner":
            temperature = document.createElement("div");
            temperature.className = "temperature_icon";

            bright_title = document.createElement("p");
            bright_title.textContent = "Speed"
            range = document.createElement("input");
            range_wrapper.className = "device_range_wrapper";
            range_value.className = "device_range_value";
            range.style.width="130px";
            range.type = "range";
            range.min = "0";
            range.max = "100";
            range.value = "0";
            range_value.textContent = "0";
            let on_off3 = document.createElement("input");
            on_off3.type = "hidden";
            on_off3.className="on_off";
            on_off3.value = "0";
            range_wrapper.appendChild(range);
            range_wrapper.appendChild(range_value);
            range.addEventListener("change", function(event){ event.preventDefault(); changeDeviceRange("Fan",this);});

            col.appendChild(bright_title);
            col.appendChild(range_wrapper);

            setTimeout(function(){conditionRotate(wrapper)}, 5000);
            button.addEventListener("click" , function(event){event.preventDefault(); changeDevicePicture("Conditioner", this);})

            fan = document.createElement("img");
            fan.className = "condition_fan";
            fan.alt = "fan";
            fan.src = "resources/pics/devices/fan-off.png";
            fan.style.rotate="0deg";

            bright_title = document.createElement("p");
            bright_title.textContent = "Temperature Set"
            range = document.createElement("input");
            range.className = "temp_set";
            range.type = "number";
            range.min = "6";
            range.max = "40";
            range.value = "6";
            range.step = "0.1";
            range.addEventListener("change", function(event){ event.preventDefault(); changeDeviceRange("Conditioner",this);});
            color = document.createElement("p");
            color.className = "temperature_sensor";
            color.textContent = "0";
            color.style.width="50px";

            pic.alt="thermostat";
            pic.className="thermostat";
            pic.src = "resources/pics/devices/thermostat-hot.png";
            button.appendChild(color);
            button.appendChild(temperature);
            col.appendChild(bright_title);
            col.appendChild(range);
            col.appendChild(on_off3);
            break;
        default:
    }


    button.appendChild(pic);
    if( fan != null )
        button.appendChild(fan);
    wrapper.appendChild(button);
    wrapper.appendChild(col);
    device.appendChild(wrapper);
    return device;

}

function changeDevicePicture( type , elem){
    let pic = elem.getElementsByTagName("img")[0];
    let on_off = elem.parentNode.getElementsByClassName("on_off")[0];
    switch(type){
        case "Light":
            let ball = elem.getElementsByClassName("light_ball")[0];
            if( on_off.value !== "0" ) {
                pic.src = "resources/pics/devices/light-off.png";
                ball.style.display = "none";
            }else {
                pic.src = "resources/pics/devices/light-on.png";
                ball.style.display = "block";
            }

            break;

        case "Door":
            if( on_off.value !== "0" )
                pic.src = "resources/pics/devices/door-closed.png";
            else
                pic.src = "resources/pics/devices/door-open.png";

            break;

        case "Fan":
            if( on_off.value !== "0" )
                pic.src = "resources/pics/devices/fan-off.png";

            else
                pic.src = "resources/pics/devices/fan-on.png";

            break;
        case "Conditioner":
            pic = elem.getElementsByTagName("img")[1];
            if( on_off.value !== "0" )
                pic.src = "resources/pics/devices/fan-off.png";

            else
                pic.src = "resources/pics/devices/fan-on.png";

            break;
        default:
    }

    if( on_off.value === "0")
        on_off.value = "1";
    else
        on_off.value = "0";
}


async function changeDeviceRange(type, elem){

    switch(type){
        case "Light":
            let output = elem.parentNode.getElementsByClassName("device_range_value")[0];
            output.textContent = elem.value;
            let wrapper = elem.parentNode.parentNode.parentNode;
            let pic = wrapper.getElementsByClassName("light_ball")[0];
            pic.style.opacity = ""+(parseInt(elem.value)/100);
            break;
        case "Fan":
            let pointer = elem.parentNode.getElementsByClassName("device_range_value")[0];
            pointer.textContent = elem.value;
            break;
        case "Thermostat":
            for( let a = 0; a<40; a++) {
                updateThermostatTemperature(a + "", "asd");
                await sleep(500);
            }
            break;
        case "Conditioner":
            for( let a = 0; a<40; a++) {
                updateConditionerTemperature(a + "", "asd");
                await sleep(500);
            }
            break;
    }
}
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function fanRotate(root){

    let on_off = root.getElementsByClassName("on_off")[0].value;
    let speed = root.getElementsByClassName("device_range_value")[0].textContent;

    if( on_off !== "0" && speed !== "0"){
        let fan = root.getElementsByTagName("img")[0];
        let rotation = fan.style.rotate.replace("deg","");
        rotation = parseInt(rotation)+speed/4;
        fan.style.transform = "rotate("+ rotation+"deg)";
        fan.style.rotate = rotation+"deg";

    }
    setTimeout(function(){fanRotate(root);}, 250);

}

function conditionRotate(root){

    let on_off = root.getElementsByClassName("on_off")[0].value;
    let speed = root.getElementsByClassName("device_range_value")[0].textContent;

    if( on_off !== "0" && speed !== "0"){
        let fan = root.getElementsByTagName("img")[1];
        let rotation = fan.style.rotate.replace("deg","");
        rotation = parseInt(rotation)+speed/4;
        fan.style.transform = "rotate("+ rotation+"deg)";
        fan.style.rotate = rotation+"deg";

    }
    setTimeout(function(){conditionRotate(root);}, 250);

}

function openDoor(elem){

    let wrapper = elem.parentNode;
    let lock = wrapper.getElementsByClassName("lock");
    if( lock.value === "0"){
        elem.className="fa fa-unlock";
        elem.style.color = "#57b846";
        lock.value = "1";
    }else{
        elem.className="fa fa-lock";
        elem.style.color = "#DC143C";
        lock.value = "0";
    }
}
function changeDeviceColor( elem ){

    let device = elem.parentNode.parentNode;
    let color = device.getElementsByClassName("light_ball")[0];
    color.style.boxShadow = "0 0 60px 40px " + elem.value;
}