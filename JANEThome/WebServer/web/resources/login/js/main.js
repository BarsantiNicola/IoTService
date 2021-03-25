
(function ($) {
    "use strict";

    const input = $('.validate-input .input');

    $('.validate-form').on('submit',function(){
        let check = true;

        for(let i=0; i<input.length; i++) {
            if(validate(input[i]) === false){
                showValidate(input[i]);
                check=false;
            }
        }

        return check;
    });


    $('.validate-form .input').each(function(){
        $(this).focus(function(){
           hideValidate(this);
        });
    });

    function validate (input) {
        if($(input).attr('type') === 'email' || $(input).attr('name') === 'email') {
            if($(input).val().trim().match(/^([a-zA-Z0-9_\-]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(?)$/) == null) {
                return false;
            }
        }
        else {
            if($(input).val().trim() === ''){
                return false;
            }
        }
    }

    function showValidate(input) {
        const thisAlert = $(input).parent();

        $(thisAlert).addClass('alert-validate');
    }

    function hideValidate(input) {
        const thisAlert = $(input).parent();

        $(thisAlert).removeClass('alert-validate');
    }
    
    

})(jQuery);

$('.js-tilt').tilt({
    scale: 1.1
})
function passwordHide(){
        alert("ok");
        var elem = document.getElementById("passInput");
        var icon = document.getElementById('pass');
        if (elem.getAttribute('type') === 'password') {
            elem.setAttribute('type', 'text');
            icon.classList.replace('fa fa-lock', 'fa fa-unlock');
        } else {
            elem.setAttribute('type', 'password');
            icon.classList.replace('fa fa-unlock', 'fa fa-lock');
        }
}

async function sha256(message) {

    const msgBuffer = new TextEncoder().encode(message);
    const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
    return Array.from(new Uint8Array(hashBuffer));

}


