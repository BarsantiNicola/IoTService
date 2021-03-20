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

    <title>JANET home Service</title>

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

        <%switch(Integer.parseInt(request.getParameter("request_state"))){
            case 0: %>
        <h2 class="registration-form-title">Registration Completed</h2>
        <%  break;
            case 1: %>
        <h2 class="registration-form-title">Confirm your registration</h2>
        <%  break;
            case 2: %>
        <h2 class="registration-form-title">Change Password</h2>
        <%  break;
            case 3: %>
        <h2 class="registration-form-title">Password Changed</h2>
        <%  break;
            default:%>
        <h2 class="registration-form-title">Error</h2>
        <%}%>
        <%switch(Integer.parseInt(request.getParameter("request_state"))){
            case 0: %>
        <div class="row row-space">
            <p class="txt3">You have correctly registered into the JANET home Service.</p>
        </div>
        <%  break;
            case 1: %>
        <div class="row row-space">
            <p class="txt3">We have sent an email to the provided address to confirm your registration</p>
        </div>
        <%  break;
            case 2: %>
        <div class="row row-space">
            <p class="txt3">We have sent an email to the provided address for the password change</p>
        </div>
        <%  break;
            case 3: %>
        <div class="row row-space">
            <p class="txt3">You have correctly updated your password for your JANET home profile.</p>
        </div>
        <%  break;
            default:%>
        <div class="row row-space">
            <p class="txt3">An error as occured during the management of the request</p>
        </div>
        <%}%>

        <div class="container-login-form-btn-2">
            <a class="txt3" href="login.jsp">
                <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                Return to Login
                </a>
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
