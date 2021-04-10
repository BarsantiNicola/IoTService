
const button_wrap = document.getElementById("button_wrap");
const left_angle = document.getElementById("angle_left");
const right_angle = document.getElementById("angle_right");
const body_page = document.getElementById("body-webapp");
let scroller = document.getElementById("scroller");
let position = 0;
let actual_loc = null;
let throttled = false;
let errorFlag = false;


////////   DYNAMIC PAGE CREATION FUNCTIONS

//// PRIMARY FUNCTIONS[TO BE USED]

//  creates a new location
function createLocation(name){

    let div = document.createElement('div');
    div.className = "location_wrapper location";
    div.id = name;
    div.appendChild(createLocationButtons());
    div.appendChild(createSublocation(name));
    body_page.appendChild(div);

}

//  creates a new sublocation into a location
function createSublocation(location, name="Default"){

    let container = document.createElement("div");
    let splitter = document.createElement("div");
    splitter.className = "divider_sublocation";
    splitter.append(document.createElement("span"));

    container.className = "sublocation_wrapper";
    container.id = location+"_"+name;
    if( name === "Default")
        container.append(createPopupContainer());
    else
        container.append(createPopupContainer(false));

    container.append(createSublocationHeader(name));
    container.append(splitter);
    container.append(createSublocationContent());
    return container;

}

//// SUPPORT FUNCTIONS[TO NOT BE USED DIRECTLY]

//  creates the rename/delete location buttons for a location
function createLocationButtons(){

    let button_location_wrapper = document.createElement("div");
    button_location_wrapper.className = "location_button_wrapper";
    button_location_wrapper.appendChild(createLocationRenameButton());
    button_location_wrapper.appendChild(createLocationDeleteButton());

    return button_location_wrapper;

}

//  creates a rename location button
function createLocationRenameButton() {
    let button_anchor = document.createElement("a");
    let button_paragraph = document.createElement("p");
    let span1 = document.createElement("span");
    let span2 = document.createElement("span");
    let span3 = document.createElement("span");
    button_anchor.href = "#";
    button_anchor.addEventListener('click', function(){renameLocation(this);});
    button_paragraph.className = "location_rename_button";
    span1.className = "bg";
    span2.className = "base";
    span3.className = "text";
    span3.textContent = "Rename Location";
    button_paragraph.appendChild(span1);
    button_paragraph.appendChild(span2);
    button_paragraph.appendChild(span3);
    button_anchor.appendChild(button_paragraph);
    return button_anchor;
}

//  creates a delete location button
function createLocationDeleteButton(){
    let button_anchor = document.createElement("a");
    let button_paragraph = document.createElement("p");
    let span1 = document.createElement("span");
    let span2 = document.createElement("span");
    let span3 = document.createElement("span");
    button_anchor.href = "#";
    button_anchor.addEventListener('click', function(){deleteLocation(this);});
    button_paragraph.className = "location_delete_button";
    span1.className = "bg";
    span2.className = "base";
    span3.className = "text";
    span3.textContent = "Delete Location";
    button_paragraph.appendChild(span1);
    button_paragraph.appendChild(span2);
    button_paragraph.appendChild(span3);
    button_anchor.appendChild(button_paragraph);
    return button_anchor;
}

//  creates sublocation popup container(where will be placed the interactions popups)
function createPopupContainer(default_container=true){

    let popups_container = document.createElement("div");
    let popups_wrapper = document.createElement("div");
    popups_container.className = "container-popups";
    popups_wrapper.className = "popups-wrapper";
    if( default_container ) {
        popups_wrapper.appendChild(createRenameLocationPopup());
        popups_wrapper.append(createAddSubLocationPopup());
        popups_wrapper.appendChild(createAddDevicePopup());
    }else{
        popups_wrapper.appendChild(createRenameSubLocationPopup());
        popups_wrapper.appendChild(createAddDevicePopup());
    }
    popups_container.appendChild(popups_wrapper);
    return popups_container;

}

