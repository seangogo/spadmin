<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link href="${contextPath}/assets/src/css/custom-form-pc.css" rel="stylesheet" type="text/css">
<link href="${contextPath}/assets/src/css/iconfont/iconfont.css" rel="stylesheet" type="text/css">
</head>
<body class="version3">
<div class="design-wrap">
    <div class="design-header">
        <div class="head-title" >
            <a href="javascript:">
                <img class="design-header-logo" src="${contextPath}/assets/src/css/img/logo.png" alt="" >
                <span>移动审批后台管理</span>
            </a>
            <span class="design-header-note">PC端表单设计器</span>
        </div>
        <div class="head-actions" >
            <button class="design-button design-button-cyan button-save">保存</button>
            <button class="design-button design-button-cyan button-exit">退出</button>
        </div>
    </div>
    <div class="design-main">
        <div class="design-formcanvas">
            <div class="design-formcanvas-inner">
                <div class="design-formcanvas-body">
                </div>
            </div>
        </div>
        <div class="design-panel design-settingpanel">
            <div class="design-panel-tab">
                <a data-tabname="widget" class="tabitem tab-widget">控件设置</a>
                <a data-tabname="form" class="tabitem tab-form current">表单设置</a>
                <a data-tabname="list" class="tabitem tab-list">控件列表</a>
            </div>
            <div class="design-form design-widgetsettings" style="display: none;">
            </div>
            <div class="design-form design-formsettings">
                <form class="formsetting-form">
                    <div class="design-field design-setting-title">
                        <div class="fieldname">
                            <span>表单名称</span>
                            <span class="fieldinfo">最多10个字</span>
                        </div>
                        <div class="fieldblock">
                            <input type="text" name="name" value="" maxlength="10">
                        </div>
                    </div>
                </form>
            </div>
            <div class="design-form design-listsettings" style="display: none;">
            </div>
        </div>
    </div>
</div>

<!-- 图标 & 控件 列表 -->
<script id="tmpl-icons" type="text/html">
    <# _.each(icons, function(item, index){ #>
        <label class="iconitem {{index === 0 ? 'selected' : ''}}" data-id="{{item}}">
            <i class="icon icon-checked"></i>
            <img src="${contextPath}/assets/src/css/img/formicons/{{item}}.png" alt="{{item}}">
        </label>
    <# }) #>
</script>
<script id="tmpl-widgets" type="text/html">
    <# _.each(widgets, function (item) { if (item.disabled) return; #>
        <div class="design-widgetitem" data-type="{{item.id}}">
            <label>{{item.name}}</label>
            <i class="widgeticon {{item.id.toLowerCase()}}"></i>
        </div>
    <# }) #>
</script>
<!-- 控件 -->
<script id="tmpl-widget-normal" type="text/html">
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview">
        <div class="design-componentview-border">
            <label class="design-componentview-label">
                {{label}}{{unit.length > 0 ? '（' + unit + '）' : ''}}
            </label>
            <# if (typeof icon !== 'undefined') { #>
                <i class="icon icon-{{icon}}"></i>
            <# } #>
            <span class="design-componentview-placeholder">
                {{placeholder}}{{required ? '（必填）' : ''}}&nbsp;
            </span>
        </div>
    </div>
</script>
<script id="tmpl-widget-picturenote" type="text/html">
    <# var phStrs = placeholder.split('\r\n') #>
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview" >
        <div class="design-componentview-content" >
            <# _.each(phStrs, function (str) { #>
                <p>{{str}}</p>
            <# }); #>
            <div>
            <# if (typeof imginfo !== 'undefined' && imginfo.length > 0) {
                var imgObj = JSON.parse(imginfo); #>
                <img src="${contextPath}/file/download/{{imgObj.id}}/thum_{{imgObj.addr}}.do" alt="" style="max-height: 80px; max-width: 100%;">
            <# } #>
            </div>
        </div>
    </div>
