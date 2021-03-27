const button_wrap = document.getElementById("button_wrap");
const left_angle = document.getElementById("angle_left");
const right_angle = document.getElementById("angle_right");
const body_page = document.getElementById("body-webapp");
let scroller = document.getElementById("scroller");
let position = 0;
let actual_loc = null;
let throttled = false;
let location_code = " <div class=\"delete_button_wrapper\">\n" +
    "                            <a href=\"#\" onclick='renameLocation(this)'>\n" +
    "                                <p class=\"location_rename_button\" id=\"rename_location_button\" >\n" +
    "                                    <span class=\"bg\" id=\"delete_back\"></span>\n" +
    "                                    <span class=\"base\"></span>\n" +
    "                                    <span class=\"text\">Rename Location</span>\n" +
    "                                </p>\n" +
    "                            </a>\n" +
    "                            <a href=\"#\">\n" +
    "                                <p class=\"location_button\" id=\"delete_location_button\">\n" +
    "                                    <span class=\"bg\" id=\"delete_back\"></span>\n" +
    "                                    <span class=\"base\"></span>\n" +
    "                                    <span class=\"text\">Delete Location</span>\n" +
    "                                </p>\n" +
    "                            </a>\n" +
    "                   </div>\n" +
    "                   <div class=\"sublocation_wrapper\" id=\"sublocation_wrapper_default\">\n" +
    "                      <div class=\"container-popups\">\n" +
    "                               <div class=\"popups-wrapper\">" +
    "                                   <div class=\"rename_location_popup popups\">" +
    "                                       <div class=\"popup_input_wrapper\">" +
    "                                           <i class=\"fa fa-times close_button\" onclick='closePopup(this)'></i>"+
    "                                           <h1>Rename Location</h1>    " +
    "                                           <label>" +
    "                                               <input class=\"input\" type=\"text\" name=\"loc_rename\" placeholder=\"Location Name\">"+
    "                                               <i class=\"fa fa-location-arrow\"></i>"+
    "                                           </label>"+
    "                                           <button class=\"login-form-btn\" onclick='renameLocation(this)'>Rename Location</button>"+
    "                                        </div>"+
    "                                   </div>\n" +
    "                                   <div class=\"add_sublocation_popup popups\">\n" +
    "                                           <img alt='img' src='#'>"+
    "                                           <div class=\"outer\">"+
    "                                               <div class=\"inner\"></div>"+
    "                                           </div>"+
    "                                           <div class=\"popup_input_wrapper\">" +
    "                                               <h1>Rename Sublocation</h1>    " +
    "                                               <label>" +
    "                                                   <input class=\"input\" type=\"text\" name=\"loc_rename\" placeholder=\"Location Name\">"+
    "                                                   <i class=\"fa fa-location-arrow\"></i>"+
    "                                               </label>"+
    "                                               <button class=\"login-form-btn\">Rename Sublocation</button>"+
    "                                           </div>"+
    "                                   </div>" +
    "                               </div>" +
    "                       </div>" +
    "                       <div class=\"sublocation_header_wrapper\">"+
    "                            <h1 class=\"heading_sublocation\">Default</h1>\n" +
    "                            <a class=\"add_sublocation_wrapper\" href=\"#\" onclick=\"addSublocation('prova',this)\">\n" +
    "                            <p class=\"add_sublocation_button\">\n" +
    "                               <span class=\"bg\" id=\"delete_back\"></span>\n" +
    "                               <span class=\"base\"></span>\n" +
    "                               <span class=\"text\">Add sublocation</span>\n" +
    "                            </p>\n" +
    "                            </a>\n" +
    "                        </div>"+
    "                        <div class=\"divider_sublocation\"> <span></span></div>\n            " +

    "                        <div class=\"sublocation_content_wrapper location\">\n" +
    "                             <i class=\"fa fa-plus-square-o\" aria-hidden=\"true\" onclick='addDevice(this)'></i>\n" +
    "                             <i class=\"fa fa-angle-left left_direction\" aria-hidden=\"true\" onclick=\"lclick(this)\"></i>\n" +
    "                             <div class=\"device_wrapper wrapper\">\n" +
    "                                 <div class=\"device_scroller scroller\"></div>\n" +
    "                             </div>\n" +
    "                             <i class=\"fa fa-angle-right right_direction\" aria-hidden=\"true\" onclick=\"rclick(this)\"></i>\n" +
    "                         </div>\n" +
    "                     </div>";

