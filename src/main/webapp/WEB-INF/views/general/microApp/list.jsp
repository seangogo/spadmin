<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
</head>
<body>
<!-- header -->
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<!-- end header -->
<div class="container-fluid container-spadmin">
    <div class="row row-spadmin">
        <div class="col-spadmin col-sm-3 col-md-2 col-lg-2">
            <div class="panel panel-spadmin">
                <div class="panel-heading">
                    <div class="panel-title">微应用</div>
                </div>
                <div class="panel-body">
                    欢迎使用
                </div>
            </div>
        </div>
        <div class="col-spadmin col-sm-9 col-md-10 col-lg-10">
            <div class="panel panel-spadmin panel-spadmin-content">
                <div class="panel-heading">
                    所有应用
                </div>
                <div class="panel-body">
                    <div class="list-app row">
                        <div class="item col-sm-3 col-md-2 col-lg-2">
                            <h2 class="item-name">审批</h2>
                            <div class="thumbnail">
                                <img src="${contextPath}/assets/src/css/img/app-form.png" />
                            </div>
                            <div class="item-actions">
                                <a href="${contextPath}/microApp/customForm/manager.do">管理</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script>seajs.use('page/general/micro-app/list', function(page){ page.run(); });</script>
</body>
</html>