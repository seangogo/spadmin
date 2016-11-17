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
                    <a href="javascript:" class="list-group-item active">
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
                    <a href="${contextPath}/role/permission.do" class="list-group-item">
                        权限
                    </a>
                </div>-->
            </div>
        </div>
        <div class="col-sm-9 col-md-10 col-lg-10">
            <div class="row">
                <div class="col-sm-3 col-md-2 col-lg-2">
                    <div class="panel panel-spadmin">
                        <div class="panel-heading">
                            <div class="panel-title clearfix">
                                部门
                                <a href="javascript:" class="btn btn-grey btn-add-dept pull-right"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span>新建一级部门</a>
                            </div>
                        </div>
                        <div class="panel-body" role="depts" style="overflow-x: auto;">
                            <ul id="dept-tree" class="dept-tree ztree"></ul>
                            <div class="right-menu">
                                <ul>
                                    <li data-do="addNode">添加子部门</li>
                                    <li data-do="editNode">编辑</li>
                                    <li data-do="removeNode">删除</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-sm-9 col-md-10 col-lg-10">
                    <div class="panel panel-spadmin panel-spadmin-content">
                        <div class="panel-heading">人员列表</div>
                        <div class="panel-body">
                            <div class="pull-right" role="group" aria-label="...">
                                <a href="javascript:" class="btn btn-info btn-check"><span class="glyphicon glyphicon-inbox" aria-hidden="true"></span>同步人员信息</a>
                                <a href="javascript:" class="btn btn-blue btn-add"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span>添加新人员</a>
                                <a href="javascript·:" class="btn btn-green btn-import"><span class="glyphicon glyphicon-floppy-open" aria-hidden="true"></span>导入人员</a>
                                <a href="${contextPath}/assets/files/orgUserExcel.xlsx" class="btn btn-grey">下载模板</a>
                            </div>
                            <form class="form-filter form-filter-top form-inline"></form>
                            <table id="table-employee" class="table-employee table table-spadmin">
                                <thead>
                                    <tr>
                                        <th>姓名</th>
                                        <th>职务</th>
                                        <th>手机号码</th>
                                        <th>工号</th>
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

<script id="tmpl-employeeRow" type="text/html">
    <td>{{userName}}</td>
    <td>{{post}}</td>
    <td>{{mobile}}</td>
    <td>{{workNumber}}</td>
    <td class="col-actions">
        <div><a href="javascript:void(0);" data-do="edit">编辑</a></div>
        <div><a class="act-red" href="javascript:void(0);" data-do="delete">删除</a></div>
        <div><a class="act-grey" href="javascript:void(0);" data-do="move">更换部门</a></div>
        <!--<a href="javascript:void(0);" data-do="disable">禁用</a>-->
    </td>
</script>
<script id="tmpl-departEditModal" type="text/html">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>
                </button>
                <h4 class="modal-title">{{ edit ? '编辑' : '添加'}}部门</h4>
            </div>
            <div class="modal-body">
                <div class="approval-setting-body row">
                    <div class="panel-main col-sm-12 col-md-12 col-lg-12">
                        <form role="edit-depart">
                            <input type="hidden" name="orgId" value="{{orgId}}">
                            <input type="hidden" name="previousId" value="{{previousId}}">
                            <div class="form-group">
                                <label class="control-label" for="orgName">部门名称</label>
                                <input name="orgName" class="form-control" value="{{orgName}}">
                            </div>
                            <div class="form-group">
                                <label class="control-label" for="showindex">排序序号</label>
                                <input name="showindex" class="form-control" value="{{showindex}}">
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
<script id="tmpl-departMoveModal" type="text/html">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>
                </button>
                <h4 class="modal-title">选择要更换的部门</h4>
            </div>
            <div class="modal-body">
                <div class="dept-move-body row">
                    <div class="panel-main col-sm-12 col-md-12 col-lg-12">
                        <ul id="move-tree" class="ztree"></ul>
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
<script id="tmpl-employeeEditModal" type="text/html">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>
                </button>
                <h4 class="modal-title">{{ edit ? '编辑' : '添加' }}人员</h4>
            </div>
            <div class="modal-body">
                <div class="approval-setting-body row">
                    <div class="panel-main col-sm-12 col-md-12 col-lg-12">
                        <form role="edit-employee">
                            <input type="hidden" name="userId" value="{{userId}}">
                            <input type="hidden" name="orgId" value="{{orgId}}">
                            <# if (!edit) { #>
                            <div class="form-group">
                                <label class="control-label" for="mobile">手机</label>
                                <input name="mobile" class="form-control" value="{{mobile}}">
                            </div>
                            <# } #>
                            <div class="form-group">
                                <label class="control-label" for="userName">姓名</label>
                                <input name="userName" class="form-control" value="{{userName}}">
                            </div>
                            <div class="form-group">
                                <label class="control-label" for="post">职务</label>
                                <input name="post" class="form-control" value="{{post}}">
                            </div>
                            <div class="form-group">
                                <label class="control-label" for="workNumber">工号</label>
                                <input name="workNumber" class="form-control" value="{{workNumber}}">
                            </div>
                            <div class="form-group">
                                <label class="control-label" for="showindex">排序序号</label>
                                <input name="showindex" class="form-control" value="{{showindex}}">
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

<!-- page scripts -->
<script>
    seajs.use('page/general/employee/manager', function(module) {
        module.run();
    });
</script>
<!-- end page scripts -->
</body>
</html>
