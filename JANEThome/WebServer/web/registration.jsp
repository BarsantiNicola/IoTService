<!DOCTYPE html>
<html lang="en">

    <head>
        <meta name="author" content="Barsanti Nicola">
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="icon" type="image/png" href="resources/pics/logo2.png"/>
        <link rel="stylesheet" type="text/css" href="resources/vendor/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" type="text/css" href="resources/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
        <link rel="stylesheet" type="text/css" href="resources/vendor/animate/animate.css">
        <link rel="stylesheet" type="text/css" href="resources/vendor/select2/select2.min.css">
        <link rel="stylesheet" type="text/css" href="resources/css/registration.css">
        <title>JANET home</title>
    </head>

    <body>
        <div class="registration-page">
            <div class="page-wrapper">
                <% switch(Integer.parseInt(request.getParameter("state")==null?"0":request.getParameter("state"))){
                        case 0:%>
                <div class="registration-pic js-tilt" data-tilt>
                    <img src="resources/pics/logo.png" alt="logo">
                </div>
                <h1 class="registration-form-title">Registration</h1>
                <form class="registration-form">
                    <div class="row-space">
                        <div class="wrap-input">
                            <label>
                                <input id="nameInput" class="input" type="text" name="first-name" placeholder="First Name" required>
                            </label>
                            <span class="symbol-input">
                                <i class="fa fa-user-circle" aria-hidden="true"></i>
                            </span>
                        </div>
                        <div class="wrap-input">
                            <label>
                                <input id="surnameInput" class="input" type="text" name="Surname" placeholder="Last Name" required>
                            </label>
                            <span class="symbol-input">
                                <i class="fa fa-user-circle" aria-hidden="true"></i>
                            </span>
                        </div>
                    </div>
                    <div class="row-space">
                        <div class="wrap-input">
                            <label>
                                <input id="emailInput" class="input" type="email" name="email" placeholder="Email" required>
                            </label>
                            <span class="symbol-input">
                                <i class="fa fa-envelope" aria-hidden="true"></i>
                            </span>
                        </div>
                        <div class="wrap-input row-input">
                            <label>
                                <input id="passInput" class="input" type="password" name="pass" placeholder="Password" required>
                                <span id="pass" class="symbol-input">
                                    <i  class="fa fa-lock" aria-hidden="true"></i>
                                </span>
                                <span class="hide-page">
                                    <i id="passButton" class="fa fa-lock" aria-hidden="true"></i>
                                </span>
                            </label>


                        </div>
                    </div>

                    <div class="container-registration-button">
                        <button id="registration_button" class="registration-form-button">Send</button>
                        <span id="loadingPlaceholder" class="loading_placeholder" >
                            <img src="resources/pics/loading.gif" alt="#">
                        </span>
                        <span id="errorPlaceholder" class="error_placeholder" >
                            <i class="fa fa-times" aria-hidden="true"></i>
                        </span>
                    </div>
                </form>
                <%  break;
                    case 1:%>
                <h1 class="registration-form-title">Confirm your account</h1>
                <p class="txt2">We have sent an email to the provided email. Open it to confirm your account and been able to use our service</p>
                <%  break;
                    case 2:%>
                <h1 class="registration-form-title">Welcome!</h1>
                <p class="txt2">Welcome to JANEThome!<br> You can now login to our service to install and control your JANETHome IoT devices</p>
                <%  break;
                    default:%>
                <h1 class="registration-form-title">Error</h1>
                <p class="txt2">An error as occurred during your request management</p>
                <%}%>
                <a class="txt p-t-50" href="login.jsp">
                    <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                    Home
                </a>
            </div>

        </div>

        <script src="resources/vendor/jquery/jquery-3.2.1.min.js"></script>
        <script src="resources/vendor/bootstrap/js/popper.js"></script>
        <script src="resources/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="resources/vendor/select2/select2.min.js"></script>
        <script src="resources/vendor/tilt/tilt.jquery.min.js"></script>
        <script src="resources/js/crypto.js"></script>
        <script src="resources/js/registration.js"></script>
    </body>

</html>