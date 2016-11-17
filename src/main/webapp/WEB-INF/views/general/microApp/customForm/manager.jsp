<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
<link rel="stylesheet" href="${contextPath}/assets/src/css/modal-approval-setting.css" />
<link rel="stylesheet" href="${contextPath}/assets/src/css/custom-form-manager.css" />
<link rel="stylesheet"
    href="${contextPath}/assets/dep/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />
</head>
<body>
<!-- header -->
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<!-- end header -->
<div class="container-fluid container-spadmin">
    <div class="row row-spadmin">
        <div class="col-spadmin col-sm-3 col-md-2 col-lg-2">
            <div class="panel panel-spadmin menu-spadmin">
                <div class="panel-heading">产品</div>
                <div class="list-group">

                </div>
            </div>
        </div>
        <div class="col-spadmin col-sm-9 col-md-10 col-lg-10">
            <div class="panel panel-spadmin panel-spadmin-content">
                <div class="panel-heading">审批列表</div>
                <div class="panel-body">
                    <div class="btn-group pull-right" role="group" aria-label="...">
                        <a id="input-url" href="javascript:" class="btn btn-blue">
                            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>创建新审批
                        </a>
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
                    <table id="table-approval" class="table-spadmin table">
                        <thead>
                            <tr>
                                <th class="col-checkbox">
                                    <label class="checkbox">
                                        <input type="checkbox" role="checkall">
                                    </label>
                                </th>
                                <th>序号</th>
                                <th>审批名称</th>
                                <th>说明</th>
                                <th>状态</th>
                                <th>操作</th>
                                <!--<th>其他</th>-->
                            </tr>
                        </thead>
                        <tbody role="items">
                            <script type="text/html" id="tmpl-approvalItem">
                                <td class="col-checkbox">
                                    <label class="checkbox {{checked ? 'checked' : ''}}">
                                        <input type="checkbox" data-do="check" {{checked ? 'checked' : ''}}>
                                    </label>
                                </td>
                                <td class="col-index">{{$index + 1}}</td>
                                <td class="col-form">{{name}}</td>
                                <td>{{des}}</td>
                                <td class="col-status">
                                    <div class="status-tip">
                                        <span class="{{ status == 1 ? 'bg-green' : 'bg-red' }}">
                                            {{ status == 1 ? '启用' : '停用' }}
                                        </span>
                                    </div>
                                    <div class="status-tip">
                                        <span class="{{ (isDefault > 0 && isDefault < 1000) ? 'bg-blue' : 'bg-orange' }}">
                                            {{ (isDefault > 0 && isDefault < 1000) ? '默认' : '非默认' }}
                                        </span>
                                    </div>
                                </td>
                                <td class="col-actions">
                                    <# if(isBoutique != null && isBoutique == 1) { #>
                                        <div><a href="{{thirdConfigLink}}" target="_blank">配置第三方流程</a></div>
                                    <# } else { #>
                                        <# if(status == 1) { #>
                                            <div><a class="act-red" href="javascript:void(0);" data-do="disable">停用</a></div>
                                        <# } else if(status == 2) { #>
                                            <div><a class="act-green" href="javascript:void(0);" data-do="enable">启用</a></div>
                                        <# } #>
                                        <# if(isDefault > 0 && isDefault < 1000) { #>
                                            <div><a class="act-orange" href="javascript:void(0);" data-do="defaultOn">取消默认</a></div>
                                        <# } else { #>
                                            <div><a href="javascript:void(0);" data-do="defaultOff">设为默认</a></div>
                                        <# } #>
                                        <# if (scene == 3 || scene == 4) {#>
                                            <div><a href="javascript:void(0);" data-do="manageFlow">编辑</a></div>
                                            <div><a href="javascript:void(0);" data-do="deploy">部署</a></div>
                                            <div><a class="act-grey" href="javascript:void(0);" data-do="export">报表</a></div>
                                        <# } else { #>
                                            <div><a href="${contextPath}/microApp/customForm/input-e{{scene}}.do?id={{id}}&wyyId={{wyyId}}">编辑</a></div>
                                            <div><a href="javascript:void(0);" data-do="setApprovalUser">审批设置</a></div>
                                            <div><a href="${contextPath}/microApp/task/manager.do?approvalTypeId={{id}}&wyyId={{wyyId}}">批量任务</a></div>

                                            <!--<div><a href="javascript:void(0);" data-do="batch">批量发起</a></div>
                                            <div><a class="act-grey" href="javascript:void(0);" data-do="revoke">批量撤销</a></div>-->
                                            <div><a class="act-grey" href="javascript:void(0);" data-do="export">报表</a></div>
                                        <# } #>
                                    <# } #>
                                </td>
                                <!--
                                <td class="col-more">
                                    <# if (scene == 2) { #>
                                        <div><a class="act-grey" href="${contextPath}/microApp/customForm/createApprovalImprot.do?typeId={{id}}">模板下载</a></div>
                                        <div><a class="act-grey" href="javascript:void(0);" data-do="import">导入</a></div>
                                    <# } #>
                                </td>
                                -->
                            </script>
                        </tbody>
                    </table>
                    <button id="import-trigger" style="display:none">Import</button>
                </div>
                <div class="panel-footer clearfix">
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/html" id="tmpl-pagination">
    <div class="pageinfo">共计：{{total}}条 ,每页：{{pageSize}}条</div>
    <div class="page-box">
        <ul class="pagination clearfix">
            <li class="{{pageNum <= 1 ? 'disabled' : ''}}">
                <a href="javascript:" data-do="first">首页</a>
            </li>
            <li class="{{pageNum <= 1 ? 'disabled' : ''}}">
                <a href="javascript:" data-do="prev">上一页</a>
            </li>
            <# _.each(pagelist, function (page, index) { #>
                <# if (index > 0 && (page - pagelist[index - 1]) > 1) { #>
                    <li><span>...</span></li>
                <# } #>
                <li class="{{page === pageNum ? 'active' : ''}}">
                    <a href="javascript:" aria-label="第{{page}}页" data-do="pagechange">{{page}}</a>
                </li>
            <# }) #>
            <li class="{{pageNum >= pages ? 'disabled' : ''}}">
                <a href="javascript:" data-do="next">下一页</a>
            </li>
            <li class="{{pageNum >= pages ? 'disabled' : ''}}">
                <a href="javascript:" data-do="last">末页</a>
            </li>
        </ul>
    </div>
