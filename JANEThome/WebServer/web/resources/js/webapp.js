

//  PAGE MAIN ELEMENTS
const button_wrap = document.getElementById("button_wrap");    //  container for the buttons
let scroller = document.getElementById("scroller");        //  button scroller
const left_angle = document.getElementById("angle_left");    //  left arrow of the scroller
const right_angle = document.getElementById("angle_right");  //  right arrow of the scroller
const body_page = document.getElementById("body-webapp");  //  container for the page content

let position = 0;         //  scroll value
let actual_loc = null;    //  actual location showed into the page
let throttled = false;    //  optimization of the button scroll
let errorFlag = false;    //  an error has been showed


////////   DYNAMIC PAGE CREATION FUNCTIONS

//// PRIMARY FUNCTIONS[TO BE USED]

//  creates a new location named as the parameter. The location will contain all the needed elements
//  in particular a button to remove it/change its name and the default sublocation
function createLocation( name ){

    let div = document.createElement('div');
    div.className = "location_wrapper location";
    div.id = name;
    div.appendChild(createLocationButtons());  //  generation of location buttons(delete location/change location)
    div.appendChild(createSublocation(name));  //  generation of 'Default' sublocation on the 'name' location
    body_page.appendChild(div);

}

//  creates a new sublocation named as the parameter into a defined location
function createSublocation(location, name="default"){

    let container = document.createElement("div");  //  main container for the sublocation
    let splitter = document.createElement("div");   //  separator for the title
    splitter.className = "divider_sublocation";
    splitter.append(document.createElement("span"));

    container.className = "sublocation_wrapper";
    container.id = location+"_"+name;   //  sublocation ids organized as location_sublocation to easy retrieval
    if( name === "default")     //  default sublocation, it is mandatory into a location and cannot be removed
        container.append(createPopupContainer());
    else     //  sublocation requested by the user/server
        container.append(createPopupContainer(false));  //  with false it adds the remove/change sublocation buttons

    container.append(createSublocationHeader(name)); //  generation of sublocation header
    container.append(splitter);
    container.append(createSublocationContent());    //  generation of sublocation elements into a container
    return container;

}

//// SUPPORT FUNCTIONS [! TO NOT BE USED DIRECTLY !]

//  creates a wrapper containing a rename button and delete button for a location(used by createSublocation)
function createLocationButtons(){

    //  wrapper for the buttons
    let button_location_wrapper = document.createElement("div");
    button_location_wrapper.className = "location_button_wrapper";
    button_location_wrapper.appendChild(createLocationRenameButton());  //  creates the location rename button
    button_location_wrapper.appendChild(createLocationDeleteButton());  //  creates the location delete button

    return button_location_wrapper;

}

//  creates a button to rename a location(used by createLocationButtons)
function createLocationRenameButton() {

    //  button components
    let button_anchor = document.createElement("a");
    let button_paragraph = document.createElement("p");
    let span1 = document.createElement("span");
    let span2 = document.createElement("span");
    let span3 = document.createElement("span");

    button_anchor.href = "#";
    button_anchor.addEventListener('click', function(){renameLocation(this);}); //  [ on click -> renameLocation ]
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

//  creates a button to delete a location(used by createLocationButtons)
function createLocationDeleteButton(){

    //  button components
    let button_anchor = document.createElement("a");
    let button_paragraph = document.createElement("p");
    let span1 = document.createElement("span");
    let span2 = document.createElement("span");
    let span3 = document.createElement("span");

    button_anchor.href = "#";
    button_anchor.addEventListener('click', function(){deleteLocation(this);});  //  [ on click -> deleteLocation ]
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

//  creates the sublocation popup container, each sublocation has one and it is used to show forms for
//  the user interaction (used by createSublocation)
function createPopupContainer(default_container=true){

    let popups_container = document.createElement("div");
    let popups_wrapper = document.createElement("div");
    popups_container.className = "container-popups";
    popups_wrapper.className = "popups-wrapper";

    if( default_container ) {   //  default sublocation has the rename location/add sublocation/add device forms
        popups_wrapper.appendChild(createRenameLocationPopup());  // contains rename location form
        popups_wrapper.append(createAddSubLocationPopup());       // contains add sublocation form
        popups_wrapper.appendChild(createAddDevicePopup());       // contains add device form
    }else{    //  sublocation has the rename sublocation/add device forms
        popups_wrapper.appendChild(createRenameSubLocationPopup());  //  contains rename sublocation form
        popups_wrapper.appendChild(createAddDevicePopup());          //  contains add device form
    }

    popups_container.appendChild(popups_wrapper);
    return popups_container;

}

//  creates the popup to show the form to rename the location (used by createPopupContainer)
function createRenameLocationPopup(){

    //  rename popup components
    let icon = document.createElement("i");              //  cross icon to close the popup
    let icon2 = document.createElement("i");             //  arrow icon
    let icon3 = document.createElement("i");             //  form icon
    let span = document.createElement("span");           //  title container
    let span2 = document.createElement("span");          //  loading button
    let span3 = document.createElement("span");          //  error button
    let label = document.createElement("label");         //  input container
    let input = document.createElement("input");         //  form input
    let button_main = document.createElement("div");     //  form submit main container
    let popup_container = document.createElement("div"); //  components container
    let button = document.createElement("button");       //  form submit definition wrapper
    let pic = document.createElement("img");             //  loading icon
    let form = document.createElement("form");           //  rename form

    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";

    pic.src = "resources/pics/loading.gif";
    pic.alt = "#";

    icon.className = "fa fa-times close_button";
    icon.addEventListener("click",function(){closePopup(this);}); //  [ on click -> closePopup ]
    icon2.className = "fa fa-location-arrow";
    icon3.className = "fa fa-times";

    span.className = "sublocation_form_title";
    span.textContent = "Rename Location";
    span2.className = "loading_placeholder";
    span2.appendChild(pic);
    span3.className = "error_placeholder";
    span3.appendChild(icon3);

    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Location Name";
    input.addEventListener( "keyup" , function(){releaseSubLock(this);}); //  [ on click -> releaseSubLock ]

    button.className = "simple_sublocation_btn";
    //  [ on click -> renameLocationAction/closePopup ]
    button.addEventListener("click",function(event){event.preventDefault(); if(renameLocationAction(this)) closePopup(this.parentNode)});
    button.textContent = "Rename";

    label.appendChild(input);
    label.appendChild(icon2);

    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label);
    form.appendChild(button_main);

    popup_container.className = "rename_location_popup popups";
    popup_container.appendChild(form);

    return popup_container;

}

//  creates the popup to show the form to rename the sublocation (used by createPopupContainer)
function createRenameSubLocationPopup(){

    //  rename popup components
    let popup_container = document.createElement("div");   //  components container
    let button_main = document.createElement("div");       //  form submit main container
    let button = document.createElement("button");         //  form submit definition wrapper
    let icon = document.createElement("i");                //  cross icon to close the popup
    let icon2 = document.createElement("i");               //  arrow icon
    let icon3 = document.createElement("i");               //  form icon
    let span = document.createElement("span");             //  title container
    let span2 = document.createElement("span");            //  loading button
    let span3 = document.createElement("span");            //  error button
    let label = document.createElement("label");           //  input container
    let input = document.createElement("input");           //  form input
    let pic = document.createElement("img");               //  loading icon
    let form = document.createElement( "form" );           //  rename form

    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";

    pic.src = "resources/pics/loading.gif";
    pic.alt = "#";

    span2.appendChild(pic);
    span3.appendChild(icon3);

    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    icon.className = "fa fa-times close_button";
    icon.addEventListener("click",function(){closePopup(this);});  //  [ on click -> closePopup ]
    icon2.className = "fa fa-location-arrow";
    icon3.className = "fa fa-times";

    span.className = "sublocation_form_title";
    span.textContent = "Rename Sublocation";
    span2.className = "loading_placeholder";
    span3.className = "error_placeholder";

    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Location Name";
    input.addEventListener( "keyup" , function(){releaseSubLock(this);});  //  [ on click -> releaseSubLock ]

    button.className = "simple_sublocation_btn";
    //  [ on click -> renameSublocationAction/closePopup ]
    button.addEventListener("click",function(event){ event.preventDefault(); if(renameSublocationAction(this)) closePopup(this.parentNode)});
    button.textContent = "Rename";

    label.appendChild(input);
    label.appendChild(icon2);

    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label);
    form.appendChild(button_main);

    popup_container.className = "rename_sublocation_popup popups";
    popup_container.appendChild(form);

    return popup_container;

}

