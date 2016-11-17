<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
</head>
<body>
<!-- header -->
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<!-- end header -->
<div class="container-fluid container-spadmin">
    <div class="row row-spadmin">
        <div class="col-spadmin col-sm-3 col-md-2 col-lg-2">
            <div class="panel panel-spadmin menu-spadmin">
                <div class="panel-heading">
                    <div class="panel-title">设置</div>
                </div>
                <div class="list-group">
                    <a href="javascript:" class="list-group-item active">
                        修改密码
                    </a>
                </div>
            </div>
        </div>
        <div class="col-spadmin col-sm-9 col-md-10 col-lg-10">
            <div class="panel panel-spadmin panel-spadmin-content">
                <div class="panel-heading">
                    <div class="panel-title">修改密码</div>
                </div>
                <div class="panel-body">
                    <form id="password-form" class="form-horizontal" action="${contextPath}/users/modifyPwd.do" method="post" style="display: none;">
                        <div class="form-group">
                            <label for="prePwd" class="col-sm-2 control-label">原密码</label>
                            <div class="col-sm-6">
                                <input type="password" class="form-control" name="prePwd"
                                    id="prePwd" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="newPwd" class="col-sm-2 control-label">新密码</label>
                            <div class="col-sm-6">
                                <input type="password" class="form-control" name="newPwd"
                                    id="newPwd" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="surePwd" class="col-sm-2 control-label">确认新密码</label>
                            <div class="col-sm-6">
                                <input type="password" class="form-control" name="surePwd"
                                    id="surePwd" autocomplete="off">
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-10 col-sm-offset-2">
                                <button type="submit" class="btn btn-blue">提交</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- page scripts -->
<script>
    seajs.use('page/general/setting/password', function (module) {
        module.run();
    });
</script>
<!-- end page scripts -->
</body>
</html>
