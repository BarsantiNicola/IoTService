<!DOCTYPE html>
<html lang="en">

    <head>
        <meta name="author" content="Barsanti Nicola">
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="icon" type="image/png" href="resources/pics/logo2.png"/>
        <link rel="stylesheet" type="text/css" href="resources/login/vendor/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" type="text/css" href="resources/login/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
        <link rel="stylesheet" type="text/css" href="resources/login/vendor/animate/animate.css">
        <link rel="stylesheet" type="text/css" href="resources/login/vendor/select2/select2.min.css">
        <link rel="stylesheet" type="text/css" href="resources/css/password.css">
        <title>JANET home</title>
    </head>

    <body>
        <div class="password-page">
            <div class="page-wrapper">
                <%int state = Integer.parseInt(request.getParameter("state")==null?"0":request.getParameter("state"));
                switch(state){
                    case 0: case 1:
                        if( state == 0 ){%>
                <h1 class="password-form-title">Password Change</h1>
                <p class="txt">Insert the mail associated with your account to change your password</p>
                <form class="password-form">
                    <div class="wrap-input">
                        <label>
                            <input id="emailInput" class="input" type="email" name="email" placeholder="Email" required>
                        </label>
                        <span class="symbol-input">
                            <i class="fa fa-envelope" aria-hidden="true"></i>
                        </span>
                    </div>
                    <div class="container-password-button">
                        <button id="password_button" class="password-form-button">Send</button>
                        <%}else{%>
                <h1 class="password-form-title">Set Password</h1>
                <p class="txt">Insert your new password</p>
                <form class="password-form">
                    <div class="wrap-input">
                        <label>
                            <input id="passInput" class="input" type="password" name="pass" placeholder="Password" required>
                        </label>
                        <span id="pass" class="symbol-input">
                            <i  class="fa fa-lock" aria-hidden="true"></i>
                        </span>
                        <span class="hide-page">
                            <i id="passButton" class="fa fa-lock" aria-hidden="true"></i>
                        </span>
                    </div>
                    <div class="container-password-button">
                        <button id="change_button" class="password-form-button">Send</button>
                        <%}%>

                        <span id="loadingPlaceholder" class="loading_placeholder" >
                            <img src="resources/pics/loading.gif" alt="#">
                        </span>
                        <span id="errorPlaceholder" class="error_placeholder" >
                            <i class="fa fa-times" aria-hidden="true"></i>
                        </span>
                    </div>
                </form>
                <a class="txt p-t-50" href="login.jsp">
                    <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                    Home
                </a>
                    <%break;
                    case 2: %>
                <h1 class="password-form-title">Email Sent</h1>
                <p class="txt2">We have sent an email to your mailbox. Open it to change your password</p>
                <a class="txt p-t-50" href="login.jsp">
                    <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                    Home
                </a>
                    <%  break;
                    case 3: %>
                <h1 class="password-form-title">Password Changed</h1>
                <p class="txt2">Your password has correctly been changed</p>
                <a class="txt p-t-50" href="login.jsp">
                    <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                    Home
                </a>
                    <% break;
                    default: %>
                <h1 class="password-form-title">Error</h1>
                <p class="txt2">An error has occured</p>
                <a class="txt p-t-50" href="login.jsp">
                    <i class="fa fa-long-arrow-left m-l-5" aria-hidden="true"></i>
                    Home
                </a>
                <%}%>
            </div><%// INTELLIJ BAD JSP SUPPORT. COMMENT TO HIDE TAG LAYERS ERROR OUTPUT%>
        </div><%// INTELLIJ BAD JSP SUPPORT. COMMENT TO HIDE TAG LAYERS ERROR OUTPUT%>

        <script src="resources/login/vendor/jquery/jquery-3.2.1.min.js"></script>
        <script src="resources/login/vendor/bootstrap/js/popper.js"></script>
        <script src="resources/login/vendor/bootstrap/js/bootstrap.min.js"></script>
        <script src="resources/login/vendor/select2/select2.min.js"></script>
        <script src="resources/login/vendor/tilt/tilt.jquery.min.js"></script>
        <script src="resources/js/crypto.js"></script>
        <script src="resources/js/password.js"></script>
    </body>
</html>