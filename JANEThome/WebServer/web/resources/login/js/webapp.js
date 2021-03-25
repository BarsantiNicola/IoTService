const button_wrap = document.getElementById("button_wrap");
const left_angle = document.getElementById("angle_left");
const right_angle = document.getElementById("angle_right");
const body_page = document.getElementById("body-webapp");
let scroller = document.getElementById("scroller");
let position = 0;
let actual_loc = null;

$(window).resize(function() {
    if (button_wrap.getBoundingClientRect().width < scroller.getBoundingClientRect().width){

            left_angle.style.display= "inline";
            right_angle.style.display= "inline";

    } else {
            scroller.style.left="0";
            left_angle.style.display= "none";
            right_angle.style.display= "none";

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

function createLocationPage(name){

    var div = document.createElement('div');
    div.className = "location_page";
    div.id = "location_page_"+name;
    body_page.appendChild(div);

}

function changePage(){
    if( actual_loc != null )
        actual_loc.style.display ="none";
    actual_loc = document.getElementById("location_page_"+this.textContent);
    actual_loc.style.display = "flex";
}

$(window).on('load', function(){
    if (button_wrap.getBoundingClientRect().width < scroller.getBoundingClientRect().width){

        left_angle.style.display= "inline";
        right_angle.style.display= "inline";

    } else {

        left_angle.style.display= "none";
        right_angle.style.display= "none";

    }
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