//  creates the popup for the rename of the location
function createRenameLocationPopup(){

    let popup_container = document.createElement("div");
    let icon = document.createElement("i");
    let icon2 = document.createElement("i");
    let span = document.createElement("span");
    let label = document.createElement("label");
    let input = document.createElement("input");
    let button_main = document.createElement("div");
    let button = document.createElement("button");
    let span2 = document.createElement("span");
    let pic2 = document.createElement("img");
    let span3 = document.createElement("span");
    let icon3 = document.createElement("i");
    let form = document.createElement("form");

    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";
    span2.className = "loading_placeholder";
    span3.className = "error_placeholder";
    pic2.src = "resources/pics/loading.gif";
    pic2.alt = "#";
    icon3.className = "fa fa-times";

    span2.appendChild(pic2);
    span3.appendChild(icon3);
    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    popup_container.className = "rename_location_popup popups";
    icon.className = "fa fa-times close_button";

    icon.addEventListener("click",function(){closePopup(this);});
    icon2.className = "fa fa-location-arrow";
    span.className = "sublocation_form_title";
    span.textContent = "Rename Location";
    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Location Name";
    input.addEventListener( "keyup" , function(){releaseSubLock(this);});
    button.className = "simple_sublocation_btn";
    button.addEventListener("click",function(event){event.preventDefault(); renameLocationAction(this);});
    button.textContent = "Rename";

    label.appendChild(input);
    label.appendChild(icon2);

    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label);
    form.appendChild(button_main);
    popup_container.appendChild(form);
    return popup_container;

}

//  creates the popup for the rename of the sublocation
function createRenameSubLocationPopup(){

    let popup_container = document.createElement("div");
    let icon = document.createElement("i");
    let icon2 = document.createElement("i");
    let span = document.createElement("span");
    let label = document.createElement("label");
    let input = document.createElement("input");
    let button_main = document.createElement("div");
    let button = document.createElement("button");
    let span2 = document.createElement("span");
    let pic2 = document.createElement("img");
    let span3 = document.createElement("span");
    let icon3 = document.createElement("i");
    let form = document.createElement( "form" );
    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";
    span2.className = "loading_placeholder";
    span3.className = "error_placeholder";
    pic2.src = "resources/pics/loading.gif";
    pic2.alt = "#";
    icon3.className = "fa fa-times";

    span2.appendChild(pic2);
    span3.appendChild(icon3);
    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    popup_container.className = "rename_sublocation_popup popups";
    icon.className = "fa fa-times close_button";
    icon.addEventListener("click",function(){closePopup(this);});
    icon2.className = "fa fa-location-arrow";
    span.className = "sublocation_form_title";
    span.textContent = "Rename Sublocation";
    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Location Name";
    input.addEventListener( "keyup" , function(){releaseSubLock(this);});
    button.className = "simple_sublocation_btn";
    button.addEventListener("click",function(event){ event.preventDefault(); renameSublocationAction(this);});
    button.textContent = "Rename";

    label.appendChild(input);
    label.appendChild(icon2);

    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label);
    form.appendChild(button_main);
    popup_container.appendChild(form);

    return popup_container;
}