</script>
<script id="tmpl-widget-datarange" type="text/html">
    <div class="design-remove icon icon-close" ></div>
    <div class="design-componentview" >
        <div class="design-componentview-border" >
            <label class="design-componentview-label">{{label[0]}}</label>
            <i class="icon icon-enter"></i>
            <span class="design-componentview-placeholder">{{placeholder}}{{required ? '（必填）' : ''}}</span>
        </div>
        <div class="design-componentview-border" >
            <label class="design-componentview-label">{{label[1]}}</label>
            <i class="icon icon-enter"></i>
            <span class="design-componentview-placeholder">{{placeholder}}{{required ? '（必填）' : ''}}</span>
        </div>
    </div>
</script>
<script id="tmpl-widget-textnote" type="text/html">
    <# var phStrs = placeholder.split('\r\n') #>
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview" >
        <div class="design-componentview-content" >
            <# _.each(phStrs, function (str) { #>
                <p>{{str}}</p>
            <# }); #>
        </div>
    </div>
</script>
<script id="tmpl-widget-valid" type="text/html">
    <# var phStrs = placeholder.split('\r\n') #>
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview" >
        <div class="design-componentview-border">
            <# _.each(phStrs, function (str) { #>
                <p>{{str}}</p>
            <# }); #>
        </div>
    </div>
</script>
<script id="tmpl-widget-tablefield" type="text/html">
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview" >
        <label class="design-componentview-label">{{label}}</label>
        <div class="design-componentview-area">
            <div class="design-componentgroup clearfix">
            </div>
        </div>
        <div class="design-componentview-adddetail">{{placeholder}}</div>
    </div>
</script>
<script id="tmpl-widget-tabpage" type="text/html">
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview" >
        <label class="design-componentview-label">{{label}}</label>
        <div class="design-componentview-area">
            <div class="design-componentgroup clearfix">
            </div>
        </div>
    </div>
</script>
<script id="tmpl-widget-button" type="text/html">
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview" >
        <div class="design-componentview-button">
            <span>{{label}}</span>
        </div>
    </div>
</script>
<script id="tmpl-widget-rteditor" type="text/html">
    <div class="design-remove icon icon-close"></div>
    <div class="design-componentview">
        <div class="design-componentview-border">
            <label class="design-componentview-label">
                {{label}}
            </label>
            <span class="design-componentview-placeholder">
                {{placeholder}}{{required ? '（必填）' : ''}}&nbsp;
            </span>
        </div>
    </div>
