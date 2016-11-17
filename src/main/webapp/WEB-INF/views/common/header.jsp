<%@ page language="java" pageEncoding="utf-8"%>
<div class="bg-layer">
    <div class="row row-spadmin">
        <div class="col-spadmin bg-left col-sm-3 col-md-2 col-lg-2">
        </div>
        <div class="col-spadmin bg-right col-sm-9 col-md-10 col-lg-10">
        </div>
    </div>
</div>
<nav class="navbar navbar-static-top navbar-spadmin">
    <div class="container-fluid">
        <div class="navbar-header">
            <div class="navbar-brand">
                <span class="navbar-logo"></span>
                <span class="navbar-head">
                    江苏移动MOA管理平台&nbsp;(单位：<c:out value="${sessionScope.company.customerName}"/>)
                </span>
            </div>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li>
                <a href="${contextPath}/user/manager.do">人员管理</a>
            </li>
            <li>
                <a href="${contextPath}/microApp/customForm/manager.do">微应用</a>
            </li>
            <li>
                <a href="${contextPath}/users/passwordManager.do">设置</a>
            </li>
            <li>
                <a id="exit-btn" href="javascript:">退出</a>
            </li>
        </ul>
    </div>
</nav>