//  creates the popup for adding new sublocations
function createAddSubLocationPopup(){

    let popup_container = document.createElement("div");
    let col1 = document.createElement("div");
    let separator = document.createElement("div");
    let divider = document.createElement("div");
    let form = document.createElement("div");
    let pic = document.createElement( "img" );
    let icon = document.createElement("i");
    let icon2 = document.createElement("i");
    let span = document.createElement("span");
    let label = document.createElement("label");
    let input = document.createElement("input");
    let button_main = document.createElement("div");
    let button = document.createElement("button");
    let span2 = document.createElement("span");
    let pic2 = document.createElement("img");
    let span3 = document.createElement("span");
    let icon3 = document.createElement("i");

    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";
    span2.className = "loading_placeholder";
    span3.className = "error_placeholder";
    pic2.src = "resources/pics/loading.gif";
    pic2.alt = "#";
    icon3.className = "fa fa-times";

    span2.appendChild(pic2);
    span3.appendChild(icon3);
    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    popup_container.className = "add_sublocation_popup popups";
    col1.className = "info_col";
    separator.className = "outer";
    divider.className = "inner";
    form.className = "add_sublocation_form";
    pic.src = "resources/pics/sublocation.png";
    pic.alt = "#";
    icon.className = "fa fa-times close_button";
    icon.addEventListener("click",function(){closePopup(this);});
    icon2.className = "fa fa-location-arrow";
    span.className = "sublocation_form_title";
    span.textContent = "Add Sublocation";
    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Location Name";
    input.addEventListener( "keyup" , function(){releaseLock(this);});
    button.className = "add_sublocation_btn";
    button.addEventListener("click",function(){addSublocation(this);});
    button.textContent = "Add";

    label.appendChild(input);
    label.appendChild(icon2);
    col1.appendChild(pic);
    separator.appendChild(divider);

    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label);
    form.appendChild(button_main);

    popup_container.appendChild(col1);
    popup_container.appendChild(separator);
    popup_container.appendChild(form);
    return popup_container;
}

//  creates the popup to add new devices into a sublocation
function createAddDevicePopup(){

    let popup_container = document.createElement("div");
    let icon = document.createElement("i");
    let icon2 = document.createElement("i");
    let span = document.createElement("span");
    let label = document.createElement("label");
    let input = document.createElement("input");
    let button_main = document.createElement("div");
    let button = document.createElement("button");
    let span2 = document.createElement("span");
    let pic2 = document.createElement("img");
    let span3 = document.createElement("span");
    let icon3 = document.createElement("i");
    let form = document.createElement( "form" );
    let label2 = document.createElement("label");
    let select = document.createElement("select");

    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";
    span2.className = "loading_placeholder";
    span3.className = "error_placeholder";
    pic2.src = "resources/pics/loading.gif";
    pic2.alt = "#";
    icon3.className = "fa fa-times";

    span2.appendChild(pic2);
    span3.appendChild(icon3);
    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    popup_container.className = "add_device_popup popups";
    icon.className = "fa fa-times close_button";
    icon.addEventListener("click",function(){closePopup(this);});
    icon2.className = "fa fa-location-arrow";
    span.className = "sublocation_form_title";
    span.textContent = "Add Device";
    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Device Name";
    input.addEventListener( "keyup" , function(){releaseSubLock(this);});
    button.className = "simple_sublocation_btn";
    button.addEventListener("click",function(event){ event.preventDefault(); addDevice(this);});
    button.textContent = "Add";

    let options = [ "Light", "Fan", "Door", "Thermostat", "Conditioner"];
    for( let option of options){
        let app = document.createElement("option");
        app.textContent = option;
        select.appendChild(app);
    }

    label.appendChild(input);
    label.appendChild(icon2);
    label2.appendChild(select);

    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label2);
    form.appendChild(label);
    form.appendChild(button_main);
    popup_container.appendChild(form);

    return popup_container;
}

