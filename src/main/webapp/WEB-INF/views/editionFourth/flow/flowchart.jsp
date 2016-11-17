<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet" href="${contextPath}/assets/dep/zTree/css/zTreeStyle/zTreeStyle.css" />
<style>
    #palette-container {
        overflow-x: hidden;
        overflow-y: auto;
    }
    .row-flowchart {
        padding: 10px 0 0 0;
    }
    .row-flowchart .col-spadmin {
        padding:  0 10px;
    }
    .palette,
    .diagram,
    .attribute-box {
        width: 100%;
        border: 1px solid #ccc;
    }
    .attribute-box {
        padding: 20px 10px;
        overflow-y: auto;
    }
    .palette-header {
        background: #ccc;
        padding: 5px;
    }

    #flowinfo {
        margin-bottom: 15px;
        border-bottom: 1px solid #e5e5e5;
    }

    .approvalList {
        padding-left: 0;
    }
    .approvalList li {
        list-style: none;
        line-height: 30px;
        height: 30px;
        background: #f2f2f2;
        padding: 0 10px;
    }
    .approvalList li:nth-child(odd) {
        background: #d7d7d7;
    }
    .approvalList li > a {
        float: right;
    }
    .approvalList-box {
        max-height: 300px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    .modal-role-select .modal-dialog {
        width: 800px;
    }
    .modal-role-select .modal-body {
        padding: 0;
    }
    .modal-role-select .tab-content > .tab-pane {
        height: 380px;
    }
    .modal-role-select .tab-content .panel-sidebar {
        height: 100%;
    }
    .modal-role-select .tab-content .panel-sidebar-body {
        height: 100%;
        overflow-x: hidden;
        overflow-y: auto;
        padding: 0 15px;
        border-right: 1px solid #e5e5e5;
    }
    ul.list-items {
        padding: 10px;
    }
    ul.list-items > li {
        list-style: none;
        float: left;
        margin: 5px;
        border: 1px solid #ccc;
        padding: 3px 6px;
        border-radius: 5px;
        color: #666;
        background: #f0f0f0;
    }
    ul.list-items > li > a {
        margin-left: 3px;
        text-decoration: none;
    }
    ul.list-items > li > a:hover {
        text-decoration: none;
    }

    .select-tree * {
        font-family: "Open Sans", Arial, "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", STHeiti, "WenQuanYi Micro Hei", SimSun, sans-serif;
    }
    .select-tree li a {
        height: auto;
    }
    .select-tree .node_name {
        display: inline-block;
        height: 30px;
        line-height: 30px;
        font-size: 14px;
    }
    .select-tree .node-item span.button.switch {
        margin-top: 6px;
        margin-right: 5px;
    }
</style>
</head>
<body>
<%@ include file="/WEB-INF/views/common/header.jsp"%>
<div class="container-fluid container-spadmin">
    <div class="row row-spadmin row-flowchart">
        <div id="palette-container" class="col-spadmin col-sm-3 col-md-2 col-lg-2">
            <div class="palette-header">普通节点</div>
            <div id="palette" class="palette"></div>
            <div class="palette-header">高级节点</div>
            <div id="adv-palette" class="palette"></div>
        </div>
        <div class="col-spadmin col-sm-6 col-md-7 col-lg-7">
            <div id="diagram" class="diagram"></div>
        </div>
        <div class="col-spadmin col-sm-3 col-md-3 col-lg-3">
            <div class="attribute-box">
                <form id="flowinfo" class="form">
                    <div class="form-group clearfix">
                        <input type="submit" class="btn btn-blue" value="保存流程图">
                        <a id="btn-exit" href="javascript:" class="btn btn-default">返回</a>
                    </div>
                </form>
                <form id="attribute-form" class="form">
                </form>
            </div>
        </div>
    </div>
</div>

<script type="text/html" id="tmpl-Task">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联用户/角色</label>
            <a href="javascript:" class="btn-selectrole pull-right" data-for="user">选择用户/角色</a>
        </div>
        <# var usertext = ''; if (user.list.length > 0) {
            usertext = user.list[0].name + (user.list.length > 1 ? ' 等': '');
        } #>
        <input type="text" name="usertext" class="form-control" value="{{usertext}}" readonly>
    </div>
    <div class="form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="groupsType" {{groupsType ? 'checked' : ''}}>
                按组分割
            </label>
        </div>
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联表单</label>
            <a href="javascript:" class="btn-selectform pull-right" data-for="form">选择表单</a>
        </div>
        <input type="text" name="formText" class="form-control" value="{{form ? form.name : ''}}" readonly>
    </div>
    <div class="form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="extendForm" {{extendForm ? 'checked' : ''}}>
                继承上一节点表单
            </label>
        </div>
        <div class="checkbox">
            <label>
                <input type="checkbox" name="extendFormEditable" {{extendFormEditable ? 'checked' : ''}}>
                上一节点表单可编辑
            </label>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label">按钮类型</label>
        <select name="buttons" class="form-control">
            <# _.each(buttonsSet, function (item) { #>
                <option value="{{item.value}}" {{buttons === item.value ? 'selected' : ''}}>{{item.text}}</option>
            <# }) #>
        </select>
    </div>
