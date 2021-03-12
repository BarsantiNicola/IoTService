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
    <title>JANET home User Registration</title>

    <!-- Icons font CSS-->
    <link rel="icon" type="image/png" href="resources/login/images/icons/favicon.ico"/>
    <link href="resources/registration/vendor/mdi-font/css/material-design-iconic-font.min.css" rel="stylesheet" media="all">
    <link href="resources/registration/vendor/font-awesome-4.7/css/font-awesome.min.css" rel="stylesheet" media="all">

    <!-- Vendor CSS-->
    <link href="resources/registration/vendor/select2/select2.min.css" rel="stylesheet" media="all">
    <link href="resources/registration/vendor/datepicker/daterangepicker.css" rel="stylesheet" media="all">

    <!-- Main CSS-->
    <link href="resources/registration/css/registration.css" rel="stylesheet" media="all">
</head>

<body>
    <div class="container-registration">
        <div class="wrapper wrapper--w680">
            <div class="card card-4">
                <div class="card-body">
                    <div class="login100-pic js-tilt pic-reg" data-tilt>
                        <img src="resources/login/images/logo.png" alt="IMG">
                    </div>
                    <h2 class="title">Registration Form</h2>

                    <form method="POST">
                        <div class="row row-space">

                            <div class="col-2">
                                <div class="wrap-input100 validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                                    <input class="input100" type="text" name="name" placeholder="First Name">
                                    <span class="focus-input100"></span>
                                    <span class="symbol-input100">
                                        <i class="fa fa-user-circle" aria-hidden="true"></i>
						            </span>
                                </div>
                            </div>
                            <div class="col-2">
                                <div class="wrap-input100 validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                                    <input class="input100" type="text" name="surname" placeholder="Last Name">
                                    <span class="focus-input100"></span>
                                    <span class="symbol-input100">
                                        <i class="fa fa-user-circle" aria-hidden="true"></i>
						            </span>
                                </div>
                            </div>
                        </div>
                        <div class="row row-space">
                            <div class="col-2">
                                <div class="wrap-input100 validate-input" >
                                </div>

                            </div>
                        </div>
                        <div class="row row-space">
                            <div class="col-2">
                                <div class="wrap-input100 validate-input" data-validate = "Valid email is required: ex@abc.xyz">
                                    <input class="input100" type="text" name="email" placeholder="Email">
                                    <span class="focus-input100"></span>
                                    <span class="symbol-input100">
                                        <i class="fa fa-envelope" aria-hidden="true"></i>
						            </span>
                                </div>
                            </div>
                            <div class="col-2">
                                <div class="wrap-input100 validate-input">
                                    <input class="input100" type="password" name="password" placeholder="Password">
                                    <span class="focus-input100"></span>
                                    <span class="symbol-input100">
                                        <i class="fa fa-lock" aria-hidden="true"></i>
						            </span>
                                </div>
                            </div>
                        </div>
                        <div class="input-group">
                            <div class="rs-select2 js-select-simple select--no-search">
                                <div class="select-dropdown"></div>
                            </div>
                        </div>
                        <div class="p-t-15">
                            <button class="btn btn--radius-2 btn--blue" type="button" onclick="location.href = 'login.jsp';">Return</button>
                            <button class="btn btn--radius-2 btn--green" type="submit">Submit</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

</body><!-- This templates was made by Colorlib (https://colorlib.com) -->


</html>
<!-- end document-->