//  creates the popup to show the form to add a new sublocation (used by createPopupContainer)
function createAddSubLocationPopup(){

    let popup_container = document.createElement("div");   //  components container
    let col1 = document.createElement("div");              //  column for image
    let separator = document.createElement("div");         //  line separator between columns
    let divider = document.createElement("div");           //  line separator between columns
    let form = document.createElement("div");              //  add sublocation form
    let pic = document.createElement( "img" );             //  sublocation pic
    let pic2 = document.createElement("img");              //  loading gif
    let icon = document.createElement("i");                //  cross icon to close the popup
    let icon2 = document.createElement("i");               //  arrow icon
    let icon3 = document.createElement("i");               //  form icon
    let span = document.createElement("span");             //  title container
    let span2 = document.createElement("span");            //  loading button
    let span3 = document.createElement("span");            //  error button
    let label = document.createElement("label");           //  input container
    let input = document.createElement("input");           //  form input
    let button = document.createElement("button");         //  form submit definition wrapper
    let button_main = document.createElement("div");       //  form submit main container

    button_main.className = "add_sublocation_submit";
    button.className = "location-form-button";
    button.textContent = "Add";

    span2.className = "loading_placeholder";
    span3.className = "error_placeholder";

    pic2.src = "resources/pics/loading.gif";
    pic2.alt = "#";

    icon.className = "fa fa-times close_button";
    //  [ on click -> closePopup ]
    icon.addEventListener("click",function(){closePopup(this);});
    icon2.className = "fa fa-location-arrow";
    icon3.className = "fa fa-times";

    span2.appendChild(pic2);
    span3.appendChild(icon3);

    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    pic.src = "resources/pics/sublocation.png";
    pic.alt = "#";

    span.className = "sublocation_form_title";
    span.textContent = "Add Sublocation";

    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Location Name";
    input.addEventListener( "keyup" , function(){releaseLock(this);});  //  [ on click -> releaseLock ]

    button.className = "add_sublocation_btn";
    //  [ on click -> addSublocation/closePopup ]
    button.addEventListener("click",function(){if(addSublocation(this)) closePopup(this.parentNode)});
    button.textContent = "Add";

    label.appendChild(input);
    label.appendChild(icon2);

    col1.className = "info_col";
    col1.appendChild(pic);

    divider.className = "inner";
    separator.className = "outer";
    separator.appendChild(divider);

    form.className = "add_sublocation_form";
    form.appendChild(icon);
    form.appendChild(span);
    form.appendChild(label);
    form.appendChild(button_main);

    popup_container.className = "add_sublocation_popup popups";
    popup_container.appendChild(col1);
    popup_container.appendChild(separator);
    popup_container.appendChild(form);

    return popup_container;
}

