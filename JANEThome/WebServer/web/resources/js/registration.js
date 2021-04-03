
let nameInput = document.getElementById( "nameInput" );
let surnameInput = document.getElementById( "surnameInput" );
let emailInput = document.getElementById( "emailInput" );
let passwordInput = document.getElementById( "passInput" );
let errorPlaceholder = document.getElementById( "errorPlaceholder" );
let loadPlaceholder = document.getElementById( "loadingPlaceholder" );
let registrationButton = document.getElementById("registration_button");
let hideButton = document.getElementById("passButton");
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

$("#registration_button").on('click',function(event){
    event.preventDefault();
    let name = nameInput.value;
    let surname = surnameInput.value;
    let email = emailInput.value;
    let password = passwordInput.value;
    this.style.display = "none";
    if( !email.length || !name.length || !email.length || !password.length ) {
        errorPlaceholder.style.display="flex";
        errorFlag = true;
    }else{
        loadPlaceholder.style.display="flex";
        $.ajax({
            url: 'registration',
            type: 'POST',
            data: {
                name: name,
                surname: surname,
                email: email,
                password: sha256(password)
            },
            success: function() {

                window.location = "registration.jsp?state=1";
            },
            error: function(){

                loadPlaceholder.style.display = "none";
                errorPlaceholder.style.display = "flex";
                errorFlag = true;
            }
        });
    }
})

$("#nameInput").on('keyup',function(){

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        registrationButton.style.display = "flex";
        errorFlag = false;
    }
})

$("#surnameInput").on('keyup',function(){

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        registrationButton.style.display = "flex";
        errorFlag = false;
    }
})

$("#emailInput").on('keyup',function(){

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        registrationButton.style.display = "flex";
        errorFlag = false;
    }
})

$("#passInput").on('keyup',function(){

    if( errorFlag ) {
        errorPlaceholder.style.display = "none";
        loadPlaceholder.style.display = "none";
        registrationButton.style.display = "flex";
        errorFlag = false;
    }
})
