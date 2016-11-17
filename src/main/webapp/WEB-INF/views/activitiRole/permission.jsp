<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
</head>
<body>
<!-- header -->
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<!-- end header -->
<div class="container-fluid">
    <div class="row">
        <div class="col-sm-3 col-md-2 col-lg-2 no-col-padding">
            <div class="panel panel-spadmin menu-spadmin">
                <div class="panel-heading">人员</div>
                <div class="list-group">
                    <a href="${contextPath}/user/manager.do" class="list-group-item">
                        人员管理
                    </a>
                    <a href="${contextPath}/role/role.do" class="list-group-item">
                        角色管理
                    </a>
                    <a href="${contextPath}/role/group.do" class="list-group-item">
                        群组管理
                    </a>
                </div>
                <!--<div class="panel-heading">管理员管理</div>
                <div class="list-group">
                    <a href="${contextPath}/role/group.do" class="list-group-item">
                        管理员
                    </a>
                    <a href="${contextPath}/role/group.do" class="list-group-item active">
                        权限
                    </a>
                </div>-->
            </div>
        </div>
        <div class="col-sm-9 col-md-10 col-lg-10">
            <div class="row">
                <div class="panel panel-spadmin panel-spadmin-content">
                    <div class="panel-heading">权限列表</div>
                    <div class="panel-body">
                        <div class="pull-right" role="group" aria-label="...">
                                <a href="javascript·:" class="btn btn-green btn-import"><span class="glyphicon glyphicon-floppy-open" aria-hidden="true"></span>保存修改</a>
                            </div>
                        <form class="form-filter form-filter-top form-inline"></form>
                        <table id="table-employee" class="table-employee table table-spadmin">
                            <thead>
                                <tr>
                                    <th>姓名</th>
                                    <th>手机号码</th>
                                    <th>产品列表</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody role="items">
                            </tbody>
                        </table>
                    </div>
                    <div class="panel-footer clearfix">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- <script>
    seajs.use('page/role/permission', function(module) {
        module.run();
    });
</script> -->
<!-- end page scripts -->
</body>
</html>
