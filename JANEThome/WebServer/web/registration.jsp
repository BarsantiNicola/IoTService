
<!DOCTYPE html>
<html lang="en">

<head>

    <!-- Required meta tags-->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="Colorlib Templates">
    <meta name="author" content="Colorlib">
    <meta name="keywords" content="Colorlib Templates">

    <!-- Title Page-->
    <title>JANEThome Registration</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="resources/login/images/icons/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/animate/animate.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/css-hamburgers/hamburgers.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/select2/select2.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/util.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/registration.css">
    <link href="resources/login/css/registration.css" rel="stylesheet" media="all">
</head>

<body>

    <div class="container-registration">
        <div class="wrap-login">

            <div class="login-pic resource-pic js-tilt pic-reg" data-tilt>
                 <img src="resources/login/images/logo.png" alt="IMG">
            </div>
            <h2 class="registration-form-title">Registration Form</h2>

            <form method="POST" id="registration_form" action="registration">
                <div class="row row-space">

                    <div class="col-2">
                        <div class="wrap-input validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                            <label>
                                <input class="input" type="text" name="name" placeholder="First Name">
                            </label>
                            <span class="symbol-input">
                                <i class="fa fa-user-circle" aria-hidden="true"></i>
                            </span>
                        </div>
                    </div>
                    <div class="col-2">
                        <div class="wrap-input validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                            <label>
                                <input class="input" type="text" name="surname" placeholder="Last Name">
                            </label>
                            <span class="symbol-input">
                                <i class="fa fa-user-circle" aria-hidden="true"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row row-space">
                    <div class="col-2">
                        <div class="wrap-input validate-input" >
                        </div>
                    </div>
                </div>
                <div class="row row-space">
                    <div class="col-2">
                        <div class="wrap-input validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                            <label>
                                <input class="input" type="text" name="email" placeholder="boo">
                            </label>
                            <span class="symbol-input">
                                <i class="fa fa-envelope" aria-hidden="true"></i>
                            </span>
                        </div>
                    </div>
                    <div class="col-2">
                        <div class="wrap-input validate-input">
                            <label>
                                <input id="passInput" class="input"  type="password" name="password" placeholder="Password">
                            </label>
                            <span  class="symbol-input">
                                <i id="pass" class="fa fa-lock" aria-hidden="true"></i>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="row2">
                    <div class="container-login-form-btn">
                        <button type="submit" class="login-form-btn">Submit</button>
                    </div>
                    <a class="txt2" href="login.jsp">
                        <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                        Return to Login
                    </a>

                </div>
            </form>
        </div>
    </div>

    <script src="resources/login/vendor/jquery/jquery-3.2.1.min.js"></script>
    <script src="resources/login/vendor/bootstrap/js/popper.js"></script>
    <script src="resources/login/vendor/bootstrap/js/bootstrap.min.js"></script>
    <script src="resources/login/vendor/select2/select2.min.js"></script>
    <script src="resources/login/vendor/tilt/tilt.jquery.min.js"></script>
    <script src="resources/login/js/main.js"></script>
</body><!-- This templates was made by Colorlib (https://colorlib.com) -->

</html>
<!-- end document-->