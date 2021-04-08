
let emailInput = document.getElementById( "emailInput" );
let passwordInput = document.getElementById( "passInput" );
let hideButton = document.getElementById("passButton");
let errorPlaceholder = document.getElementById( "errorPlaceholder" );
let loadPlaceholder = document.getElementById( "loadingPlaceholder" );
let loginButton = document.getElementById("login_button");
let errorFlag = false;

// function called on hidePassword button click to change the icon and the visibility of the password field
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

//  dual functions to hideButton. Permits to recover the button state on email input change
$("#emailInput").on('keyup',function(){

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        loginButton.style.display = "flex";
        errorFlag = false;
    }
})

//  dual functions to hideButton. Permits to recover the button state on password input change
$('#passInput').on('keyup', function() {

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        loginButton.style.display = "flex";
        errorFlag = false;
    }
})

//  function called on login form submit
$("#login_button").on('click',function(event){
    event.preventDefault();
    let email = emailInput.value;
    let password = passwordInput.value;
    this.style.display = "none";
    if( !email.length || !password.length ) {
        errorPlaceholder.style.display="flex";
        errorFlag = true;
    }else{
        loadPlaceholder.style.display="flex";
        $.ajax({
            url: 'login',
            type: 'POST',
            data: {
                email: email,
                password: sha256(password)
            },
            success: function(msg) {

                window.location = "webapp.jsp"+msg;
            },
            error: function(){

                loadPlaceholder.style.display = "none";
                errorPlaceholder.style.display = "flex";
                errorFlag = true;
            }
        });
    }
})