</script>

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
    <# if(type == 'placeholder') { #>
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
    <# if(model.userId == '$$!') { #>
    <span class="item-approver-avatar glyphicon glyphicon-user"></span>
    <# } else if(model.headUrl) { #>
    <img class="item-approver-avatar" src="{{model.headUrl}}" />
    <# } else { #>
    <span class="item-approver-avatar" style="background-color: {{model.$color}}">{{model.avatar}}</span>
    <# } #>
    <span class="item-approver-name">{{model.userName}}</span>
    <a href="javascript:void(0);" class="item-remove" data-do="remove"><span class="glyphicon glyphicon-remove"></span></a>
    <# if (typeof noDealWay === 'undefined' || noDealWay !== true) { #>
    <select name="lastDealWay">
        <option value="0"<# if(model.lastDealWay == 0) { #> selected="selected"<# } #>>送待办</option>
        <option value="1"<# if(model.lastDealWay == 1) { #> selected="selected"<# } #>>送待阅</option>
    </select>
    <# } #>
    <input type="hidden" name="lastUserId" value="{{model.userId}}">
    </div>
</script>

<script type="text/html" id="tmpl-startUser">
    <div class="item-approver item-start-approver">
    <# if(model.type == 'placeholder') { #>
    <span class="item-approver-avatar glyphicon glyphicon-user"></span>
    <# } else { #>
        <# if(model.headUrl) { #>
        <img class="item-approver-avatar" src="{{model.headUrl}}" />
        <# } else { #>
        <span class="item-approver-avatar" style="background-color: {{model.$color}}">{{model.avatar}}</span>
        <# } #>
    <# } #>
    <span class="item-approver-name">{{model.userName}}</span>
    <input type="hidden" name="startUserId" value="{{model.userId}}">
    </div>
</script>

