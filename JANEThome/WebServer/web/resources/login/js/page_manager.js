
const scroller = document.getElementById("scroller");

function createButton(name, ipAddress="127.0.0.1", port="80"){

    alert("executing2");
    let button = document.createElement('a');
    let paragraph = document.createElement('p');

    alert("executing3");
    button.href="#";

    let span = document.createElement('span');
    span.className = "bg";
    paragraph.appendChild(span);
    span = document.createElement( 'span' );
    span.className = "base";
    paragraph.appendChild(span);
    span = document.createElement( 'span ');
    span.className = "text";
    span.textContent = name;
    paragraph.appendChild(span);
    button.appendChild(paragraph);
    scroller.appendChild( button );

}