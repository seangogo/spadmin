<%@ page language="java" pageEncoding="utf-8"%>
<script type="text/html" id="tmpl-roleUserListPanel">
<# var typeText = '群组'; if(model.type == 1) typeText = '角色'; #>
<div class="heading"><h2 class="title">（{{typeText}}）{{model.name}}</h2></div>
<# if(model.status == 1) { #>
<div class="btn-toolbar m-b" role="toolbar">
    <div class="btn-group" role="group">
        <# if(model.status == 1) { #>
        <a href="javascript:void(0);void(0);" class="btn btn-default" data-do="disable"><i class="fa fa-eye" aria-hidden="true"></i> 禁用</a>
        <# } else { #>
        <a href="javascript:void(0);void(0);" class="btn btn-default" data-do="enable"><i class="fa fa-eye-slash" aria-hidden="true"></i> 启用</a>
        <# } #>
    </div>
    <div class="btn-group" role="group">
        <a href="javascript:void(0);void(0);" class="btn btn-primary" data-do="edit"><i class="fa fa-pencil" aria-hidden="true"></i> 编辑</a>
        <a href="javascript:void(0);void(0);" class="btn btn-danger" data-do="delete"><i class="fa fa-minus" aria-hidden="true"></i> 删除</a>
    </div>
    <#if(model.type ==1){#>
    <div class="btn-group" role="group">
        <a href="javascript:void(0);void(0);" class="btn btn-primary" data-do="create"><i class="fa fa-plus" aria-hidden="true"></i> 新增下级角色</a>
    </div>
    <#}#>
</div>
<div class="row">
    <div class="col-md-3">
        <div class="role-user-list"></div>
    </div>
    <div class="col-md-1">
        <div class="action"><button type="button" class="btn btn-default" data-do="setUser"><i class="glyphicon glyphicon-arrow-left" aria-hidden="true"></i> </button></div>
    </div>
    <div class="col-md-5">
        <div class="user-list">
            <ul class="user-tree ztree"></ul>
        </div>
    </div>
</div>
<# } else if(model.status == 2) { #>
<div class="btn-toolbar" role="toolbar">
    <div class="btn-group" role="group">
        <# if(model.status == 1) { #>
        <a href="javascript:void(0);void(0);" class="btn btn-default" data-do="disable"><i class="fa fa-eye" aria-hidden="true"></i> 禁用</a>
        <# } else { #>
        <a href="javascript:void(0);void(0);" class="btn btn-default" data-do="enable"><i class="fa fa-eye-slash" aria-hidden="true"></i> 启用</a>
        <# } #>
    </div>
</div>
<br />
<div class="alert alert-warning">角色已被禁用，请启用后再进行操作。</div>
<# }#>
</script>

<script id="tmpl-roleUserListItem" type="text/html">
<span>{{model.name}}</span> <a href="#" data-do="remove">移除</a>
</script>
<script id="tmpl-roleGroupCreateModal" type="text/html">
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">新建角色/群组</h4>
        </div>
        <div class="modal-body">
            <form>
                <input type="hidden" name="companyId" value="{{model.companyId}}">
                <input type="hidden" name="parentId" value="{{model.parentId}}">
                <div class="form-group">
                    <label for="inputRoleName" class="control-label">名称<i class="required">*</i></label>
                    <input type="text" class="form-control" id="inputRoleName" name="roleName" placeholder="角色名称">
                </div>
                <div class="form-group">
                    <label for="inputType" class="control-label">类型<i class="required">*</i></label>
                    <select class="form-control" id="inputType" name="type">
                        <option value="0">群组</option>
                        <option value="1">角色</option>
                    </select>
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" data-do="submit">保存</button>
        </div>
    </div>
</div>
</script>
<script id="tmpl-roleCreateModal" type="text/html">
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">新建角色</h4>
        </div>
        <div class="modal-body">
            <form>
                <input type="hidden" name="companyId" value="{{model.companyId}}">
                <input type="hidden" name="parentId" value="{{model.parentId}}">
                <input type="hidden" name="type" value="{{model.type}}">
                <div class="form-group">
                    <label for="inputRoleName" class="control-label">名称<i class="required">*</i></label>
                    <input type="text" class="form-control" id="inputRoleName" name="roleName" placeholder="角色名称">
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" data-do="submit">保存</button>
        </div>
    </div>
</div>
</script>
<script id="tmpl-groupCreateModal" type="text/html">
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">新建群组</h4>
        </div>
        <div class="modal-body">
            <form>
                <input type="hidden" name="companyId" value="{{model.companyId}}">
                <input type="hidden" name="parentId" value="{{model.parentId}}">
                <input type="hidden" name="type" value="{{model.type}}">
                <div class="form-group">
                    <label for="inputRoleName" class="control-label">名称<i class="required">*</i></label>
                    <input type="text" class="form-control" id="inputRoleName" name="roleName" placeholder="群组名称">
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" data-do="submit">保存</button>
        </div>
    </div>
</div>
</script>
<script type="text/html" id="tmpl-roleGroupEditModal">
<# var typeText = '群组'; if(model.type == 1) typeText = '角色'; #>
<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">编辑{{typeText}}</h4>
        </div>
        <div class="modal-body">
            <form>
                <input type="hidden" name="id" value="{{model.id}}">
                <div class="form-group">
                    <label for="inputRoleName" class="control-label">类型<i class="required">*</i></label>
                    <p class="form-control-static">{{typeText}}</p>
                </div>
                <div class="form-group">
                    <label for="inputRoleName" class="control-label">名称<i class="required">*</i></label>
                    <input type="text" class="form-control" id="inputRoleName" name="roleName" placeholder="名称" value="{{model.name}}">
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" data-do="submit">保存</button>
        </div>
    </div>
</div>
</script>