let sublocation_code =
    "                      <div class=\"container-popups\">\n" +
    "                               <div class=\"popups-wrapper\">" +
    "                                   <div class=\"rename_sublocation_popup\">" +
    "                                       <div class=\"popup_input_wrapper\">" +
    "                                           <i class=\"fa fa-times close_button\" onclick='closePopup(this)'></i>"+
    "                                           <h1>Rename Sublocation</h1>    " +
    "                                           <label>" +
    "                                               <input class=\"input\" type=\"text\" name=\"subloc_rename\" placeholder=\"Sublocation Name\">"+
    "                                               <i class=\"fa fa-location-arrow\"></i>"+
    "                                           </label>"+
    "                                           <button class=\"login-form-btn\" onclick='rename_subloc(this)'>Rename Sublocation</button>"+
    "                                        </div>"+
    "                                   </div>\n" +
    "                                   <div class=\"add_device_popup\"></div>\n" +
    "                                   <div class=\"manage_device_popup\"></div>\n" +
    "                               </div>" +
    "                       </div>" +
    "                       <div class=\"sublocation_header_wrapper\">"+
    "                            <h1 class=\"heading_sublocation\">";

let sublocation_code_2 = "</h1>\n" +
    "                            <a class=\"rename_sublocation_wrapper\" href=\"#\" onclick=\"renameSublocation(this)\">\n" +
    "                               <p class=\"rename_sublocation_button\">\n" +
    "                                   <span class=\"bg\" id=\"delete_back\"></span>\n" +
    "                                   <span class=\"base\"></span>\n" +
    "                                   <span class=\"text\">Rename sublocation</span>\n" +
    "                               </p>\n" +
    "                            </a>\n" +
    "                            <a class=\"delete_sublocation_wrapper\" href=\"#\" onclick=\"deleteSublocation()\">\n" +
    "                               <p class=\"delete_sublocation_button\">\n" +
    "                                   <span class=\"bg\" id=\"delete_back\"></span>\n" +
    "                                   <span class=\"base\"></span>\n" +
    "                                   <span class=\"text\">Delete sublocation</span>\n" +
    "                               </p>\n" +
    "                            </a>\n" +
    "                        </div>"+
    "                        <div class=\"divider_sublocation\"> <span></span></div>\n" +
    "                        <div class=\"sublocation_content_wrapper location\">\n" +
    "                             <i class=\"fa fa-plus-square-o\" aria-hidden=\"true\" onclick='addDevice(this)'></i>\n" +
    "                             <i class=\"fa fa-angle-left left_direction\" aria-hidden=\"true\" onclick=\"lclick(this)\"></i>\n" +
    "                             <div class=\"device_wrapper wrapper\">\n" +
    "                                 <div class=\"device_scroller scroller\"></div>\n" +
    "                             </div>\n" +
    "                             <i class=\"fa fa-angle-right right_direction\" aria-hidden=\"true\" onclick=\"rclick(this)\"></i>\n" +
    "                         </div>\n";

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

$("#add_location").on('click', function(){

    if( actual_loc != null )
        actual_loc.style.display = "none";
    actual_loc = document.getElementById("add_location_page");
    actual_loc.style.display="flex";

})

$("#add_location_sub").on('click', function(e){
    e.preventDefault();
    let form = document.getElementById("location_form");
    createLocationPage(form.elements['name'].value);
    createButton(form.elements['name'].value);
    form.elements['name'].value="";
    form.elements['ip'].value="";
    if (button_wrap.getBoundingClientRect().width < scroller.getBoundingClientRect().width){

        left_angle.style.display= "inline";
        right_angle.style.display= "inline";

    } else {

        left_angle.style.display= "none";
        right_angle.style.display= "none";

    }

})


