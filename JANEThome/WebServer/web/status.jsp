<%--
  Created by IntelliJ IDEA.
  User: nico
  Date: 12/03/21
  Time: 00:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%if(request.getParameter("request_state").compareTo("registration") == 0){%>
        <title>JANET home User Registration</title>
    <%}
    else {%>
        <title>JANET home User Error</title>
    <% } %>


<!-- Icons font CSS-->
    <link rel="icon" type="image/png" href="resources/login/images/icons/favicon.ico"/>
    <link href="resources/registration/vendor/mdi-font/css/material-design-iconic-font.min.css" rel="stylesheet" media="all">
    <link href="resources/registration/vendor/font-awesome-4.7/css/font-awesome.min.css" rel="stylesheet" media="all">

    <!-- Vendor CSS-->
    <link href="resources/registration/vendor/select2/select2.min.css" rel="stylesheet" media="all">
    <link href="resources/registration/vendor/datepicker/daterangepicker.css" rel="stylesheet" media="all">

    <!-- Main CSS-->
    <link href="resources/login/css/registration.css" rel="stylesheet" media="all">
</head>

<body>
<div class="container-registration">
    <div class="wrapper wrapper--w680">
        <div class="card card-4">
            <div class="card-body">
                <%switch(Integer.parseInt(request.getParameter("request_state"))){
                    case 0: %>
                        <h2 class="title">Registration Completed</h2>
                        <%  break;
                    case 1: %>
                        <h2 class="title">Confirm your registration</h2>
                        <%  break;
                    case 2: %>
                        <h2 class="title">Reset Password</h2>
                    <%  break;
                    default:%>
                        <h2 class="title">Error</h2>
                <%}%>

                <form method="POST">
                    <%switch(Integer.parseInt(request.getParameter("request_state"))){
                        case 0: %>
                            <div class="row row-space">
                            <p class="paragraph">You have correctly registered into the JANET home Service.</p>
                            </div>
                            <%  break;
                        case 1: %>
                            <div class="row row-space">
                                <p class="paragraph">We have sent an email to the provided address to confirm your registration</p>
                            </div>
                            <%  break;
                        case 2: %>
                            <div class="row row-space">
                                <p class="paragraph">We have sent an email to the provided address for the password change</p>
                            </div>
                            <%  break;

                        default:%>
                            <div class="row row-space">
                                <p class="paragraph">An error as occured during the management of the request</p>
                            </div>
                    <%}%>
                    <div class="input-group">
                        <div class="rs-select2 js-select-simple select--no-search">
                            <div class="select-dropdown"></div>
                        </div>
                    </div>
                    <div class="p-t-15">
                        <button class="buttonC btn btn--radius-2 btn--blue" type="button" onclick="location.href = 'login.jsp';">Return</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

</body><!-- This templates was made by Colorlib (https://colorlib.com) -->

</html>
