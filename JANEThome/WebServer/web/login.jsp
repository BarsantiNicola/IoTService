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
        <link rel="stylesheet" type="text/css" href="resources/css/login.css">
        <title>JANET home</title>
    </head>
    <body>
        <div class="login-page">
            <div class="login-container">
                <div class="login-wrapper">
                    <div class="login-pic js-tilt" data-tilt>
                        <img src="resources/pics/logo.png" alt="logo">
                    </div>
                    <form id="login_form" class="login-form validate-form" action="#">
					    <span class="login-form-title">
						    Member Login
					    </span>

                        <div class="wrap-input validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                            <label>
                                <input id="emailInput" class="input" type="email" name="email" placeholder="Email" required>
                            </label>
                            <span class="symbol-input">
						        <i class="fa fa-envelope" aria-hidden="true"></i>
					        </span>
                        </div>

                        <div class="wrap-input validate-input" data-validate = "Password is required">
                            <label>
                                <input id="passInput" class="input" type="password" name="pass" placeholder="Password" required>
                            </label>
                            <span id="pass" class="symbol-input">
						        <i  class="fa fa-key" aria-hidden="true"></i>
					        </span>
                            <span class="hide-page">
						        <i id="passButton" class="fa fa-lock" aria-hidden="true"></i>
					        </span>
                        </div>

                        <div class="container-login-form-button">
                            <button id="login_button" class="login-form-button">Login</button>
                            <span id="loadingPlaceholder" class="loading_placeholder" >
                                <img src="resources/pics/loading.gif" alt="#">
                            </span>
                            <span id="errorPlaceholder" class="error_placeholder" >
                                <i class="fa fa-times" aria-hidden="true"></i>
                            </span>
                        </div>

                        <div class="text-center p-t-12">
					        <span class="txt1">
						        Forgot
                            </span>
                            <a class="txt2" href="#" onclick="location.href = 'password.jsp';">
                                Username / Password?
                            </a>
                        </div>

                        <div class="text-center p-t-136">
                            <a class="txt2" href="registration">
                                    Create your Account
                                <i class="fa fa-long-arrow-right m-l-5" aria-hidden="true"></i>
                            </a>
                        </div>

                    </form>
                </div>
            </div>
        </div>

        <script src="resources/vendor/jquery/jquery-3.2.1.min.js"></script>
        <script src="resources/vendor/bootstrap/js/popper.js"></script>
        <script src="resources/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="resources/vendor/select2/select2.min.js"></script>
        <script src="resources/vendor/tilt/tilt.jquery.min.js"></script>
        <script src="resources/js/crypto.js"></script>
        <script src="resources/js/login.js"></script>

    </body>
</html>