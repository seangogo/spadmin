<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet"
	href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
<link rel="stylesheet"
	href="${contextPath}/assets/src/css/modal-approval-setting.css" />
<link rel="stylesheet"
	href="${contextPath}/assets/src/css/custom-form-manager.css" />
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
						<a id="btn-back" href="javascript:" class="btn btn-blue"> 返回 </a>
					</div>
					<div class="list-group">
						<a href="javascript:" class="list-group-item active"> 任务列表 </a>
					</div>
				</div>
			</div>
			<div class="col-spadmin col-sm-9 col-md-10 col-lg-10">
				<div class="panel panel-spadmin panel-spadmin-content">
					<div class="panel-heading">任务列表</div>
					<div class="panel-body">
						<div class="btn-group pull-right" role="group" aria-label="...">
							<a id="add-task" href="javascript:" class="btn btn-blue"> <span
								class="glyphicon glyphicon-plus" aria-hidden="true"></span>添加任务
							</a> <a
								href="${contextPath}/microApp/customForm/createApprovalImprot.do?typeId=${approvalTypeId}"
								class="btn btn-green"> 数据模板下载 </a> <a
								href="${contextPath}/assets/files/batchStartUserInfoModel.xlsx"
								class="btn btn-green"> 人员模板下载 </a>
						</div>
						<form class="form-filter form-filter-top form-inline">
							<div class="form-group"></div>
						</form>
						<table id="table-task" class="table-spadmin table">
							<thead>
								<tr>
									<th>序号</th>
									<th>任务名称</th>
									<th>状态</th>
									<th>初始化人数</th>
									<th>导入人数</th>
									<th>实际生成人数</th>
									<th>已处理人数</th>
									<th>已完成人数</th>
									<th>撤销人数</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody role="items">
							</tbody>
						</table>
						<button id="user-import-trigger" style="display: none">Import</button>
						<button id="data-import-trigger" style="display: none">Import</button>
						<button id="undo-trigger" style="display: none">Undo</button>
					</div>
					<div class="panel-footer clearfix"></div>
				</div>
			</div>
		</div>
	</div>

<script type="text/html" id="tmpl-taskItem">
    <# var statusText = ''; switch (status) {
            case 0: statusText = '处理中'; break;
            case 1: statusText = '已完成'; break;
            default: statusText = '处理中'; break; } #>
    <td class="col-index">{{$index + 1}}</td>
    <td class="col-form">{{taskName}}</td>
    <td>{{statusText}}</td>
    <td>{{initdtUsers}}</td>
    <td>{{tocreatetaskUsers}}</td>
    <td>{{createdtaskUsers}}</td>
    <td>{{donetaskUsers}}</td>
    <td>{{completetaskUsers}}</td>
    <td>{{undotaskUsers}}</td>
    <td class="col-actions">
        <# if (status == 0) { #>
            <div><a href="javascript:void(0);" data-do="edit">编辑</a></div>
            <div><a class="act-red" href="javascript:void(0);" data-do="remove">删除</a></div>
            <div><a href="javascript:void(0);" data-do="publish">发布</a></div>
            <div><a href="javascript:void(0);" data-do="import-user">人员导入</a></div>
            <div><a href="javascript:void(0);" data-do="import-data">数据导入</a></div>
        <# } else if (status == 1) { #>
            <div><a href="javascript:void(0);" data-do="undo">撤销</a></div>
            <div><a href="javascript:void(0);" data-do="recovery">回收</a></div>
        <# }#>
		<div><a href="${contextPath}/task/export.do?taskId={{id}}" >导出报表</a></div>
    </td>
</script>

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

<script type="text/html" id="tmpl-taskModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>
                </button>
                <h4 class="modal-title">{{ edit ? '编辑' : '添加'}}任务</h4>
            </div>
            <div class="modal-body">
                <div class="approval-setting-body row">
                    <div class="panel-main col-sm-12 col-md-12 col-lg-12">
                        <form role="edit-task">
                            <input type="hidden" name="id" value="{{id}}">
                            <input type="hidden" name="approvalTypeId" value="{{approvalTypeId}}">
                            <div class="form-group">
                                <label class="control-label" for="taskName">任务名称</label>
                                <input name="taskName" class="form-control" value="{{taskName}}">
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
                            <ul id="batch-dept-tree" class="dept-tree ztree"></ul>
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
                <!--<button type="button" class="btn btn-primary" data-do="prev">上一步</button>
                <button type="button" class="btn btn-primary" data-do="next">下一步</button>-->
                <button type="button" class="btn btn-primary" data-do="save">提交</button>
            </div>
        </div>
    </div>
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
	<script type="text/html" id="tmpl-dept">
    <h2 class="list-heading"><a href="javascript:void(0);" data-toggle="items">{{orgName}}</a></h2>
    <ul class="list-items" role="items"></ul>
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

	<!-- page scripts -->
	<script>
		seajs.use('page/general/micro-app/task/manager', function(page) {
			page.run();
		});
	</script>
	<!-- end page scripts -->
</body>
</html>
