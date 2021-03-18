
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>JANET home</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="resources/login/images/icons/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/animate/animate.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/css-hamburgers/hamburgers.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/select2/select2.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/util.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/main.css">
</head>

<body onload="init()">

        <div class="limiter">
            <div id="wrap_main_login" class="container-login">
                <div  class="wrap-login">
                    <div class="login-pic js-tilt" data-tilt>
                        <img src="resources/login/images/logo.png" alt="logo">
                    </div>

                    <form class="login-form validate-form" action="auth">
					    <span class="login-form-title">
						    Member Login
					    </span>

                        <div class="wrap-input validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                            <label>
                                <input class="input" type="text" name="email" placeholder="Email">
                            </label>
                            <span class="focus-input"></span>
                            <span class="symbol-input">
						        <i class="fa fa-envelope" aria-hidden="true"></i>
					        </span>
                        </div>

                        <div class="wrap-input validate-input" data-validate = "Password is required">
                            <label>
                                <input class="input" type="password" name="pass" placeholder="Password">
                            </label>
                            <span class="focus-input"></span>
                            <span class="symbol-input">
						        <i class="fa fa-lock" aria-hidden="true"></i>
					        </span>
                        </div>

                        <div class="container-login-form-btn">
                            <button class="login-form-btn">Login</button>
                        </div>

                        <div class="text-center p-t-12">
					        <span class="txt1">
						        Forgot
                            </span>
                            <a class="txt2" href="#" onclick="location.href = 'password';">
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

        <script src="resources/login/vendor/jquery/jquery-3.2.1.min.js"></script>
        <script src="resources/login/vendor/bootstrap/js/popper.js"></script>
        <script src="resources/login/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="resources/login/vendor/select2/select2.min.js"></script>
        <script src="resources/login/vendor/tilt/tilt.jquery.min.js"></script>
        <script src="resources/login/js/main.js"></script>

    </body>
</html>
