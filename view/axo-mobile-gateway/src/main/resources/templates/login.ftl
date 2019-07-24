<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>登陆</title>
</head>
<body>
<div class="main">
    <form method="post" name="loginForm" action="j_spring_security_check" novalidate>
        <div class="login_area p-4">
            <div class="mx-3">
                <p class="success mb-3"><b>登陆</b></p>
                <div class="login_input">
                    <div class="has-feedback search-input mb-3">
                        <input type="text" value="citsamex" class="form-control" name="companyCode">
                        <i class="mdi mdi-14px mdi-briefcase form-control-feedback"></i>
                    </div>
                    <div class="has-feedback search-input mb-3">
                        <input type="text" value="gary.fu@citsgbt.com" class="form-control" name="username">
                        <i class="mdi mdi-14px mdi-account form-control-feedback"></i>
                    </div>
                    <div class="has-feedback search-input mb-3">
                        <input class="form-control" value="123456b" type="password" name="password">
                        <i class="mdi mdi-14px mdi-account-key form-control-feedback"></i>
                    </div>
                </div>
                <div class="clearfix mb-2">
                    <div class="float-left">
                        <div class="checkbox my-0">
                            <label><input type="checkbox" name="remember-me">记住登陆</label>
                        </div>
                    </div>
                </div>
            </div>
            <#if errorInfo??>
                <div>${errorInfo.error!''}</div>
            </#if>
            <input class="btn btn-lg btn-success w-100 mb-2 py-2" type="submit" value="登陆"/>
        </div>
    </form>
</div>
</body>
</html>