// creates the header of a sublocation
function createSublocationHeader(sublocation_name = "Default"){

    let wrapper = document.createElement("div");
    let header = document.createElement("h1");
    let anchor = document.createElement("a");
    let paragraph = document.createElement("p");
    let span1 = document.createElement("span");
    let span2 = document.createElement("span");
    let span3 = document.createElement("span");

    wrapper.className = "sublocation_header_wrapper";
    header.className = "heading_sublocation";
    header.textContent = sublocation_name;
    if( sublocation_name === "Default") {

        anchor.className = "add_sublocation_wrapper";
        anchor.href = "#";
        anchor.addEventListener("click", function () {
            openSublocation(this);
        });
        paragraph.className = "add_sublocation_button";
        span1.className = "bg";
        span2.className = "base";
        span3.className = "text";
        span3.textContent = "Add Sublocation";
        paragraph.appendChild(span1);
        paragraph.appendChild(span2);
        paragraph.appendChild(span3);
        anchor.appendChild(paragraph);
        wrapper.appendChild(header);
        wrapper.appendChild(anchor);

    }else{

        anchor.className = "rename_sublocation_wrapper";
        anchor.href = "#";
        anchor.addEventListener("click", function () {
            renameSublocation(this);
        });
        paragraph.className = "rename_sublocation_button";
        span1.className = "bg";
        span2.className = "base";
        span3.className = "text";
        span3.textContent = "Rename Sublocation";
        paragraph.appendChild(span1);
        paragraph.appendChild(span2);
        paragraph.appendChild(span3);
        anchor.appendChild(paragraph);

        wrapper.appendChild(header);
        wrapper.appendChild(anchor);

        anchor = document.createElement("a");
        paragraph = document.createElement("p");
        span1 = document.createElement("span");
        span2 = document.createElement("span");
        span3 = document.createElement("span");

        anchor.className = "delete_sublocation_wrapper";
        anchor.href = "#";
        anchor.addEventListener("click", function () {
            deleteSublocation(anchor);
        });
        paragraph.className = "delete_sublocation_button";
        span1.className = "bg";
        span2.className = "base";
        span3.className = "text";
        span3.textContent = "Delete Sublocation";
        paragraph.appendChild(span1);
        paragraph.appendChild(span2);
        paragraph.appendChild(span3);
        anchor.appendChild(paragraph);
        wrapper.appendChild(anchor);
    }

    return wrapper;
}

//  creates to body of a sublocation
function createSublocationContent(){
    let wrapper = document.createElement("div");
    let device_wrapper = document.createElement("div");
    let device_scroller = document.createElement("div");

    let icon1 = document.createElement("i");
    let icon2 = document.createElement("i");
    let icon3 = document.createElement("i");

    wrapper.className = "sublocation_content_wrapper location";
    device_wrapper.className = "device_wrapper wrapper";
    device_scroller.className = "device_scroller scroller";

    icon1.className = "fa fa-plus-square-o";
    icon1.addEventListener("click",function(){addDevicePopup(this);});
    icon2.className = "fa fa-angle-left left_direction";
    icon2.addEventListener("click", function(){lclick(this);});
    icon3.className = "fa fa-angle-right right_direction";
    icon3.addEventListener("click", function(){rclick(this);});

    device_wrapper.appendChild(device_scroller);
    wrapper.appendChild(icon1);
    wrapper.appendChild(icon2);
    wrapper.appendChild(device_wrapper);
    wrapper.appendChild(icon3);

    return wrapper;

}

//  creates a new button for reaching a location
function createLocationButton(name){

    let button = document.createElement('a');
    let paragraph = document.createElement('p');
    let span1 = document.createElement('span');
    let span2 = document.createElement('span');
    let span3 = document.createElement( 'span' );

    button.href="#";
    button.id = "button_"+name;
    paragraph.className = "location_button";
    paragraph.addEventListener("click", changePage, false);
    span1.className = "bg";
    span2.className = "base";
    span3.className = "text";
    span3.textContent = name;

    paragraph.appendChild(span1);
    paragraph.appendChild(span2);
    paragraph.appendChild(span3);
    button.appendChild(paragraph);
    scroller.appendChild( button );

}

////////  DYNAMIC PAGE ELEMENTS REMOVAL

//  removes a location
function deleteLocation(elem){

    if( elem.getElementsByClassName("bg")[0].getBoundingClientRect().width< elem.getBoundingClientRect().width )
        return;
    let location = elem.parentNode.parentNode;
    let body = location.parentNode;
    let location_name = location.id;
    document.getElementById("scroller").removeChild(document.getElementById("button_"+location_name));
    body.removeChild(location);
}