//  creates the popup to show the form to add a new device into the current sublocation(used by createPopupContainer)
function createAddDevicePopup(){

    let popup_container = document.createElement("div");            //  components container
    let icon = document.createElement("i");                         //  cross icon to close the popup
    let icon2 = document.createElement("i");                        //  arrow icon
    let icon3 = document.createElement("i");                        //  form icon
    let span = document.createElement("span");                      //  title container
    let span2 = document.createElement("span");                     //  loading button
    let span3 = document.createElement("span");                     //  error button
    let label = document.createElement("label");                    //  container for device name
    let label2 = document.createElement("label");                   //  container for device type
    let input = document.createElement("input");                    //  form input
    let button_main = document.createElement("div");                //  form submit main container
    let button = document.createElement("button");                  //  form submit definition wrapper
    let pic = document.createElement("img");                        //  loading gif
    let form = document.createElement( "form" );                    //  add device form
    let select = document.createElement("select");                  //  device selection
    let options = [ "Light", "Fan", "Door", "Thermostat", "Conditioner"];   //  device types

    button.className = "location-form-button";
    button.textContent = "Add";

    pic.src = "resources/pics/loading.gif";
    pic.alt = "#";

    span.className = "sublocation_form_title";
    span.textContent = "Add Device";
    span2.className = "loading_placeholder";
    span2.appendChild(pic);
    span3.className = "error_placeholder";
    span3.appendChild(icon3);

    button_main.className = "add_sublocation_submit";
    button_main.appendChild(button);
    button_main.appendChild(span2);
    button_main.appendChild(span3);

    icon.className = "fa fa-times close_button";
    icon.addEventListener("click",function(){closePopup(this);});  //  [ on click -> closePopup ]
    icon2.className = "fa fa-location-arrow";
    icon3.className = "fa fa-times";

    input.className = "input";
    input.type = "text";
    input.name = "name";
    input.placeholder = "Device Name";
    input.addEventListener( "keyup" , function(){releaseSubLock(this);}); //  [ on click -> releaseSubLock ]

    button.className = "simple_sublocation_btn";
    //  [ on click -> addDevice ]
    button.addEventListener("click",function(event){ event.preventDefault(); addDevice(this);});
    button.textContent = "Add";

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

    popup_container.className = "add_device_popup popups";
    popup_container.appendChild(form);

    return popup_container;

}

