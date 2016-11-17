<%@ page language="java" pageEncoding="utf-8"%>
<nav class="navbar navbar-default navbar-static-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="javascript:">
                <span class="navbar-logo">V</span><span class="navbar-head"><c:out value="${sessionScope.companyName}"/>管理后台</span>
            </a>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li>
                <a href="${contextPath}/exit.do">退出</a>
            </li>
        </ul>
    </div>
</nav>
