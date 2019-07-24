<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title></title>
</head>
<body>
<#import "/spring.ftl" as spring />
<#global ctx = request.contextPath />
<#if errorInfo?? && errorInfo.error??>
    ${errorInfo.error}
<#else>
    <@spring.message "errors.error.notavailable"/>
</#if>(${.now?string("yyyy-MM-dd HH:mm:ss")})
<#if errorInfo?? && errorInfo.linkUrl??>
    <a href="${errorInfo.linkUrl}" target="_self"><@spring.message "common.label.back"/></a>
</#if>
</body>
</html>