<script type="text/html" id="tmpl-batchApprovalModal">
    <%-- modal-approval-setting --%>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span></button>
                <h4 class="modal-title">批量发起流程</h4>
            </div>
            <div class="modal-body">
                <div class="batch-approval-body">
                    <div class="batch-main center-block">
                        <div>
                            <p class="text-muted">请先导入要批量发起的人员</p>
                        </div>
                        <button type="button" class="btn btn-primary" data-do="import">导入人员</button>
                        <input class="batch-ids" type="hidden" value="">
                        <div>
                            <a href="${contextPath}/assets/files/batchStartUserInfoModel.xlsx">下载模版</a>
                        </div>
                        <div>
                            <p class="text-muted batch-info"></p>
                        </div>
                    </div>
                </div>
                <div class="approval-setting-body row">
                    <div class="panel-sidebar col-sm-4 col-md-4 col-lg-4">
                        <div class="panel-sidebar-body" role="depts">
                            <ul class="dept-tree ztree"></ul>
                        </div>
                    </div>
                    <div class="panel-main col-sm-8 col-md-8 col-lg-8">
                        <form class="approver-selection clearfix" role="approval-users">
                            <ul class="nav nav-tabs" role="tablist">
                                <li role="presentation" class="active"><a href="#startUser" role="tab" data-toggle="tab">发起人</a></li>
                                <!--<li role="presentation"><a href="#defApprovalUser" role="tab" data-toggle="tab">待办接收人</a></li>
                                <li role="presentation"><a href="#lastApprovalUser" role="tab" data-toggle="tab">归口办理人</a></li>-->
                            </ul>
                            <div class="tab-content">
                                <div role="tabpanel" class="tab-pane clearfix active" id="startUser"></div>
                                <div role="tabpanel" class="tab-pane clearfix" id="defApprovalUser"></div>
                                <div role="tabpanel" class="tab-pane clearfix" id="lastApprovalUser"></div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" data-do="prev">上一步</button>
                <button type="button" class="btn btn-primary" data-do="next">下一步</button>
                <button type="button" class="btn btn-primary" data-do="save">提交</button>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="tmpl-createDialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>
                </button>
                <h4 class="modal-title">创建新审批</h4>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label class="control-label" for="inputApprovalType">场景</label>
                    <select name="approvalType" id="version-id" class="form-control">
                        <# _.each(versions, function (item, index) { #>
                            <option value="{{item.id}}" {{index === 0 ? 'selected' : ''}}>{{item.name}}</option>
                        <# }) #>
                    </select>
                </div>
                <div class="form-group">
                    <label class="control-label">说明</label>
                    <div id="version-note" class="form-control">
                        {{versions[0].note}}
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" data-do="ok">确定</button>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="tmpl-exportReport">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>
                </button>
                <h4 class="modal-title">数据导出</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal"
                    <# if (scene === 3) { #>
                        action="${contextPath}/workflow/export.do"
                    <# } else { #>
                        action="${contextPath}/export/exportExcel.do"
                    <# } #>
                    method="post">
                    <input type="hidden" name="{{ scene === 3 ? 'typeId' : 'id' }}" value="{{id}}">
                    <div class="form-group">
                        <label class="col-sm-2 control-label" for="status">请选择类型状态</label>
                        <div class="col-sm-8">
                            <select name="{{ scene === 3 ? 'processStatus' : 'status' }}" class="form-control">
                                <option value="">全部</option>
                                <# if (scene === 3) { #>
                                    <option value="0">起草状态</option>
                                    <option value="1">流转状态</option>
                                    <option value="2">完成状态</option>
                                    <option value="3">拒绝状态</option>
                                    <option value="9">起草人撤销</option>
                                <# } else { #>
                                <c:forEach items="${status}" var="item">
                                    <option value="${item.status}">${item.name}</option>
                                </c:forEach>
                                <# } #>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputFlowId" class="col-sm-2 control-label">审批编号</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" name="flowId"
                                id="inputFlowId">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">发起时间</label>
                        <div class="col-sm-4">
                            <input type="text" class="form-control" id="applyStartTimePicker">
                            <input type="hidden" name="applyStartTime" id="applyStartTime">
                        </div>
                        <div class="col-sm-4">
                            <input type="text" class="form-control" id="applyEndTimePicker">
                            <input type="hidden" name="applyEndTime" id="applyEndTime">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-sm-10 col-sm-offset-2">
                            <button type="submit" class="btn btn-blue">导出</button>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="tmpl-leftMenu">
    <a href="{{ wyyId === 'wyy0001' ? 'javascript:' : '${contextPath}/microApp/customForm/manager.do?wyyId=wyy0001' }}"
        class="list-group-item {{ wyyId === 'wyy0001' ? 'active' : '' }}">
        移动审批
    </a>
    <# _.each(list, function (item) { #>
        <a href="{{ wyyId === item.wyyId ? 'javascript:' : '${contextPath}/microApp/customForm/manager.do?wyyId=' + item.wyyId }}"
            class="list-group-item {{ wyyId === item.wyyId ? 'active' : '' }}">
            {{item.name}}
        </a>
    <# }) #>
</script>

<!-- page scripts -->
<script>seajs.use('page/general/micro-app/custom-form/manager', function(page){ page.run(); });</script>
<!-- end page scripts -->
</body>
</html>