</script>
<script type="text/html" id="tmpl-MultiTask">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联用户/角色</label>
            <a href="javascript:" class="btn-selectrole pull-right" data-for="user">选择用户/角色</a>
        </div>
        <# var usertext = ''; if (user.list.length > 0) {
            usertext = user.list[0].name + (user.list.length > 1 ? ' 等': '');
        } #>
        <input type="text" name="usertext" class="form-control" value="{{usertext}}" readonly>
    </div>
    <div class="form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="groupsType" {{groupsType ? 'checked' : ''}}>
                按组分割
            </label>
        </div>
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联表单</label>
            <a href="javascript:" class="btn-selectform pull-right" data-for="form">选择表单</a>
        </div>
        <input type="text" name="formText" class="form-control" value="{{form ? form.name : ''}}" readonly>
    </div>
    <div class="form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="extendForm" {{extendForm ? 'checked' : ''}}>
                继承上一节点表单
            </label>
        </div>
        <div class="checkbox">
            <label>
                <input type="checkbox" name="extendFormEditable" {{extendFormEditable ? 'checked' : ''}}>
                上一节点表单可编辑
            </label>
        </div>
    </div>
    <div class="form-group">
        <label class="control-label">按钮类型</label>
        <select name="buttons" class="form-control">
            <# _.each(buttonsSet, function (item) { #>
                <option value="{{item.value}}" {{buttons === item.value ? 'selected' : ''}}>{{item.text}}</option>
            <# }) #>
        </select>
    </div>
    <div class="form-group">
        <label class="control-label">执行顺序</label>
        <select name="sequential" class="form-control">
            <option value="false" {{sequential === 'false' ? 'selected' : ''}}>并行</option>
            <option value="true" {{sequential === 'true' ? 'selected' : ''}}>串行</option>
        </select>
    </div>

    <div class="form-group">
        <label class="control-label">汇总节点名称</label>
        <input type="text" name="suName" class="form-control" value="{{suName}}">
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">汇总节点用户/角色</label>
            <a href="javascript:" class="btn-selectrole pull-right" data-for="suUser">选择用户/角色</a>
        </div>
        <# var suUsertext = ''; if (suUser.list.length > 0) {
            suUsertext = suUser.list[0].name + (suUser.list.length > 1 ? ' 等': '');
        } #>
        <input type="text" name="suUsertext" class="form-control" value="{{suUsertext}}" readonly>
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">汇总节点表单</label>
            <a href="javascript:" class="btn-selectform pull-right" data-for="suForm">选择表单</a>
        </div>
        <input type="text" name="suFormText" class="form-control" value="{{suForm ? suForm.name : ''}}" readonly>
    </div>
    <div class="form-group">
        <label class="control-label">汇总节点按钮类型</label>
        <select name="suButtons" class="form-control">
            <# _.each(buttonsSet, function (item) { #>
                <option value="{{item.value}}" {{suButtons === item.value ? 'selected' : ''}}>{{item.text}}</option>
            <# }) #>
        </select>
    </div>
