let login_form;
let registration_form;
let password_form;

const formType = {
    MAIN: "main",
    REGISTRATION: "registration",
    PASSWORD: "password"
}


function init(){

    login_form = document.getElementById( "wrap_main_login" );
    registration_form = document.getElementById( "wrap_registration_login" );
    password_form = document.getElementById( "wrap_password_login" );

}



function showForm( fType ){

    switch( fType ){

        case formType.MAIN:
            registration_form.style.visibility = "hidden";
            password_form.style.visibility = "hidden";
            login_form.style.visibility = "visible";
            break;

        case formType.REGISTRATION:
            login_form.style.visibility = "hidden";
            password_form.style.visibility = "hidden";
            registration_form.style.visibility ="visble";
            break;

        case formType.PASSWORD:
            login_form.style.visibility = "hidden";
            registration_form.style.visibility ="hidden";
            password_form.style.visibility = "visible";
            break;

    }

}
