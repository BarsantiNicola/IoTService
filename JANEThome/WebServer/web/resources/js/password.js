
let emailInput = document.getElementById( "emailInput" );
let passwordInput = document.getElementById( "passInput" );
let errorPlaceholder = document.getElementById( "errorPlaceholder" );
let loadPlaceholder = document.getElementById( "loadingPlaceholder" );
let passwordButton = document.getElementById("password_button");
let changeButton = document.getElementById("password_button");
let errorFlag = false;

$("#passButton").on('click', function(event){

    if( hideButton.className === "fa fa-lock"){
        hideButton.className = "fa fa-unlock";
        hideButton.style.color = "#57b846";
        passwordInput.type = "text";
    }else{
        hideButton.className = "fa fa-lock";
        hideButton.style.color = "#721c24";
        passwordInput.type = "password";
    }
    event.preventDefault();

})

$("#password_button").on('click',function(event){
    event.preventDefault();
    let email = emailInput.value;
    this.style.display = "none";
    if( !email.length ) {
        errorPlaceholder.style.display="flex";
        errorFlag = true;
    }else{
        loadPlaceholder.style.display="flex";
        $.ajax({
            url: 'password',
            type: 'POST',
            data: {
                email: email
            },
            success: function() {

                window.location = "password.jsp?state=2";
            },
            error: function(){

                loadPlaceholder.style.display = "none";
                errorPlaceholder.style.display = "flex";
                errorFlag = true;
            }
        });
    }
})

$("#change_button").on('click',function(event){
    event.preventDefault();
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    let password = passwordInput.value;
    this.style.display = "none";
    if( !password.length ) {
        errorPlaceholder.style.display="flex";
        errorFlag = true;
    }else{
        loadPlaceholder.style.display="flex";
        $.ajax({
            url: 'password',
            type: 'POST',
            data: {
                password: sha256(password),
                auth:  urlParams.get("auth")
            },
            success: function() {

                window.location = "password.jsp?state=3";
            },
            error: function(){

                loadPlaceholder.style.display = "none";
                errorPlaceholder.style.display = "flex";
                errorFlag = true;
            }
        });
    }
})

$("#emailInput").on('keyup',function(){

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        passwordButton.style.display = "flex";
        errorFlag = false;
    }
})

$('#passInput').on('keyup', function() {

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        changeButton.style.display = "flex";
        errorFlag = false;
    }
})

function get(name){
    if(name === (new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)')).exec(location.search))
        return decodeURIComponent(name[1]);
}