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
    <title>JANEThome Password Change</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="resources/login/images/icons/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/animate/animate.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/css-hamburgers/hamburgers.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/select2/select2.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/util.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/password.css">
    <link href="resources/login/css/registration.css" rel="stylesheet" media="all">
</head>

<body>

<div class="container-registration">
    <div class="wrap-login">

        <h2 class="registration-form-title">Password Change</h2>

        <form class="registration-form" method="POST" id="password_form" action="password">
            <div class="row row-space">
                <p class="txt2">Insert a new password for your profile</p>
            </div>
            <div class="row row-space">

                <div class="wrap-input validate-input" data-validate = "Password is required">
                    <label>

                        <input id="passInput" class="input" type="password" name="password" placeholder="Password">
                    </label>
                    <span id="pass" class="symbol-input">
                                <i  class="fa fa-user-circle" aria-hidden="true"></i>
                        </span>
                    <input type="hidden" name="token" value=<% out.print('"'+request.getParameter("token")+'"');%>>
                </div>

                <div class="row2">
                    <div class="container-login-form-btn">
                        <button type="submit" class="login-form-btn">Submit</button>
                    </div>
                </div>
            </div>
            <div class="row2">

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