//  removes a sublocation
function deleteSublocation(elem){
    let sub_location = elem.parentNode.parentNode;
    let container =  sub_location.parentNode;
    container.removeChild(sub_location);
}

//////// DYNAMIC PAGE ELEMENT SHOW

//  shows the add sublocation popup
function openSublocation(node){
    let location = node.parentNode.parentNode.parentNode.id;
    let popup_container = document.getElementById(location+"_Default").getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("add_sublocation_popup")[0].style.display="flex";
    popup_container.style.display="flex";
}

// shows the rename location popup
function renameLocation(node){
    let loc_elem = node.parentNode.parentNode;
    let location = loc_elem.id;
    let popup_container = document.getElementById(location + "_Default").getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("rename_location_popup")[0].style.display="flex";
    popup_container.style.display="flex";
}

//  shows the rename sublocation popup
function renameSublocation(node){
    let popup_container = node.parentNode.parentNode;
    popup_container = popup_container.getElementsByClassName("container-popups")[0];
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("rename_sublocation_popup")[0].style.display="flex";
    popup_container.style.display="flex";
    popup_container.focus();
}

//  shows the add device popup
function addDevicePopup(node){
    let loc_elem = node.parentNode.parentNode;
    let popup_container = loc_elem.getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("add_device_popup")[0].style.display="flex";
    popup_container.style.display="flex";
}

//  shows a sublocation
function changePage(){
    if( actual_loc != null )
        actual_loc.style.display ="none";
    actual_loc = document.getElementById(this.textContent);
    actual_loc.style.display = "flex";
    adaptLocationScrolling();
}

//// ELEMENTS ACTION HANDLERS