// creates the header of a sublocation containing all the sublocation buttons( used by createSublocation )
function createSublocationHeader(sublocation_name = "default"){

    let wrapper = document.createElement("div");    //  container of the sublocation
    let header = document.createElement("h1");      //  title of the sublocation
    let anchor = document.createElement("a");       //  add sublocation wrapper
    let paragraph = document.createElement("p");    //  add sublocation button
    let span1 = document.createElement("span");     //  background wrapper
    let span2 = document.createElement("span");     //  moving color wrapper
    let span3 = document.createElement("span");     //  button title

    wrapper.className = "sublocation_header_wrapper";
    header.className = "heading_sublocation";
    header.textContent = sublocation_name;

    //  dynamic sublocation actions generation
    if( sublocation_name === "default") {  //  in case of default sublocation we can add a new sublocation

        anchor.className = "add_sublocation_wrapper";
        anchor.href = "#";
        //  on click open the add sublocation form
        anchor.addEventListener("click", function () {
            openSublocation(this);  //  [ on click -> openSublocation ]
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

    }else{ //  in case of normal sublocation we can delete or rename the sublocation

        anchor.className = "rename_sublocation_wrapper";
        anchor.href = "#";
        //  on click open the rename sublocation form
        anchor.addEventListener("click", function () {
            renameSublocation(this); // [ on click -> renameSublocation ]
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
        //  on click open the delete sublocation form
        anchor.addEventListener("click", function () {
            deleteSublocation(anchor); // [ on click -> deleteSublocation ]
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

//  creates to body of a sublocation used to contain the sublocation's devices ( used by createSublocation )
function createSublocationContent(){

    let wrapper = document.createElement("div");             //  content wrapper
    let device_wrapper = document.createElement("div");      //  device wrapper for scroll
    let device_scroller = document.createElement("div");     //  devices container

    let icon1 = document.createElement("i"); //  add device icon
    let icon2 = document.createElement("i"); //  left arrow icon
    let icon3 = document.createElement("i"); //  right arrow icon

    wrapper.className = "sublocation_content_wrapper sublocation";
    device_wrapper.className = "device_wrapper wrapper";
    device_scroller.className = "device_scroller scroller";

    icon1.className = "fa fa-plus-square-o";
    //  on click open the add device form
    icon1.addEventListener("click",function(){addDevicePopup(this);}); // [ on click -> addDevicePopup ]
    icon2.className = "fa fa-angle-left left_direction";
    //  on click move the scroller to left
    icon2.addEventListener("click", function(){lclick(icon2);});  // [ on click -> lclick ]
    //  on click move the scroller to right
    icon3.className = "fa fa-angle-right right_direction";
    icon3.addEventListener("click", function(){rclick(icon3);});  // [ on click -> rclick ]

    device_wrapper.appendChild(device_scroller);
    wrapper.appendChild(icon1);
    wrapper.appendChild(icon2);
    wrapper.appendChild(device_wrapper);
    wrapper.appendChild(icon3);

    return wrapper;

}

//  creates a new button to be inserted into the location scroller ( used by createLocation )
function createLocationButton(name){

    let button = document.createElement('a');      //  location wrapper
    let paragraph = document.createElement('p');   //  location button
    let span1 = document.createElement('span');    //  button background wrapper
    let span2 = document.createElement('span');    //  button moving color wrapper
    let span3 = document.createElement( 'span' );  //  button title wrapper

    button.href="#";
    button.id = "button_"+name;   //  reference to the button button_location for easy retrieval

    paragraph.className = "location_button";
    // clicking the button will change the location showed
    paragraph.addEventListener("click", function(){changePage(this)}); // [ on click -> changePage ]

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

//  locations can only be changed by the back-end(user can make a request to it, but only the backend can accept it)
//  Every function named Reaction can be used directly by the backend to update the user interface or as a reaction to
//  a user request

////  LOCATION DELETE

//  user request to remove a location, the function passes as a parameter the clicked button
function deleteLocation(elem){

    //  verification if the background color has reached the end of the button to accept the request
    //  this is just a control to prevent that a user removes a location for an error. He has to wait that the button
    //  has changed its color before clicking
    if( elem.getElementsByClassName("bg")[0].getBoundingClientRect().width< elem.getBoundingClientRect().width )
        return;

    //  getting the selected location
    let location = elem.parentNode.parentNode;
    serverRemoveLocation(location.id);  //  request to the server to remove the location giving its name

}

//  deletes a location named as the parameter [ deleteLocation REACTION ]
function deleteLocationReaction(location){

    let loc = document.getElementById(location); //  getting the location container
    if( loc == null )
        return;

    loc.parentNode.removeChild(loc);  //  removing the container and all its content
    scroller.removeChild(document.getElementById("button_"+location));  //  removing the location from the location selector

}

////  SUB-LOCATION DELETE

//  user request to remove a sub-location, the function passes as a parameter the clicked button
function deleteSublocation(elem){

    //  verification if the background color has reached the end of the button to accept the request
    //  this is just a control to prevent that a user removes a location for an error. He has to wait that the button
    //  has changed its color before clicking
    if( elem.getElementsByClassName("bg")[0].getBoundingClientRect().width< elem.getBoundingClientRect().width )
        return;

    //  getting the selected sublocation
    let sublocation = elem.parentNode.parentNode;
    let info = sublocation.id.split("_");  //  getting the sublocation name(id format location_sublocation)
    serverRemoveSublocation(info[0], info[1]);  //  request to the server to remove the 'sublocation'(info[1]) from the 'location'(info[0])

}

//  deletes a sublocation from the location [ deleteSublocation REACTION ]
function deleteSublocationReaction(location, sublocation){

    let subloc = document.getElementById(location+"_"+sublocation);  //  getting the sublocation(id= location_sublocation)
    if( subloc != null )
        subloc.parentNode.removeChild(subloc);  //  removing the sublocation from the location with all its content

}

//// LOCATION RENAME

//  user request to rename a location, the function passes as a parameter the clicked button
function renameLocationAction(elem) {

    //  getting the form container
    let form = elem.parentNode.parentNode;
    //  getting from the submit button the loading label
    let button = form.getElementsByTagName("button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  getting the new location name
    let input = form.getElementsByClassName("input")[0].value.toLowerCase();
    let locations = document.getElementsByClassName("location");

    //  verification the location is not already used
    for( let location of locations )
        if(location.id === input ){
            button.style.display = "none";
            error.style.display = "flex";
            return;
        }

    //  setting the button into loading behavior
    button.style.display = "none";
    loading.style.display = "flex";
    //  getting the current location name
    let location = elem.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
    let old_location = location.id;

    //  request to the server to change the name of the location
    renameServerLocation(old_location, input);

}

//  management function to rename a location
function renameLocationAct(old_name, new_name){

    //  parameter verification
    if( new_name.length === 0 )
        return false;

    //  getting the selected location
    let selected_location = document.getElementById(old_name);
    if( selected_location == null )
        return false;

    //  verification of location not already present
    let locations = document.getElementsByClassName("location");
    for( let location of locations )
        if(location.id === new_name )
            return false;

    //  renaming the ids of all the sublocations into the selected location
    let sublocations = selected_location.getElementsByClassName("sublocation_wrapper");
    for( let sublocation of sublocations )
        sublocation.id = new_name + "_"+ sublocation.id.replace(old_name+"_","");

    //  renaming of the selected location
    selected_location.id = new_name;

    //  renaming the location button
    let button = document.getElementById("button_"+ old_name);
    button.id = "button_" + new_name;
    button.getElementsByClassName("text")[0].textContent = new_name;

    return true;

}

//  renames a location [ renameLocationAction REACTION ]
function renameLocationReaction(old_location, new_location ){

    //  getting the rename location popup
    let form = document.getElementById(old_location+"_default").getElementsByClassName("rename_location_popup")[0].getElementsByTagName("form")[0];
    //  getting the submit button layers
    let button = form.getElementsByTagName("button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  if the button is on loading(user request)
    if( button.style.display !== "inline")
        if(renameLocationAct(old_location,new_location)){
            //  in case of success remove the button load
            loading.style.display = "none";
            button.style.display = "inline";
            return true;

        }else{
            //  in case of error put the button error
            loading.style.display = "none";
            error.style.display = "flex";
            return false;
        }
    else //  if the button is not loading directly apply the renaming(server update)
        return renameLocationAct(old_location,new_location);

}

//// SUB-LOCATION RENAME

//  user request to rename a sub-location, the function passes as a parameter the clicked button
function renameSublocationAction(elem) {

    let button = elem.parentNode.getElementsByTagName("button")[0];
    let loading = elem.parentNode.getElementsByClassName("loading_placeholder")[0];
    let error = elem.parentNode.getElementsByClassName( "error_placeholder")[0];



    //  getting the sublocation container
    let sublocation = elem.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
    //  getting the location name
    let location = sublocation.parentNode.id;

    //  getting the current sub-location name
    let old_name = sublocation.id.replace(location + "_", "");
    //  getting the new sub-location name
    let new_name = elem.parentNode.parentNode;

    let sub_locations = document.getElementsByClassName("sublocation_wrapper");
    new_name = new_name.getElementsByClassName("input")[0].value.toLowerCase();

    //  verification the location is not already used
    for( let sub_location of sub_locations )
        if(sub_location.id.substr(sub_location.id.indexOf("_")+1) === new_name ){
            button.style.display = "none";
            error.style.display = "flex";
            return;
        }

    button.style.display = "none";
    loading.style.display = "flex";

    //  request to the server to change the sub-location name
    renameServerSublocation(location, old_name, new_name);

}

//  management function to rename a sub-location
function renameSublocationAct(location_name, old_name, new_name){

    //  getting the sub-location
    let sublocation = document.getElementById(location_name+"_"+old_name);
    //  verification of parameters, presence of current sublocation and not presence of the renamed sublocation
    if( sublocation == null || new_name.length === 0 || document.getElementById(location_name+"_"+new_name) != null)
        return false;

    //  change the sub-location name
    sublocation.id = location_name + "_" + new_name;
    sublocation.getElementsByClassName("heading_sublocation")[0].textContent = new_name;
    return true;

}

//  renames a sublocation into a location [ renameSublocationAction REACTION ]
function renameSublocationReaction(location, old_name, new_name){

    //  getting the rename sublocation popup
    let elem = document.getElementById(location+"_"+old_name).getElementsByClassName("rename_sublocation_popup")[0];
    //  getting the submit button layers
    let button = elem.parentNode.getElementsByTagName("button")[0];
    let loading = elem.parentNode.getElementsByClassName("loading_placeholder")[0];
    let error = elem.parentNode.getElementsByClassName("error_placeholder")[0];

    //  if the button is on loading(user request)
    if( button.style.display !== "inline" )
        if(renameSublocationAct(location, old_name, new_name )){
            //  in case of success remove the button load
            loading.style.display = "none";
            button.style.display = "inline";
            return true;
        }else{
            //  in case of error put the button error
            loading.style.display = "none";
            error.style.display = "inline";
            return false;
        }
    else  //  if the button is not loading directly apply the renaming(server update)
        return renameSublocationAct(location, old_name, new_name );

}

////  SUBLOCATION ADD

//  user request to add a sub-location, the function passes as a parameter the clicked button
function addSublocation(node){

    //  getting the submit button layers
    let form = node.parentNode.parentNode;
    let button = form.getElementsByClassName("add_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  getting the location container
    let wrapper = node.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
    //  getting the sublocation name
    let name = form.getElementsByClassName('input')[0].value.toLowerCase();
    let sub_locations = document.getElementsByClassName("sublocation_wrapper");

    //  verification the location is not already used
    for( let sub_location of sub_locations )
        if(sub_location.id.substr(sub_location.id.indexOf("_")+1) === name ){
            button.style.display = "none";
            error.style.display = "flex";
            return;
        }

    //  setting the button on load
    button.style.display = "none";
    loading.style.display = "flex";
    //  request to the server to add the sublocation into the location
    requestServerSublocation(wrapper.id, name);

}

//  management function to add a sublocation
function addSublocationAct(location, sub_location){

    //  verification of sub-location parameter
    if( sub_location === null || sub_location.length === 0)
        return false;

    //  getting the location
    let selected_location = document.getElementById(location);
    if( selected_location == null )
        return false;

    let id = location+"_"+sub_location;  //  generating the sublocation id

    //  verification of the presence of the sublocation
    let sub_locations = selected_location.getElementsByClassName("sublocation_wrapper");
    for( let sub_loc of sub_locations )
        if( sub_loc.id === id )
            return false;

    //  generation of the new location
    selected_location.appendChild(createSublocation(location,sub_location));
    //  adaptation of the scrollbars
    adaptLocationScrolling();
    return true;

}

//  add a new sublocation into a location [ addSublocation REACTION ]
function addSublocationReaction(location, sublocation){

    //  getting the submit button layers
    let form = document.getElementById(location+"_default").getElementsByClassName("add_sublocation_form")[0];
    let button = form.getElementsByClassName("add_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  if the button is on loading(user request)
    if( button.style.display !== "inline" )
        if( addSublocationAct(location,sublocation)){
            //  in case of success remove the button load
            form.getElementsByClassName('input')[0].value = "";
            loading.style.display = "none";
            button.style.display = "inline";
            return true;
        }else{
            //  in case of error put the button error
            loading.style.display = "none";
            error.style.display = "flex";
            return false;
        }
    else  //  if the button is not loading directly apply the adding(server update)
        return addSublocationAct(location,sublocation);
}

//// DEVICE ADD

//  user request to add a device into a sub-location, the function passes as a parameter the clicked button
function addDevice(element) {

    let form = element.parentNode.parentNode; //  getting the form

    //  getting the submit button layers
    let button = form.getElementsByClassName("simple_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  getting the form's information
    let type = form.getElementsByTagName("select")[0].value;                  //  device type
    let name = form.getElementsByClassName("input")[0].value.toLowerCase();   //  device name
    let sublocation = form.parentNode.parentNode.parentNode.parentNode;

    //  putting loading button
    button.style.display = "none";
    loading.style.display = "flex";

    //  name verification
    if (name.length === 0) {
        //  setting error button
        loading.style.display = "none";
        error.style.display = "flex";
        return;
    }

    //  verification of double device presence, devices names must be unique in all the locations
    let devices = document.getElementsByClassName("device");  //  getting al the devices not considering locations/sublocations
    for (let device of devices)
        if (device.id === "device_" + name) {  //  device already present
            //  setting error button
            loading.style.display = "none";
            error.style.display = "flex";
            return;
        }

    //  getting location/sub-location by splitting the sub-location id(id=location_sublocation)
    let info = sublocation.id.split("_");

    //  request to the server to add the device
    addServerDevice(info[0], info[1], name, type);

}

//  management function to add a device
function addDeviceAct(location, sublocation, name, type){

    //  getting the subsection
    let subsection = document.getElementById(location+"_"+sublocation);
    if( subsection === undefined )
        return false;

    //  adding to the device scroller the new device
    subsection.getElementsByClassName("scroller")[0].appendChild(createDevice(type,name));
    adaptLocationScrolling();  // adapt the scrollbars
    return true;

}

//  add a new device into a sub-location [ addDevice REACTION ]
function addDeviceReaction(location, sublocation, dID, dType){

    //  getting the submit button layers
    let form = document.getElementById(location+"_"+sublocation).getElementsByClassName("add_device_popup")[0];
    let button = form.getElementsByClassName("simple_sublocation_btn")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  if the button is on loading(user request)
    if( button.style.display !== "inline" )
        if( addDeviceAct(location,sublocation,dID,dType)){
            //  in case of success remove the button load
            loading.style.display = "none";
            button.style.display = "inline";
            return true;
        }else{
            //  in case of error put the button error
            loading.style.display = "none";
            error.style.display = "flex";
            return false;
        }
    else  //  if the button is not loading directly apply the adding(server update)
        return addDeviceAct(location,sublocation,dID,dType);

}

//// DEVICE DELETE

//  user request to remove a device into a sublocation, the function passes as a parameter the clicked button
function deleteDevice(node){

    //  getting the device ID
    let dID = node.parentNode.getElementsByClassName("device_expander_name")[0].textContent;

    //  request to the server to remove the device
    serverDeleteDevice(dID);

}

//  removes a new device into a sub-location [ deleteDevice REACTION ]
function deleteDeviceAct(dID){

    //  getting the device(id=device_dID)
    let device = document.getElementById("device_" + dID);
    if( device == null)
        return false;

    //  removing the device from the device scroller
    device.parentNode.removeChild(device);

    adaptLocationScrolling();  //  adapt the scrollbars
    closeExpander();           //  eventually closing the popup
    return true;

}

////  DEVICE RENAME

//  user request to rename a device into a sublocation, the function passes as a parameter the clicked button
function renameDevice(node) {

    //  getting the device wrapper
    let wrapper = node.parentNode.parentNode;
    let old_name = wrapper.getElementsByClassName("device_name")[0].value;  //  device name
    let input = wrapper.getElementsByClassName("device_input")[0];          //  getting the new name
    let new_name = input.value;
    let button = wrapper.getElementsByClassName("location-form-button")[0]; //  getting the submit button layers
    let loading = wrapper.getElementsByClassName("loading_placeholder")[0];
    let error = wrapper.getElementsByClassName("error_placeholder")[0];

    //  verification of double device presence, devices names must be unique in all the locations
    let devices = document.getElementsByClassName("device");  //  getting al the devices not considering locations/sublocations
    for (let device of devices)
        if (device.id === "device_" + new_name) {  //  device already present
            //  setting error button
            button.style.display = "none";
            error.style.display = "flex";
            return;
        }

    //  setting the button to load
    button.style.display = "none";
    loading.style.display = "flex";

    //  request to the server to rename a device
    renameServerDevice(old_name, new_name);

}

//  management function to rename a device
function renameDeviceAct(oldDID, newDID){
    //  getting the device
    let device = document.getElementById("device_"+oldDID);
    if( device == null )
        return false;

    //  verification device not already present
    let devices = document.getElementsByClassName("device");
    let new_ID = "device_"+newDID;
    for( let dev of devices )
        if( dev.id === new_ID )
            return false;

    let parent = device.parentNode;
    parent.removeChild( device );
    //  renaming the device
    device.getElementsByClassName("device_title")[0].textContent = newDID+"["+device.getElementsByClassName("type")[0].value+"]";
    device.id = new_ID;

    parent.appendChild( device );

    let expander = document.getElementsByClassName("device_expander_name")[0];
    if(expander.textContent === oldDID)
        expander.textContent = newDID;
    return true;

}

//  rename a device into a sub-location [ renameDevice REACTION ]
function renameDeviceReaction( old_name, new_name ){

    let form = document.getElementById("container_expand").getElementsByClassName("expand_device")[0];
    let button = form.getElementsByClassName("location-form-button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName("error_placeholder")[0];

    //  if the button is on loading(user request)
    if( button.style.display !== "inline" )
        if( renameDeviceAct(old_name,new_name)){
            //  in case of success remove the button load
            loading.style.display = "none";
            button.style.display = "inline";
            return true;
        }else{
            //  in case of error put the button error
            loading.style.display = "none";
            error.style.display = "flex";
            return false;
        }
    else  //  if the button is not loading directly apply the renaming(server update)
        return renameDeviceAct(old_name,new_name);

}

//  DEVICE CHANGE SUBLOCATION

//  user request to change the device sub-location, the function passes as a parameter the clicked button
function changeDeviceSublocation(node){

    //  getting the wrapper
    let wrapper = node.parentNode.parentNode;
    //  getting the location id
    let location = wrapper.getElementsByClassName("device_location")[0].value;
    //  getting the device ID
    let dID = wrapper.getElementsByClassName("device_name")[0].value;
    //  getting the new sub-location name
    let new_sublocation = node.value;

    //  request to the server to change the device sub-location
    serverChangeDeviceSublocation(dID, location, new_sublocation);

}

//  rename a device into a sub-location [ changeDeviceSublocation REACTION ]
function changeDeviceSublocationAct(dID, location, new_sublocation){

    //  getting the device
    let device = document.getElementById("device_"+dID);
    //  getting the form information
    let value = document.getElementById("container_expand").getElementsByClassName("device_sublocation")[0]; //  new sublocation name
    let select = document.getElementById("container_expand").getElementsByTagName("select")[0];  //  getting the device type

    //  verification of device existance
    if( device == null )
        select.value =value.value;

    //  verification of sublocation existance
    let sublocation = document.getElementById(location+"_"+new_sublocation);
    if( sublocation != null )
        select.value =value.value;

    //  getting the device scroller of the destination sub-location
    let wrapper = sublocation.getElementsByClassName("device_scroller")[0];
    //  change device sub-location
    device.parentNode.removeChild(device);
    wrapper.appendChild(device);

    //  changing the location and sub-location linked to the device
    value.value = new_sublocation;
    select.value = new_sublocation;

    return true;

}

//////// DYNAMIC PAGE ELEMENT SHOW

//  shows the add sublocation popup. The function passes the clicked button
function openSublocation(node){

    let location = node.parentNode.parentNode.parentNode.id;  // getting the current location
    //  getting all the popups from the default sublocation(it contains the add sublocation popup)
    let popup_container = document.getElementById(location+"_default").getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');

    //  hide all the eventually showed popups
    for( let popup of popups )
        popup.style.display="none";

    //  show the add sublocation popup
    popup_container.getElementsByClassName("add_sublocation_popup")[0].style.display="flex";
    popup_container.style.display="flex";

}

// shows the rename location popup. The function passes the clicked button
function renameLocation(node){

    let loc_elem = node.parentNode.parentNode; //  getting the location container
    let location = loc_elem.id;  //  getting the location name

    //  getting all the popups from the default sublocation(it contains the rename location popup)
    let popup_container = document.getElementById(location + "_default").getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');

    //  hide all the eventually showed popups
    for( let popup of popups )
        popup.style.display="none";

    //  show the rename location popup
    popup_container.getElementsByClassName("rename_location_popup")[0].style.display="flex";
    popup_container.style.display="flex";

}

//  shows the rename sublocation popup. The function passes the clicked button
function renameSublocation(node){

    let popup_container = node.parentNode.parentNode; //  getting the sublocation container
    //  getting all the popups from the current sublocation(it contains the rename sublocation popup)
    popup_container = popup_container.getElementsByClassName("container-popups")[0];
    let popups = popup_container.getElementsByClassName('popups');

    //  hide all the eventually showed popups
    for( let popup of popups )
        popup.style.display="none";

    //  show the rename sublocation popup
    popup_container.getElementsByClassName("rename_sublocation_popup")[0].style.display="flex";
    popup_container.style.display="flex";
    popup_container.focus();

}

//  shows the add device popup. The function passes the clicked button
function addDevicePopup(node){

    let loc_elem = node.parentNode.parentNode; //  getting the sublocation container
    //  getting all the popups from the current sublocation(it contains the add device popup)
    let popup_container = loc_elem.getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');

    //  hide all the eventually showed popups
    for( let popup of popups )
        popup.style.display="none";

    //  show the add device popup
    popup_container.getElementsByClassName("add_device_popup")[0].style.display="flex";
    popup_container.style.display="flex";

}

//  shows a sublocation. The function passes the clicked button
function changePage(elem){

    //  we hide the current location
    if( actual_loc != null )
        actual_loc.style.display ="none";
    //  getting the name of the location from the button and from that getting the selected location
    actual_loc = document.getElementById(elem.textContent);
    //  displaying the location
    actual_loc.style.display = "flex";

    //  adapt all the scrollbars of the new location
    adaptLocationScrolling();

}

//////// ELEMENTS ACTION HANDLERS

//  close of a displayed popup
function closePopup(node){
    node.parentNode.parentNode.parentNode.parentNode.style.display = "none";
}

////  MANAGEMENT OF STYLE ELEMENTS

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
    if( isNaN(position) ) position = -40;
    if( position < -(scroller.getBoundingClientRect().width) )
        position = -(scroller.getBoundingClientRect().width);

    scroller.style.left = position+"px";
}

//  closes the device expander
function closeExpander(){
    let limiter = document.getElementById("limiter");
    let expander = document.getElementById("container_expand");
    expander.style.display = "none";
    limiter.style.display = "inline";
    adaptLocationScrolling();
}

//  apply the scrolling adaptation for all the page's lists
function adaptLocationScrolling(){

    let elements = document.getElementsByClassName("sublocation");
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

function validatePort(port) {
    let num = parseInt(port);
    return !isNaN(num) && num>0;
}

function unlockDeviceName(node){

    let container = node.parentNode.parentNode;
    let button = container.getElementsByClassName("location-form-button")[0];
    let loading = container.getElementsByClassName("loading_placeholder")[0];
    let error = container.getElementsByClassName("error_placeholder")[0];

    if( error.style.display !== "none"){
        error.style.display = "none";
        loading.style.display = "none";
        button.style.display = "flex";
    }
}


function openExpander(elem){

    let expander = document.getElementById("container_expand");
    let title = document.getElementsByClassName("device_expander_name")[0];
    let input = document.getElementsByClassName("device_input")[0];
    let dID = document.getElementsByClassName("device_name")[0];
    let location = document.getElementsByClassName("device_location")[0];
    let sublocation = document.getElementsByClassName("device_sublocation")[0];
    let type = document.getElementsByClassName("device_type")[0];
    let select = document.getElementById("container_expand").getElementsByTagName("select")[0];
    let dates = expander.getElementsByClassName("date_picker");

    select.innerHTML = "";

    let position = elem.parentNode.parentNode.parentNode.parentNode.id;
    type.value = elem.parentNode.parentNode.parentNode.parentNode.getElementsByClassName("type")[0].value;
    position = position.split("_");
    location.value = position[0];
    sublocation.value = position[1];


    title.textContent = elem.id.replace("device_","");
    input.value = title.textContent;
    dID.value = title.textContent;
    let sublocations = document.getElementById(position[0]).getElementsByClassName("sublocation_wrapper");
    for( let sublocation of sublocations){
        let option = document.createElement("option");
        option.textContent =sublocation.id.replace(position[0]+"_","");
        select.appendChild(option);
    }
    select.value = position[1];

    let today = new Date();
    for( let date of dates)
        date.valueAsDate = today;

    chartCreation(dID.value, elem.getElementsByClassName("type")[0].value);
    document.getElementById("limiter").style.display = "none";
    expander.style.display = "flex";
}

function refreshStat(node){

    let period = node.parentNode.getElementsByClassName("date_picker");
    let start_time = new Date(period[0].value);
    let end_time = new Date(period[1].value);

    if( start_time> end_time || node.className === "fa fa-search search_no")
        return;

    let dID = document.getElementsByClassName("device_name")[0].value;
    let container =  node.parentNode.parentNode;
    let loader = container.getElementsByClassName("graph_loader")[0];
    let graph = container.getElementsByClassName("graph")[0];
    let cheater = container.getElementsByClassName("cheater")[0];
    graph.style.display = "none";
    cheater.style.display = "none";
    loader.style.display = "block";
    let stat = container.getElementsByClassName("statistic_header")[0].textContent;
    node.className = "fa fa-search search_no";
    serverStatRequest( dID, stat, start_time, end_time);

}

function updateStatistic(device_name, statistic, data){

    if( document.getElementById("container_expand").getElementsByClassName("device_name")[0].value === device_name)
        createChart(toGraphID(statistic), statistic, data);

}

function chartCreation(device_name, device_type){

    let charts = document.getElementsByClassName("graph_loader");
    let graphs = document.getElementsByClassName("graph");
    for( let graph of graphs )
        graph.style.display = "none";
    for( let chart of charts )
        chart.style.display = "block";
    let date = new Date();
    let start = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    let end = new Date(date.getFullYear(), date.getMonth(), date.getDate()+1);
    switch(device_type){
        case "Light":
            serverStatRequest( device_name, "Device Usage", start, end);
            serverStatRequest( device_name, "Brightness", start, end);
            break;
        case "Fan":
            serverStatRequest( device_name, "Device Usage", start, end);
            serverStatRequest( device_name, "Fan Speed", start, end);
            break;
        case "Door":
            serverStatRequest( device_name, "N. door opening", start, end);
            serverStatRequest( device_name, "N. door locking", start, end);
            break;
        case "Thermostat":
            serverStatRequest( device_name, "Device Usage", start, end);
            serverStatRequest( device_name, "Temperature", start, end);
            break;
        case "Conditioner":
            serverStatRequest( device_name, "Device Usage", start, end);
            serverStatRequest( device_name, "Temperature", start, end);
            break;
        default:
    }

}

function fromGraphID(graphID, device_type){
    switch(device_type){
        case "Light":
            if( graphID === 1)
                return "Device Usage";
            else
                return "Brightness";
        case "Fan":
            if( graphID === 1)
                return "Device Usage";
            else
                return "Fan Speed";
        case "Door":
            if( graphID === 1)
                return "N. door opening";
            else
                return "N. door locking";
        case "Thermostat":
            if( graphID === 1)
                return "Device Usage";
            else
                return "Temperature";
        case "Conditioner":
            if( graphID === 1)
                return "Device Usage";
            else
                return "Temperature";
        default:
            return "unknown";
    }
}

function toGraphID(statistic){
    switch(statistic){
        case "Device Usage":
        case "N. door opening":
            return 1;
        default:
            return 2;
    }
}
function createChart(id, name, data){

    document.getElementsByClassName("statistic_header")[id-1].textContent = name;
    let max = 0;
    let min = 0;

    let node = document.getElementById("container_expand").getElementsByClassName("fa fa-search")[id - 1];
    try {
        alert(data.toString());
        for (let info of data) {
            if (info.y > max)
                max = info.y;
            if (info.y < min)
                min = info.y;
        }
    }catch(e){
        alert("error");
        node.className = "fa fa-search search_ok";
    }

    if( max < 2 ) max = 1;
    else
        max = max + Math.floor( max/5 );
    alert(max);
    let minutes = Math.ceil((data[data.length - 1].x - data[0].x) / (1000 * 60));
    let format;
    if (minutes <= 60 * 24)
        format = "hh-mm";
    else
        format = "DD-MMM-hh-mm"
    alert( document.getElementById("chart_" + id).style);
    document.getElementById("chart_" + id).innerHTML = "";

    try {
        let chart = new CanvasJS.Chart("chart_" + id,
            {
                height: 235,
                width: 430,

                axisX:{
                    gridThickness: 0,
                    tickLength: 0,
                    lineThickness: 2,
                    labelFormatter: function(){
                        return " ";
                    }
                },
                axisY:{
                    gridThickness: 0,
                    tickLength: 0,
                    lineThickness: 2,
                    labelFormatter: function(){
                        return " ";
                    }
                },

                data: [
                    {
                        indexLabelFontColor: "darkSlateGray",
                        type: "area",
                        color: "#e0a800",
                        dataPoints: data
                    }],
                options: {
                    maintainAspectRatio: false,
                }
            });

        chart.render();
    }catch(e){
        alert(e);
    }

        let loader = document.getElementsByClassName("graph_loader")[id - 1];
        let graph = document.getElementsByClassName("graph")[id - 1];
        let cheater = document.getElementsByClassName("cheater")[id - 1];
        let search = document.getElementsByClassName("statistic_period")[id - 1].getElementsByTagName("i")[0];



        loader.style.display = "none";
        graph.style.display = "block";
        cheater.style.display = "block";
        search.className = "fa fa-search search_ok";



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
    let port = form.elements['port'].value;

    if( location_name.length !== 0 && address.length !== 0 && validatePort(port)){
        let locations = document.getElementsByClassName("location");
        for( let location of locations )
            if( location.id === location_name ){
                loading.style.display = "none";
                error.style.display = "flex";
                errorFlag = true;
                return;
            }
        requestServerLocation(location_name, address, parseInt(port).toString());

    }else{
        loading.style.display = "none";
        error.style.display = "flex";
        errorFlag = true;
    }

})

function addLocationReaction(location_name){
    let form = document.getElementById("add_location_sub");
    let button = form.getElementsByClassName("location-form-button")[0];
    let loading = form.getElementsByClassName("loading_placeholder")[0];
    let error = form.getElementsByClassName( "error_placeholder" )[0];
    addLocation(location_name);
    loading.style.display = "none";
    error.style.display = "none";
    button.style.display = "inline";
}

function addLocation(location_name){

    createLocation(location_name);
    createLocationButton(location_name);

    if (button_wrap.getBoundingClientRect().width < scroller.getBoundingClientRect().width){

        left_angle.style.display= "inline";
        right_angle.style.display= "inline";

    } else {

        left_angle.style.display= "none";
        right_angle.style.display= "none";

    }
}

function errorAddLocation(){

    let button_wrapper= document.getElementById("add_location_sub");
    let button = button_wrapper.getElementsByClassName("location-form-button")[0];
    let loading = button_wrapper.getElementsByClassName("loading_placeholder")[0];
    let error = button_wrapper.getElementsByClassName("error_placeholder")[0];

    if( button.style.display === "none" ){
        loading.style.display = "none";
        error.style.display = "flex";
        errorFlag = true;
    }
}

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

//  function to undo add location button error state
$("#portInput").on('keyup',function(){

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