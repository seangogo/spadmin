<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/src/css/login.css" />
</head>
<body>

<form id="form-login" class="form-login" method="post" action="${contextPath}/user/checkPass.do" style="display: none;">
    <div class="form-login-heading"></div>
    <div class="form-login-content">
        <h3 class="form-login-title">江苏移动MOA管理平台</h3>
        <div class="alert-login alert alert-danger hide" role="alert"></div>
        <label for="inputUsername" class="sr-only">手机号</label>
        <div class="control-box">
            <input type="text" id="inputUsername" name="username" class="form-control user" placeholder="请输入手机号码" autofocus autocomplete="off">
        </div>
        <label for="inputPassword" class="sr-only">密码</label>
        <div class="control-box">
            <input type="password" id="inputPassword" name="password" class="form-control password" placeholder="请输入密码" autocomplete="off">
        </div>
        <button class="btn btn-login" type="submit">登录</button>
    </div>
</form>

<form id="form-company" class="form-login" method="post" action="${contextPath}/user/checkLogin.do" style="display: none;">
    <div class="form-login-heading"></div>
    <div class="form-login-content">
        <h3 class="form-login-title">江苏移动MOA管理平台</h3>
        <div class="alert-company alert alert-danger hide" role="alert"></div>
        <label for="inputPassword" class="sr-only">公司组织</label>
        <div class="control-box">
            <select id="company" name="company" class="form-control"></select>
        </div>
        <button class="btn btn-login" type="submit">确定</button>
    </div>
</form>

<script>seajs.use('page/login', function(page){ page.run(); });</script>
</body>
</html>
