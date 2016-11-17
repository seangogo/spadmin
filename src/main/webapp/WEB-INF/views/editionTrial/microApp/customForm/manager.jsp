<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
<link rel="stylesheet" href="${contextPath}/assets/src/css/modal-approval-setting.css" />
</head>
<body>
<!-- header -->
<%@ include file="/WEB-INF/views/common/header-trial.jsp"%>
<!-- end header -->
<div class="container">
    <div class="row">
        <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="panel-title">审批</div>
                </div>
                <div class="panel-body">
                    <ul class="nav nav-pills nav-stacked">
                        <li class="active"><a href="javascript:">审批管理</a></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-xs-12 col-sm-6 col-md-8 col-lg-9">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="panel-title">审批列表</div>
                </div>
                <div class="panel-body">
                    <div class="btn-group pull-right" role="group" aria-label="...">
                        <a id="input-url" href="${contextPath}/trialEdition/customForm/input.do" class="btn btn-primary"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span>创建新审批</a>
                    </div>
                    <form class="form-filter form-filter-top form-inline">
                        <div class="form-group">
                            <label class="control-label" for="inputApprovalType">类型</label>
                            <select name="approvalType" id="inputApprovalType" class="form-control">
                                <option value="">所有审批</option>
                                <c:forEach items="${data}" var="item">
                                <option value="${item.id}">${item.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </form>
                    <table id="table-approval" class="table-approval table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th>审批名称</th>
                                <th>说明</th>
                                <!-- <th>备注</th> -->
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody role="items">
                            <script type="text/html" id="tmpl-approvalItem">
                            <td class="col-form">{{name}}</td>
                            <td>{{des}}</td>
                            <td class="col-actions">
							<# if(isBoutique != null && isBoutique == 1) { #>
                         	<a href="javascript:" data-do="thirdlink">配置第三方流程</a>
  							<# } else{ #>
   							<a href="${contextPath}/trialEdition/customForm/input.do?id={{id}}">编辑</a> <a href="javascript:void(0);" data-do="setApprovalUser">审批人设置</a>
 							<# } #>
                            </td>
                            </script>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/html" id="tmpl-approvalSettingModal">
<%-- modal-approval-setting --%>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span></button>
            <h4 class="modal-title">{{name}} 设置</h4>
        </div>
        <div class="modal-body">
            <div class="approval-setting-body row">
                <div class="panel-sidebar col-sm-4 col-md-4 col-lg-4">
                    <div class="panel-sidebar-body" role="depts">
                        <ul id="dept-tree" class="dept-tree ztree"></ul>
                    </div>
                </div>
                <div class="panel-main col-sm-8 col-md-8 col-lg-8">
                    <form class="approver-selection clearfix" role="approval-users">
                        <ul class="nav nav-tabs" role="tablist">
                            <li role="presentation" class="active"><a href="#defApprovalUser" role="tab" data-toggle="tab">默认审批人</a></li>
                            <li role="presentation"><a href="#lastApprovalUser" role="tab" data-toggle="tab">归口办理人</a></li>
                        </ul>
                        <div class="tab-content">
                            <div role="tabpanel" class="tab-pane clearfix active" id="defApprovalUser"></div>
                            <div role="tabpanel" class="tab-pane clearfix" id="lastApprovalUser"></div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" data-do="save">保存</button>
        </div>
    </div>
</div>
</script>
<script type="text/html" id="tmpl-dept">
<h2 class="list-heading"><a href="javascript:void(0);" data-toggle="items">{{orgName}}</a></h2>
<ul class="list-items" role="items"></ul>
</script>
<script type="text/html" id="tmpl-deptUser1">
<a href="javascript:void(0);">
    <# if(type == 'role') { #>
    <span class="item-approval-user-avatar glyphicon glyphicon-user"></span>
    <# } else { #>
        <# if(headUrl) { #>
    <img class="item-approval-user-avatar" src="{{headUrl}}" />
        <# } else { #>
    <span class="item-approval-user-avatar" style="background-color: {{$color}}">{{avatar}}</span>
        <# } #>
    <# } #>
    <span>{{userName}}</span>
</a>
</script>
<script type="text/html" id="tmpl-deptUser">
<# if(type == 'placeholder') { #>
<span class="item-approval-user-avatar glyphicon glyphicon-user"></span>
<# } else { #>
    <# if(headUrl) { #>
<img class="item-approval-user-avatar" src="{{headUrl}}" />
    <# } else { #>
<span class="item-approval-user-avatar" style="background-color: {{$color}}">{{avatar}}</span>
    <# } #>
<# } #>
<span>{{userName}}</span>
</script>
<script type="text/html" id="tmpl-approvalUser">
<# console.log(arguments); if(type == 'placeholder') { #>
<span class="item-approver-avatar glyphicon glyphicon-user"></span>
<# } else { #>
    <# if(headUrl) { #>
<img class="item-approver-avatar" src="{{headUrl}}" />
    <# } else { #>
<span class="item-approver-avatar" style="background-color: {{$color}}">{{avatar}}</span>
    <# } #>
<# } #>
<span class="item-approver-name">{{userName}}</span>
<a href="javascript:void(0);" class="item-remove" data-do="remove"><span class="glyphicon glyphicon-remove"></span></a>
<input type="hidden" name="code" value="{{code}}">
</script>

<script type="text/html" id="tmpl-lastApprovalUser">
<div class="item-approver item-last-approver">
<# if(model.headUrl) { #>
<img class="item-approver-avatar" src="{{model.headUrl}}" />
<# } else { #>
<span class="item-approver-avatar" style="background-color: {{model.$color}}">{{model.avatar}}</span>
<# } #>
<span class="item-approver-name">{{model.userName}}</span>
<a href="javascript:void(0);" class="item-remove" data-do="remove"><span class="glyphicon glyphicon-remove"></span></a>
<select name="lastDealWay">
    <option value="0"<# if(model.lastDealWay == 0) { #> selected="selected"<# } #>>送待办</option>
    <option value="1"<# if(model.lastDealWay == 1) { #> selected="selected"<# } #>>送待阅</option>
</select>
<input type="hidden" name="lastUserId" value="{{model.userId}}">
</div>
</script>

<!-- page scripts -->
<script>seajs.use('page/trial-edition/custom-form/manager', function(page){ page.run(); });</script>
<!-- end page scripts -->
</body>
</html>