//  action on rename location popup submit button
function renameLocationAction(elem){

    let form = elem.parentNode.parentNode;
    let button = form.getElementsByTagName("button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    button.style.display = "none";
    loading.style.display = "flex";
    let input = form.getElementsByClassName("input")[0].value.toLowerCase();

    if( input.length === 0 ){
        loading.style.display = "none";
        error.style.display = "flex";
        return;
    }

    let locations = document.getElementsByClassName("location");
    for( let location of locations )
        if(location.id === input ){
            loading.style.display = "none";
            error.style.display = "flex";
            return;
        }

    let location = elem.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
    let old_location = location.id;

    if( renameServerLocation(old_location, input)){
        let sublocations = document.getElementsByClassName("sublocation_wrapper");
        for( let sublocation of sublocations )
            sublocation.id = input + "_" + sublocation.id.replace(old_location+"_","");

        location.id = input;
        let loc_button = document.getElementById("button_"+ old_location);
        loc_button.id="button_"+input;
        let button_label = loc_button.getElementsByClassName("text")[0];
        button_label.textContent = input;

        loading.style.display = "none";
        button.style.display = "inline";
    }else{
        loading.style.display = "none";
        error.style.display = "flex";
    }

}

//  action on rename sublocation popup submit button
function renameSublocationAction(elem){

    let button = elem.parentNode.getElementsByTagName("button")[0];
    let loading = elem.parentNode.getElementsByClassName("loading_placeholder")[0];
    let error = elem.parentNode.getElementsByClassName("error_placeholder")[0];

    button.style.display = "none";
    loading.style.display = "flex";

    let sublocation = elem.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
    let subloc_name = elem.parentNode.parentNode;
    subloc_name = subloc_name.getElementsByClassName("input")[0].value.toLowerCase();

    if( subloc_name.length === 0 ){
        loading.style.display = "none";
        error.style.display = "flex";
        return;
    }

    let location = sublocation.parentNode;
    let sublocations = location.getElementsByClassName("sublocation_wrapper");
    let new_sublocation_name = location.id + "_" + subloc_name;

    for( let subloc of sublocations)
        if( subloc.id === new_sublocation_name ){
            loading.style.display = "none";
            error.style.display = "flex";
            return;
        }

    if( renameServerSublocation( sublocation.id.replace(location.id+"_",""), subloc_name)){
        let header = sublocation.getElementsByClassName("heading_sublocation")[0];
        sublocation.id = new_sublocation_name;
        header.textContent = subloc_name;
        loading.style.display = "none";
        button.style.display = "flex";
    }else{
        loading.style.display = "none";
        error.style.display = "flex";
    }

}

//  reaction on add sublocation popup submit button
function addSublocation(node){
    let form = node.parentNode.parentNode;
    let button = form.getElementsByClassName("add_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    button.style.display = "none";
    loading.style.display = "flex";

    let wrapper = node.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
    let location = wrapper.id;
    let name = form.getElementsByClassName('input')[0].value.toLowerCase();
    if( name.length === 0 ){
        loading.style.display = "none";
        error.style.display = "flex";
        return;
    }

    let new_name = location+"_"+name;
    let sublocations = wrapper.getElementsByClassName("sublocation_wrapper");
    for( let sublocation of sublocations )
        if( sublocation.id === new_name ){
            loading.style.display = "none";
            error.style.display = "flex";
            return;
        }
    if( requestServerSublocation(wrapper.id, name)){
        form.getElementsByClassName('input')[0].value = "";
        wrapper.appendChild(createSublocation(location,name));
        adaptLocationScrolling();
        loading.style.display = "none";
        button.style.display = "inline";
    }else{
        loading.style.display = "none";
        error.style.display = "flex";
    }

}

//  close of a displayed popup
function closePopup(node){
    node.parentNode.parentNode.parentNode.parentNode.style.display = "none";
}

//  MANAGEMENT OF STYLE ELEMENTS

//  release lock on add sublocation button
function releaseLock(node){
    let form = node.parentNode.parentNode;
    let button = form.getElementsByClassName("add_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    if( button.style.display === "none" ){
        loading.style.display = "none";
        error.style.display = "none";
        button.style.display = "inline";
    }
}

//  release lock on add popups submit button
function releaseSubLock(node){
    let form = node.parentNode.parentNode;
    let button = form.getElementsByClassName("simple_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    if( button.style.display === "none" ){
        loading.style.display = "none";
        error.style.display = "none";
        button.style.display = "inline";
    }
}

//  used by all lists to scrolling on the left
function lclick(elem){
    let scroller = elem.parentNode.getElementsByClassName("scroller")[0];
    let position = parseInt(scroller.style.left, 10) +40;
    if( position > 0 ) position = 0;
    scroller.style.left = position+"px";
}

//  used by all lists to scrolling on the right
function rclick(elem){
    let scroller = elem.parentNode.getElementsByClassName("scroller")[0];
    let position = parseInt(scroller.style.left, 10) -40;
    if( position < -(scroller.getBoundingClientRect().width-420) )
        position = -(scroller.getBoundingClientRect().width-420);

    scroller.style.left = position+"px";
}

//  apply the scrolling adaptation for all the page's lists
function adaptLocationScrolling(){

    let elements = document.getElementsByClassName("location");
    for( let elem of elements )
        adaptScroll(elem);
}

//  shows elements for scrolling if needed for all the sublocation of a location
function adaptScroll(location) {

    let wrapper = location.getElementsByClassName("wrapper")[0];

    let scroller = wrapper.getElementsByClassName("scroller")[0];
    let left_angle = location.getElementsByClassName("left_direction")[0];
    let right_angle = location.getElementsByClassName("right_direction")[0];

    if (scroller.getBoundingClientRect().width < wrapper.getBoundingClientRect().width) {

        scroller.style.left = "0";
        left_angle.style.display = "none";
        right_angle.style.display = "none";

    } else {

        left_angle.style.display = "inline";
        right_angle.style.display = "inline";

    }
}

//  verification of IP address
function validateAddress(ipaddress) {
    return /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress);
}

function addDevice(element){

    let form = element.parentNode.parentNode;
    let button = form.getElementsByClassName("simple_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];
    let type = form.getElementsByTagName("select")[0].value;
    let name = form.getElementsByClassName("input")[0].value.toLowerCase();
    let sublocation = form.parentNode.parentNode.parentNode.parentNode;
    let location = sublocation.parentNode.parentNode;

    button.style.display = "none";
    loading.style.display = "flex";

    if( name.length === 0 ){
        loading.style.display = "none";
        error.style.display = "flex";
        return;
    }

    let devices = document.getElementsByClassName("device");
    for( let device of devices )
        if( device.id === name ){
            loading.style.display = "none";
            error.style.display = "flex";
            return;
        }

    let scroller= form.parentNode.parentNode.parentNode.parentNode.getElementsByClassName("scroller")[0];
    if( addServerDevice(location.id, sublocation.id.replace(location.id+"_",""),name,type)){
        scroller.appendChild(createDevice(type,name));
        loading.style.display = "none";
        button.style.display = "inline";
        adaptLocationScrolling();
    }else{
        loading.style.display = "none";
        error.style.display = "flex";
    }

}

//// STATIC ELEMENTS ACTION HANDLERS

//  adaptation of scrolling on page loading
$(window).on('load', function(){
    adaptLocationScrolling();
})

//  manages the resize of the page scrollers
$(window).resize(function() {
    //  optimization of window resizing
    //  prevent too calls on adaptLocationScrolling
    if (!throttled) {
        throttled = true;
        setTimeout(function() {
            adaptLocationScrolling();
            throttled = false;
        }, 500);
    }

});

//  opens the popup for new locations creation
$("#add_location").on('click', function(){

    if( actual_loc != null )
        actual_loc.style.display = "none";
    actual_loc = document.getElementById("add_location_page");
    actual_loc.style.display="flex";

})

//  creates a new location
$("#add_location_sub").on('click', function(e){
    e.preventDefault();

    let button = this.getElementsByClassName("location-form-button")[0];
    let loading = this.getElementsByClassName("loading_placeholder")[0];
    let error = this.getElementsByClassName("error_placeholder")[0];
    let form = document.getElementById("location_form");

    button.style.display = "none";
    loading.style.display = "flex";

    let location_name = form.elements['location'].value.toLowerCase();
    let address = form.elements['address'].value;

    if( location_name.length !== 0 && address.length !== 0 && validateAddress(address)){
        let locations = document.getElementsByClassName("location");
        for( let location of locations )
            if( location.id === location_name ){
                loading.style.display = "none";
                error.style.display = "flex";
                errorFlag = true;
                return;
            }
        //  TODO SERVER VERIFICATION
        if( requestServerLocation(location_name, address)){
            createLocation(location_name);
            createLocationButton(location_name);
            form.elements['location'].value="";
            form.elements['address'].value="";
            loading.style.display = "none";
            button.style.display = "flex";

            if (button_wrap.getBoundingClientRect().width < scroller.getBoundingClientRect().width){

                left_angle.style.display= "inline";
                right_angle.style.display= "inline";

            } else {

                left_angle.style.display= "none";
                right_angle.style.display= "none";

            }

        }else{
            loading.style.display = "none";
            error.style.display = "flex";
            errorFlag = true;
        }
    }else{
        loading.style.display = "none";
        error.style.display = "flex";
        errorFlag = true;
    }

})

//  function to undo add location button error state
$("#locInput").on('keyup',function(){

    let form = this.parentNode.parentNode;
    let button = form.getElementsByClassName("location-form-button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    if( errorFlag ) {
        error.style.display = "none";
        loading.style.display = "none";
        button.style.display = "flex";
        errorFlag = false;
    }
})

//  function to undo add location button error state
$("#addrInput").on('keyup',function(){

    let form = this.parentNode.parentNode;
    let button = form.getElementsByClassName("location-form-button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    if( errorFlag ) {
        error.style.display = "none";
        loading.style.display = "none";
        button.style.display = "flex";
        errorFlag = false;
    }
})