</script>
<script type="text/html" id="tmpl-CounterSignTask">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联用户/角色</label>
            <a href="javascript:" class="btn-selectrole pull-right" data-for="user">选择用户/角色</a>
        </div>
        <# var usertext = ''; if (user.list.length > 0) {
            usertext = user.list[0].name + (user.list.length > 1 ? ' 等': '');
        } #>
        <input type="text" name="usertext" class="form-control" value="{{usertext}}" readonly>
    </div>
    <div class="form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="groupsType" {{groupsType ? 'checked' : ''}}>
                按组分割
            </label>
        </div>
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联表单</label>
            <a href="javascript:" class="btn-selectform pull-right" data-for="form">选择表单</a>
        </div>
        <input type="text" name="formText" class="form-control" value="{{form ? form.name : ''}}" readonly>
    </div>
    <div class="form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" name="extendForm" {{extendForm ? 'checked' : ''}}>
                继承上一节点表单
            </label>
        </div>
        <div class="checkbox">
            <label>
                <input type="checkbox" name="extendFormEditable" {{extendFormEditable ? 'checked' : ''}}>
                上一节点表单可编辑
            </label>
        </div>
    </div>
</script>

<script type="text/html" id="tmpl-Xor">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
    <div>
        <label class="control-label">条件类别</label>
        <select name="conditionType" class="form-control">
            <option value="0" {{conditionType == '0' ? 'selected' : ''}}>普通判断</option>
            <option value="1" {{conditionType == '1' ? 'selected' : ''}}>自选下一步</option>
        </select>
    </div>
</script>
<script type="text/html" id="tmpl-End">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
</script>

<script type="text/html" id="tmpl-Link">
    <div class="form-group">
        <label class="control-label">线路名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
    <# if (isCondition) { #>
    <div class="form-group">
        <label class="control-label">条件</label>
        <input type="text" name="condition" class="form-control" value="{{condition}}">
    </div>
    <# } #>
</script>

<script type="text/html" id="tmpl-TimeLimitedTask">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
    <div class="form-group">
        <label class="control-label">主办人员(变量)</label>
        <input type="text" name="mainUser" class="form-control" value="{{mainUser}}">
    </div>
    <div class="form-group">
        <label class="control-label">协办人员(变量)</label>
        <input type="text" name="assistUser" class="form-control" value="{{assistUser}}">
    </div>
    <div class="form-group">
        <label class="control-label">结束时间(变量)</label>
        <input type="text" name="endTime" class="form-control" value="{{endTime}}">
    </div>
    <div class="form-group">
        <label class="control-label">距离结束多少小时短信提醒(变量)</label>
        <input type="text" name="remainTime" class="form-control" value="{{remainTime}}">
    </div>

    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">关联表单</label>
            <a href="javascript:" class="btn-selectform pull-right" data-for="form">选择表单</a>
        </div>
        <input type="text" name="formText" class="form-control" value="{{form ? form.name : ''}}" readonly>
    </div>
    <div class="form-group">
        <label class="control-label">按钮类型</label>
        <select name="buttons" class="form-control">
            <# _.each(buttonsSet, function (item) { #>
                <option value="{{item.value}}" {{buttons === item.value ? 'selected' : ''}}>{{item.text}}</option>
            <# }) #>
        </select>
    </div>

    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">审核人</label>
            <a href="javascript:" class="btn-selectrole pull-right" data-for="rvUser" data-multi="false"
                data-enable="User">选择用户</a>
        </div>
        <# var rvUsertext = ''; if (rvUser.list.length > 0) {
            rvUsertext = rvUser.list[0].name + (rvUser.list.length > 1 ? ' 等': '');
        } #>
        <input type="text" name="rvUsertext" class="form-control" value="{{rvUsertext}}" readonly>
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">延期人</label>
            <a href="javascript:" class="btn-selectrole pull-right" data-for="etUser" data-multi="false"
                data-enable="User">选择用户</a>
        </div>
        <# var etUsertext = ''; if (etUser.list.length > 0) {
            etUsertext = etUser.list[0].name + (etUser.list.length > 1 ? ' 等': '');
        } #>
        <input type="text" name="etUsertext" class="form-control" value="{{etUsertext}}" readonly>
    </div>
    <div class="form-group">
        <div class="clearfix">
            <label class="control-label">延期申请表单</label>
            <a href="javascript:" class="btn-selectform pull-right" data-for="delayForm">选择表单</a>
        </div>
        <input type="text" name="delayFormText" class="form-control" value="{{delayForm ? delayForm.name : ''}}" readonly>
    </div>
</script>
<script type="text/html" id="tmpl-LoopTask">
    <div class="form-group">
        <label class="control-label">节点名称</label>
        <input type="text" name="text" class="form-control" value="{{text}}">
    </div>
</script>

<script>
    seajs.use('page/edition-fourth/flow/flowchart', function (page) {
        page.run();
    });
</script>
</body>
</html>
