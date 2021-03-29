<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html lang="en">
<head>
    <title>JANET home</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="resources/login/images/icons/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/fonts/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/animate/animate.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/css-hamburgers/hamburgers.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/vendor/select2/select2.min.css">
    <link rel="stylesheet" type="text/css" href="resources/login/css/webapp.css">
</head>
<body>
<div class="limiter">
    <div class="container-webapp">
        <div class="header-webapp">
            <div class="account-wrapper">
                <div class="account-info-wrapper">
                    <p class="account-name">Nome</p>
                    <p class="account-surname">Cognome</p>
                </div>
                <img class="js-tilt" src="resources/login/images/logo2.png" alt="logo" data-tilt="">
                <div class="account-button-wrapper">
                    <a href="#" class="btn">
                        <i class="fa fa-cog"></i>
                        <p id="settings_label">Settings</p>
                    </a>

                    <br>
                    <a href="#" class="btn">
                        <i class="fa fa-sign-out"></i>
                        <p id="logout_label">Logout</p>
                    </a>

                </div>
            </div>
        </div>
        <div class="location_menu location">
            <i class="fa fa-plus-square-o" aria-hidden="true" id="add_location"></i>
            <i class="fa fa-angle-left left_direction" aria-hidden="true" id="angle_left" ></i>
            <div class="button-wrapper wrapper" id="button_wrap">
                <div class="scroller" id="scroller">
                </div>
            </div>
            <i class="fa fa-angle-right right_direction" aria-hidden="true" id="angle_right"></i>
        </div>
        <div class="body-webapp" id="body-webapp">
            <div class="add_location_wrapper" id="add_location_page">
                <div class="info_col">
                    <img src="resources/login/images/loc_picture.png" alt=""><br>
                    <p>Remember that for security reasons all the given information will be settled permanently.
                        If you wanna change them delete the location and create another one.
                    </p>
                </div>
                <div class="outer">
                    <div class="inner"></div>
                </div>
                <form class="add_location_form" id="location_form" action="#">
                            <span class="location_form_title">
						        Add Location
					        </span>
                    <label>
                        <input class="input" type="text" name="name" placeholder="Location Name">
                        <i class="fa fa-location-arrow"></i>
                    </label>
                    <label>
                        <input class="input" type="text" name="ip" placeholder="IpAddress:Port">
                        <i class="fa fa-address-card-o"></i>
                    </label>
                    <button class="add_location_submit" id="add_location_sub">Register</button>
                </form>
            </div>
            <div class="location_wrapper">
                <div class="delete_button_wrapper">
                    <a href="#">
                        <p class="location_button" id="delete_location_button">
                            <span class="bg" id="delete_back"></span>
                            <span class="base"></span>
                            <span class="text">Hold then click to Delete</span>
                        </p>
                    </a>
                </div>
                <div class="sublocation_wrapper">
                    <h1 class="heading_sublocation">Default</h1>
                    <div class="divider_sublocation"> <span></span></div>
                    <div class="sublocation_content_wrapper location">
                        <i class="fa fa-plus-square-o" aria-hidden="true"></i>
                        <i class="fa fa-angle-left left_direction" aria-hidden="true" onclick="lclick(this)"></i>
                        <div class="device_wrapper wrapper">
                            <div class="device_scroller scroller">
                                <div class="device">

                                </div>
                                <div class="device">

                                </div>
                            </div>
                        </div>
                        <i class="fa fa-angle-right right_direction" aria-hidden="true" onclick="rclick(this)"></i>
                    </div>

                </div>

            </div>
        </div>
    </div>
</div>

<script src="resources/login/vendor/jquery/jquery-3.2.1.min.js"></script>
<script src="resources/login/vendor/bootstrap/js/popper.js"></script>
<script src="resources/login/vendor/bootstrap/js/bootstrap.min.js"></script>
<script src="resources/login/vendor/select2/select2.min.js"></script>
<script src="resources/login/vendor/tilt/tilt.jquery.min.js"></script>
<script src="resources/login/js/main.js"></script>
<script src="resources/login/js/webapp.js"></script>
</body>
</html>