function createButton(name){

    let button = document.createElement('a');
    let paragraph = document.createElement('p');
    let span1 = document.createElement('span');
    let span2 = document.createElement('span');
    let span3 = document.createElement( 'span' );

    button.href="#";
    button.id = name;
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

function adaptLocationScrolling(){

    let elements = document.getElementsByClassName("location");
    for( let elem of elements )
        adaptScroll(elem);
}

function adaptScroll(location){

    let wrapper = location.getElementsByClassName("wrapper")[0];

    let scroller = wrapper.getElementsByClassName("scroller")[0];
    let left_angle = location.getElementsByClassName("left_direction")[0];
    let right_angle = location.getElementsByClassName("right_direction")[0];

    if( scroller.getBoundingClientRect().width < wrapper.getBoundingClientRect().width){

        scroller.style.left="0";
        left_angle.style.display= "none";
        right_angle.style.display= "none";

    } else {

        left_angle.style.display= "inline";
        right_angle.style.display= "inline";

    }

}

function createLocationPage(name){

    let div = document.createElement('div');
    div.className = "location_wrapper";
    div.id = "location_page_"+name;
    div.innerHTML = location_code;
    body_page.appendChild(div);

}

function closePopup(node){
    node.parentNode.parentNode.parentNode.parentNode.style.display = "none";
}

function renameLocation(node){
    let popup_container = document.getElementById("sublocation_wrapper_default").getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("rename_location_popup")[0].style.display="flex";
    popup_container.style.display="flex";
}

function addSublocation(name, node){
    let wrapper = node.parentNode.parentNode.parentNode;
    let div = document.createElement('div');
    div.className = "sublocation_wrapper";
    div.id = "sublocation_wrapper_"+name;
    div.innerHTML = sublocation_code + name + sublocation_code_2;
    wrapper.appendChild(div);
    adaptLocationScrolling();
}

function openAddSublocation(){
    let popup_container = document.getElementById("sublocation_wrapper_default").getElementsByClassName("container-popups")[0]
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("add_sublocation_popup")[0].style.display="flex";
    popup_container.style.display="flex";
}

function renameSublocation(node, event){
    let popup_container = node.parentNode.parentNode;
    popup_container = popup_container.getElementsByClassName("container-popups")[0];
    let popups = popup_container.getElementsByClassName('popups');
    for( let popup of popups )
        popup.style.display="none";
    popup_container.getElementsByClassName("rename_sublocation_popup")[0].style.display="flex";
    popup_container.style.display="flex";
    popup_container.focus();
}


function addDevice(element){

    let scroller= element.parentNode.getElementsByClassName("scroller")[0];
    let device = document.createElement('div');
    device.className="device";
    scroller.appendChild(device);
    adaptLocationScrolling();

}


function changePage(){
    if( actual_loc != null )
        actual_loc.style.display ="none";
    actual_loc = document.getElementById("location_page_"+this.textContent);
    actual_loc.style.display = "flex";
    adaptLocationScrolling();
}

$(window).on('load', function(){
    adaptLocationScrolling();
})

$("#angle_left").on('click', function() {
    position -= 20;
    if( position < 0 ) position = 0;
    scroller.style.left = (-position)+"px";

});

$("#angle_right").on('click', function() {
    position += 20;
    if( position > (scroller.getBoundingClientRect().width-260) )
        position = scroller.getBoundingClientRect().width-260;
    scroller.style.left = (-position)+"px";
});

$("#delete_location_button").on("click", function() {
    let background = document.getElementById("delete_back");
    if( document.getElementById("delete_back").getBoundingClientRect().width >= this.getBoundingClientRect().width){


    }
});

function lclick(elem){
    let scroller = elem.parentNode.getElementsByClassName("scroller")[0];
    let position = parseInt(scroller.style.left, 10) +20;
    if( position > 0 ) position = 0;
    scroller.style.left = position+"px";
}

function rclick(elem){
    let scroller = elem.parentNode.getElementsByClassName("scroller")[0];
    let position = parseInt(scroller.style.left, 10) -20;
    if( position < -(scroller.getBoundingClientRect().width-420) )
        position = -(scroller.getBoundingClientRect().width-420);

    scroller.style.left = position+"px";
}