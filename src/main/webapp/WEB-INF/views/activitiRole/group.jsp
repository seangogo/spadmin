<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
<style>
.ztree * { font-size: 16px; }
.ztree li a { height: 30px; }
.ztree li { line-height: 29px; }
.ztree li span { line-height: 30px; }
.ztree li a.curSelectedNode { height: auto; }
.role-users .heading .title { margin: 0; padding: 20px 0; font-size: 16px; }
.role-users .action,
.role-users .role-user-list,
.role-users .user-list { height: 400px; }
.role-users .role-user-list .item { padding: 5px 10px; border-bottom: 1px solid #f5f5f5; line-height: 1.5 }
.role-users .role-user-list .item a { float: right; }
.role-users .role-user-list,
.role-users .user-list { background-color: #fff; overflow-y:auto; border:1px solid #ccc; }
.role-users .action button { position: absolute; top: 50%; }
.role-users .nodata,
.role-users .loading { padding: 15px; }
</style>
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
                    <a href="${contextPath}/role/group.do" class="list-group-item active">
                        群组管理
                    </a>
                </div>
                <!--<div class="panel-heading">管理员管理</div>
                <div class="list-group">
                    <a href="${contextPath}/role/group.do" class="list-group-item">
                        管理员
                    </a>
                    <a href="${contextPath}/role/permission.do" class="list-group-item">
                        权限
                    </a>
                </div>-->
            </div>
        </div>
        <div class="col-sm-9 col-md-10 col-lg-10">
            <div class="row">
                <div class="col-sm-3 col-md-2 col-lg-2">
                    <div class="m-t"><a href="javascript:void(0);" data-do="create:orgOrGroup" class="btn btn-grey"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> 新建</a></div>
                    <ul id="role-tree" class="role-tree ztree m-t"></ul>
                </div>
                <div id="role-users" class="col-sm-9 col-md-10 col-lg-10"></div>
            </div>
        </div>
    </div>
</div>
<%@ include file="./modal.jsp"%>
<!-- page scripts -->
<script>
seajs.use('page/role/group', function(module) {
    module.run();
});
</script>
<!-- end page scripts -->
</body>
</html>