</script>
<!-- 控件配置项 -->
<script id="tmpl-setting-label" type="text/html">
    <# var _templabel = label;
    if (!(_templabel instanceof Array)) {
        _templabel = [_templabel];
    }
    _.each(_templabel, function (item, index) {
    #>
    <div class="design-field design-setting-label">
        <div class="fieldname">
            <span>标题{{_templabel.length > 1 ? (index + 1).toString() : ''}}</span>
            <span class="fieldinfo">最多10个字</span>
        </div>
        <div class="fieldblock">
            <input name="label" type="text" value="{{_templabel[index]}}" maxlength="10">
        </div>
    </div>
    <# }) #>
</script>
<script id="tmpl-setting-placeholder" type="text/html">
    <div class="design-field design-setting-placeholder">
        <div class="fieldname">
            <span>提示文字</span>
            <span class="fieldinfo">最多20个字</span>
        </div>
        <div class="fieldblock">
            <input name="placeholder" type="text" value="{{placeholder}}" maxlength="20">
        </div>
    </div>
</script>
<script id="tmpl-setting-unit" type="text/html">
    <div class="design-field design-setting-placeholder">
        <div class="fieldname">
            <span>单位</span>
            <span class="fieldinfo">最多10个字</span>
        </div>
        <div class="fieldblock">
            <input name="unit" type="text" value="{{unit}}" maxlength="10">
        </div>
    </div>
</script>
<script id="tmpl-setting-integer" type="text/html">
    <div class="design-field design-setting-required">
        <label class="fieldblock">
            <input name="isInteger" type="checkbox" {{ isInteger ? 'checked' : '' }}>
            <span class="verticalmiddle">只能输入整数</span>
        </label>
    </div>
</script>
<script id="tmpl-setting-tosum" type="text/html">
    <# if (inTablefield === true) { #>
    <div class="design-field design-setting-required">
        <label class="fieldblock">
            <input name="toSum" type="checkbox" {{ toSum ? 'checked' : '' }}>
            <span class="verticalmiddle">计算合计值</span>
        </label>
    </div>
    <# } #>
</script>
<script id="tmpl-setting-required" type="text/html">
    <div class="design-field design-setting-required">
        <label class="fieldblock">
            <input name="required" type="checkbox" {{ required ? 'checked' : '' }}>
            <span class="verticalmiddle">必填</span>
        </label>
    </div>
</script>
<script id="tmpl-setting-canimport" type="text/html">
    <div class="design-field design-setting-required">
        <label class="fieldblock">
            <input name="canImport" type="checkbox" {{ canImport ? 'checked' : '' }}>
            <span class="verticalmiddle">可以导入</span>
        </label>
    </div>
</script>
<script id="tmpl-setting-readonly" type="text/html">
    <div class="design-field design-setting-required">
        <label class="fieldblock">
            <input name="readonly" type="checkbox" {{ readonly ? 'checked' : '' }}>
            <span class="verticalmiddle">不可编辑</span>
        </label>
    </div>
</script>
<script id="tmpl-setting-textnote" type="text/html">
    <div class="design-field design-setting-content">
        <div class="fieldname">
            <span>说明文字</span>
            <span class="fieldinfo">最多200个字</span>
        </div>
        <div class="fieldblock">
            <textarea name="placeholder" type="text" maxlength="200">{{placeholder}}</textarea>
        </div>
    </div>
</script>
<script id="tmpl-setting-actionname" type="text/html">
    <div class="design-field design-setting-placeholder">
        <div class="fieldname">
            <span>动作名称</span>
            <span class="fieldinfo">最多10个字</span>
        </div>
        <div class="fieldblock">
            <input name="placeholder" type="text" value="{{placeholder}}" maxlength="10">
        </div>
    </div>
</script>
<script id="tmpl-setting-options" type="text/html">
    <div class="design-field design-setting-options" >
        <div class="fieldname" >
            <span >选项</span>
            <span class="fieldinfo" >最多50项，每项最多20个字</span>
        </div>
        <div class="selection-list">
            <# _.each(selects, function (item) { #>
                <div class="fieldblock design-setting-option" >
                    <input name="selects" type="text" maxlength="20" value="{{item}}" >
                    <a class="action action-del" data-action="del"><i class="icon icon-minus"></i></a>
                    <a class="action action-add" data-action="add"><i class="icon icon-plus"></i></a>
                </div>
            <# }) #>
            <# if (selects.length === 0) { #>
                <div class="fieldblock design-setting-option" >
                    <a class="action action-add empty-plus" data-action="add"><i class="icon icon-plus"></i></a>
                </div>
            <# } #>
        </div>
    </div>
</script>
<script id="tmpl-setting-picture" type="text/html">
    <div class="design-field design-setting-content">
        <div class="fieldname">
            <span>图片</span>
        </div>
        <div class="fieldblock">
            <a href="javascript:">上传图片</a>
            <input type="hidden" name="imginfo" value="{{imginfo}}">
        </div>
    </div>
</script>
<script id="tmpl-setting-linkageoptions" type="text/html">
    <# var l_options = JSON.parse(linkage); #>
    <div class="design-field design-setting-options" >
        <div class="fieldname" >
            <span >选项</span>
            <span class="fieldinfo">最多20项，每项最多20个字</span>
        </div>
        <label class="fieldblock">
            <input name="mustnumber" type="checkbox" {{ mustnumber ? 'checked' : '' }}>
            <span class="verticalmiddle">限制子类为数字</span>
        </label>
        <div class="selection-list">
            <# _.each(l_options.selects, function (item) { #>
                <div class="fieldblock design-setting-option" >
                    <input type="text" maxlength="20" value="{{item.value}}" >
                    <a class="action action-del" data-action="del"><i class="icon icon-minus"></i></a>
                    <a class="action action-add" data-action="add"><i class="icon icon-plus"></i></a>
                    <div class="children-list">
                        <# _.each(item.children, function (child) { #>
                        <div class="fieldblock design-setting-option linkage-children">
                            <input type="text" maxlength="20" value="{{child.value}}">
                            <a class="action action-del" data-action="del"><i class="icon icon-minus"></i></a>
                            <a class="action action-add" data-action="add"><i class="icon icon-plus"></i></a>
                        </div>
                        <# }) #>
                    </div>
                </div>
            <# }) #>
        </div>
    </div>
</script>
<script id="tmpl-setting-compute" type="text/html">
    <div class="design-field design-setting-options" >
        <div class="fieldname" >
            <span>涉及控件</span>
            <a class="checkwidgets" href="javascript:">选择控件</a>
        </div>
        <table class="compute-widgets">
            <thead>
                <tr>
                    <th>变量名</th>
                    <th>对应控件</th>
                    <th>操作</th>
                </tr>
            </tehad>
            <tbody>
            </tbody>
        </table>
        <div class="fieldname">
            <span>表达式</span>
            <span class="fieldinfo">只能使用控件所对应变量名</span>
        </div>
        <div class="fieldblock">
            <input name="expression" type="text" value="{{expression}}">
        </div>
    </div>
</script>
<script id="tmpl-setting-alert" type="text/html">
    <div class="design-field design-setting-placeholder">
        <div class="fieldname">
            <span>验证未通过时的提示信息</span>
            <span class="fieldinfo">最多100个字</span>
        </div>
        <div class="fieldblock">
            <textarea name="placeholder" type="text" maxlength="100">{{placeholder}}</textarea>
        </div>
    </div>
</script>
<script id="tmpl-setting-alias" type="text/html">
    <div class="design-field design-setting-placeholder">
        <label class="fieldblock">
            <input name="useAlias" type="checkbox" {{ useAlias ? 'checked' : '' }}>
            <span class="verticalmiddle">变量名</span>
        </label>
        <div class="fieldblock">
            <input name="alias" type="text" value="{{alias}}" {{useAlias ? '' : 'disabled'}}>
        </div>
    </div>
</script>
<script id="tmpl-setting-event" type="text/html">
    <div class="design-field design-setting-placeholder">
        <div class="fieldname">
            <span>触发事件</span>
            <span class="fieldinfo">请填写已存在的事件参数</span>
        </div>
        <div class="fieldblock">
            <input name="placeholder" type="text" value="{{placeholder}}">
        </div>
    </div>
</script>
<script id="tmpl-setting-multi" type="text/html">
    <div class="design-field design-setting-required">
        <label class="fieldblock">
            <input name="multi" type="checkbox" {{ multi ? 'checked' : '' }}>
            <span class="verticalmiddle">多选</span>
        </label>
    </div>
</script>
<script id="tmpl-setting-timeformat" type="text/html">
    <div class="design-field design-setting-label">
        <div class="fieldname">
            <span>时间格式</span>
            <span class="fieldinfo"></span>
        </div>
        <div class="fieldblock">
            <select name="timeformat">
                <option value="0" {{ timeformat == '0' ? 'selected' : '' }}>日期</option>
                <option value="1" {{ timeformat == '1' ? 'selected' : '' }}>日期时间</option>
                <option value="2" {{ timeformat == '2' ? 'selected' : '' }}>时间</option>
            </select>
        </div>
    </div>
</script>

<script id="tmpl-setting-explain-valid" type="text/html">
    <div class="design-field design-setting-options">
        <div class="fieldname">
            <span>控件说明</span><br>
            <span class="fieldinfo">验证控件不会显示在界面上</span><br>
            <span class="fieldinfo">当验证表达式结果为false时，会提示对话框</span><br>
            <span class="fieldinfo">表达式可以使用计算和比较运算符及括号</span><br>
            <span class="fieldinfo">计算运算符如下:【+】【-】【*】【/】</span><br>
            <span class="fieldinfo">比较运算符如下:【>】【>=】【<】【<=】【==】【!=】</span>
        </div>
    </div>
</script>
<script id="tmpl-setting-explain-compute" type="text/html">
    <div class="design-field design-setting-options">
        <div class="fieldname">
            <span>控件说明</span><br>
            <span class="fieldinfo">计算控件可对数值型控件进行相互计算</span><br>
            <span class="fieldinfo">控件显示表达式计算结果</span><br>
            <span class="fieldinfo">表达式可以使用计算运算符及括号</span><br>
            <span class="fieldinfo">可用计算运算符如下:【+】【-】【*】【/】</span>
        </div>
    </div>
</script>

<script id="tmpl-setting-withparam" type="text/html">
    <div class="design-field design-setting-placeholder">
        <div class="fieldname">
            <span>附带参数</span>
            <span class="fieldinfo">点击按钮时附带的url参数字符串</span>
        </div>
        <div class="fieldblock">
            <input name="withParam" type="text" value="{{withParam}}">
        </div>
    </div>
</script>
<script id="tmpl-setting-submittype" type="text/html">
    <div class="design-field design-setting-label">
        <div class="fieldname">
            <span>提交数据</span>
            <span class="fieldinfo">点击按钮时要提交的数据</span>
        </div>
        <div class="fieldblock">
            <select name="submitType">
                <option value="0" {{ submitType == '0' ? 'selected' : '' }}>无</option>
                <option value="1" {{ submitType == '1' ? 'selected' : '' }}>仅参数</option>
                <option value="2" {{ submitType == '2' ? 'selected' : '' }}>表单和参数</option>
            </select>
        </div>
    </div>
</script>

<script id="tmpl-setting-widget-width" type="text/html">
    <div class="design-field design-setting-label">
        <div class="fieldname">
            <span>控件宽度</span>
            <span class="fieldinfo"></span>
        </div>
        <div class="fieldblock">
            <select name="widgetWidth">
                <# for (var i = 3; i <= 12; i++) { #>
                    <option value="{{i}}" {{ widgetWidth == i.toString() ? 'selected' : '' }}>{{i}}</option>
                <# } #>
            </select>
        </div>
    </div>
</script>

<script id="tmpl-setting-document-template" type="text/html">
    <div class="design-field design-setting-label">
        <div class="fieldname">
            <span>文档模板</span>
            <span class="fieldinfo"><a href="javascript:" role="preview">预览模板</a></span>
        </div>
        <div class="fieldblock">
            <select name="placeholder">
                <option value="official-documents" {{ placeholder == 'official-documents' ? 'selected' : '' }}>公文</option>
            </select>
        </div>
    </div>
</script>

<script id="tmpl-select-item" type="text/html">
    <div class="fieldblock design-setting-option {{ isChild ? 'linkage-children' : '' }}" >
        <input name="selects" type="text" maxlength="20" value="{{isChild ? (index * 100).toString() : ('选项' + index)}}" >
        <a class="action action-del" data-action="del"><i class="icon icon-minus"></i></a>
        <a class="action action-add" data-action="add"><i class="icon icon-plus"></i></a>
        <# if (hasChildren) { #>
            <div class="children-list">
                <# for (var i = 1; i <= 3; i++) { #>
                <div class="fieldblock design-setting-option linkage-children" >
                    <input type="text" maxlength="20" value="{{(i * 100).toString()}}">
                    <a class="action action-del" data-action="del"><i class="icon icon-minus"></i></a>
                    <a class="action action-add" data-action="add"><i class="icon icon-plus"></i></a>
                </div>
                <# } #>
            </div>
        <# } #>
    </div>
</script>
<script id="tmpl-compute-widgets" type="text/html">
    <# _.each(paramMap, function (item) { #>
        <tr>
            <td>{{item.paramName}}</td>
            <td>{{item.widget.data.label}}</td>
            <td><a href="javascript:" data-param="{{item.paramName}}">移除</a></td>
        </tr>
    <# }) #>
</script>

<script>
    var UUID = '${UUID}';
    seajs.use('page/edition-fourth/flow/edit-form-pc', function (module) {
        module.run();
    });
</script>
</body>
</html>
