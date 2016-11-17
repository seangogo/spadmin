<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link href="${contextPath}/assets/src/css/iconfont/iconfont.css" rel="stylesheet" type="text/css">
</head>
<style>
    #flowinfo {
        padding: 15px;
    }

    .iconselect .iconitem {
        width: 50px;
        height: 50px;
        display: block;
        float: left;
        margin: 5px;
        border-radius: 15px;
        cursor: pointer;
        border: 1px solid transparent;
        overflow: hidden;
        position: relative;
    }
    .iconselect .iconitem.selected {
        border-color: #3f9af9;
    }
    .iconselect .iconitem img {
        max-width: 100%;
        max-height: 100%;
        padding: 10px;
        background: rgba(127,127,127,0.15);
        border-radius: 15px;
    }
    .iconselect .iconitem .icon {
        position: absolute;
        display: none;
        border-radius: 50%;
        bottom: 1px;
        right: 1px;
        color: #fff;
        padding: 3px 2px 2px 3px;
        font-size: 14px;
        background: #3f9af9;
        line-height: 14px;
    }
    .iconselect .iconitem.selected .icon {
        display: block;
    }
</style>
<body>
<!-- header -->
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<!-- end header -->
<div class="container-fluid container-spadmin">
    <div class="row row-spadmin">
        <div class="col-spadmin col-sm-3 col-md-2 col-lg-2">
            <form id="flowinfo" class="form">

            </form>
        </div>
        <div class="col-spadmin col-sm-9 col-md-10 col-lg-10">
            <div class="panel panel-spadmin panel-spadmin-content">
                <div class="panel-heading">流程信息</div>
                <div class="panel-body">
                    <div class="pull-right">
                        <a id="flow-chart" href="javascript:" class="btn btn-blue">
                            配置流程图
                        </a>
                        <a id="new-form" href="javascript:" class="btn btn-blue">
                            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>创建新表单
                        </a>
                    </div>
                    <form class="form-filter form-filter-top form-inline">
                        <div class="form-group">
                        </div>
                    </form>
                    <table id="table-flowforms" class="table-spadmin table">
                        <thead>
                            <tr>
                                <th>表单名称</th>
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

<script type="text/html" id="tmpl-forminfo">
    <input name="id" type="hidden" value="{{id}}">
    <div class="form-group clearfix">
        <input type="submit" class="btn btn-blue" value="保存流程">
        <a id="btn-exit" href="javascript:" class="btn btn-default">取消</a>
    </div>
    <div class="form-group">
        <label class="control-label">流程名称</label>
        <input type="text" class="form-control" name="name" id="name" value="{{name}}">
    </div>
    <div class="form-group">
        <label class="control-label">流程说明</label>
        <textarea  class="form-control" name="des">{{des}}</textarea>
    </div>
    <div class="form-group">
        <label class="control-label">流程类型</label>
        <select id="mostTypeKey" class="form-control" name="mostTypeKey">
            <c:forEach items="${mostType}" var="item">
                <option value="${item.id}" {{mostTypeKey == '${item.id}' ? 'selected' : ''}}>${item.name}</option>
            </c:forEach>
        </select>
    </div>
    <div class="form-group">
        <div class="fieldname">图标</div>
        <div class="fieldblock">
            <input id="iconid" type="hidden" name="icon" value="{{icon}}">
            <div class="iconselect">
                <# _.each(icons, function (item) { #>
                    <label class="iconitem {{icon === item ? 'selected': ''}}" data-id="{{item}}">
                        <i class="icon icon-checked"></i>
                        <img src="${contextPath}/assets/src/css/img/formicons/{{item}}.png" alt="{{item}}">
                    </label>
                <# }) #>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="tmpl-formItem">
    <# _.each(forms, function (item, key) { #>
    <tr>
        <td class="">{{item.formName}}</td>
        <td class="col-actions">
            <div><a href="javascript:" data-id="{{item.formID}}" data-do="edit">编辑</a></div>
            <div><a href="javascript:" data-id="{{item.formID}}" data-do="remove">删除</a></div>
        </td>
    </tr>
    <# }) #>
</script>

<script>
    seajs.use('page/edition-third/flow/flow-manager', function (page) {
        page.run();
    });
</script>
</body>
</html>
