define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore'),
        Backbone = require('backbone');
    var template = require('template');
    var confirm = require('component/Confirm');
    var alert = require('component/Alert');
    var computeExpression = require('component/util/computeExpression');
    var sessionHelper = require('storageHelper').session;
    require('jquery-util');
    require('jqueryui');
    require('plupload');

    var DocPreviewModal = require('component/DocPreviewModal');

    var urls = {
        manager: CONTEXT_PATH + '/flow-e4/flowManager.do',
        uploadPic: CONTEXT_PATH + '/file/imageUpload.do?num=' + UUID
    };

    //定死的控件表 将来可能会改动态数据
    var constWidgets = [
        { id: 'TextField', code: '', des: '单行输入框', en_name: 'TextField', name: '单行输入框', type: '单行输入框' },
        { id: 'TextareaField', code: '', des: '多行输入框', en_name: 'TextareaField', name: '多行输入框', type: '多行输入框' },
        { id: 'NumberField', code: '', des: '数字输入框', en_name: 'NumberField', name: '数字输入框', type: '数字输入框' },
        { id: 'DDSelectField', code: '', des: '单选框', en_name: 'DDSelectField', name: '单选框', type: '单选框' },
        { id: 'DDMultiSelectField', code: '', des: '多选框', en_name: 'DDMultiSelectField', name: '多选框', type: '多选框' },
        { id: 'DDDateField', code: '', des: '日期', en_name: 'DDDateField', name: '日期', type: '日期' },
        { id: 'DDDateRangeField', code: '', des: '日期区间', en_name: 'DDDateRangeField', name: '日期区间', type: '日期区间' },
        { id: 'DDPhotoField', code: '', des: '图片', en_name: 'DDPhotoField', name: '图片', type: '图片' },
        { id: 'TableField', code: '', des: '明细', en_name: 'TableField', name: '明细', type: '明细' },
        { id: 'TextNote', code: '', des: '说明文字', en_name: 'TextNote', name: '说明文字', type: '说明文字' },
        { id: 'MoneyField', code: '', des: '金额', en_name: 'MoneyField', name: '金额', type: '金额' },
        { id: 'DDAttachment', code: '', des: '附件', en_name: 'DDAttachment', name: '附件', type: '附件' },
        { id: 'PictureNote', code: '', des: '说明文字', en_name: 'PictureNote', name: '说明图片', type: '说明图片' },
        { id: 'LinkageSelectField', code: '', des: '联动选择框', en_name: 'LinkageSelectField', name: '联动选择框', type: '联动选择框' },
        { id: 'ComputeField', code: '', des: '数值计算', en_name: 'ComputeField', name: '数值计算', type: '数值计算' },
        { id: 'ValidField', code: '', des: '条件判断', en_name: 'ValidField', name: '条件判断', type: '条件判断' },
        { id: 'EventButton', code: '', des: '事件按钮', en_name: 'EventButton', name: '事件按钮', type: '事件按钮' },
        { id: 'UserSelect', code: '', des: '用户选择', en_name: 'UserSelect', name: '用户选择', type: '用户选择' },
        { id: 'TabPage', code: '', des: '标签页', en_name: 'TabPage', name: '标签页', type: '标签页' },
        { id: 'RTEditor', code: '', des: '富文本编辑', en_name: 'RTEditor', name: '富文本编辑', type: '富文本编辑' },
        { id: 'DocumentView', code: '', des: '文档模板', en_name: 'DocumentView', name: '文档模板', type: '文档模板' }
    ];
    var iconImages = ['icon_bx', 'icon_bxiao', 'icon_byj', 'icon_cc', 'icon_dh',
        'icon_fk', 'icon_htsp', 'icon_hysp', 'icon_jb', 'icon_kdjs',
        'icon_lxsq', 'icon_qj', 'icon_rb', 'icon_rbb', 'icon_sk',
        'icon_sp', 'icon_syzz', 'icon_ty', 'icon_wc', 'icon_wply',
        'icon_wprk', 'icon_wpsg', 'icon_xwbl', 'icon_yb', 'icon_ybb',
        'icon_ycsq', 'icon_ywlxd', 'icon_yz', 'icon_zb', 'icon_zbb'];

    var isDecimal = function (val) { return /^\d+(\.\d*){0,1}$/.test(val); };
    var parseQueryString = function (qs, sep, eq, options) {
        sep = sep || '&';
        eq = eq || '=';

        var obj = {};
        if ($.type(qs) !== 'string' || qs.length === 0) {
            return obj;
        }

        var regexp = /\+/g;
        qs = qs.split(sep);

        var maxKeys = 1000;
        if (options && $.isNumeric(options.maxKeys)) {
            maxKeys = options.maxKeys;
        }

        var len = qs.length;
        // maxKeys <= 0 则不限制键的个数
        if (maxKeys > 0 && len > maxKeys) {
            len = maxKeys;
        }

        var decode = decodeURIComponent;
        if (options && $.isFunction(options.decodeURIComponent)) {
            decode = options.decodeURIComponent;
        }

        for (var i = 0; i < len; ++i) {
            var x = qs[i].replace(regexp, '%20'),
                idx = x.indexOf(eq),
                kstr, vstr, k, v;

            if (idx >= 0) {
                kstr = x.substr(0, idx);
                vstr = x.substr(idx + 1);
            } else {
                kstr = x;
                vstr = '';
            }

            try {
                k = decode(kstr);
                v = decode(vstr);
            } catch (e) {
                k = decodeURIComponent(kstr, true);
                v = decodeURIComponent(vstr, true);
            }

            if (!Object.hasOwnProperty.call(obj, k)) {
                obj[k] = v;
            } else if ($.isArray(obj[k])) {
                obj[k].push(v);
            } else {
                obj[k] = [obj[k], v];
            }
        }
        return obj;
    };
    var getParam = function (key) {
        var href = location.href;
        var queryString = href.slice(href.indexOf('?') + 1);
        var params = parseQueryString(queryString);

        return params[key] || '';
    };

    var settingRenders = {},
        widgetRenders = {};
    var getSettingRender = function (type, settings) {
        if (!settingRenders[type]) {
            var html = [];
            _.each(settings, function (setting) {
                if (typeof setting !== 'string') setting = setting.type;
                html.push($('#tmpl-setting-' + setting).html());
            });

            settingRenders[type] = template(html.join(''));
        }
        return settingRenders[type];
    };
    var getWidgetRender = function (type) {
        if (!widgetRenders[type]) {
            widgetRenders[type] = template($('#tmpl-widget-' + type.toLowerCase()).html());
        }
        return widgetRenders[type];
    };
    var selectRender = template($('#tmpl-select-item').html());
    var computeWidgetRender = template($('#tmpl-compute-widgets').html());

    var setUploader = function (button, url, filefilter, callback) {
        var uploader = new plupload.Uploader({
            browse_button: button[0],
            url: url,
            multi_selection: false,
            filters: filefilter,
            multipart_params: {
                selfCompanyId: COMPANY_ID,
                companyId: COMPANY_ID,
                userId: USER_ID,
                secretKey: SECRET_KEY
            }
        });
        uploader.bind('FilesAdded', function (_uploader, files) {
            if (files.length > 0) {
                _uploader.start();
            }
            else {
                alert('请选择文件').delay(3);
            }
        });
        uploader.bind('FileUploaded', function (_uploader, file, response) {
            if (response.status !== 200) {
                alert('发生错误，文件上传失败！').delay(3);
                return;
            }

            try {
                var result = $.parseJSON(response.response);
                if (result.success) {
                    callback && callback(result);
                }
                else {
                    alert(result.message || '上传出错！').delay(3);
                }
            }
            catch (ex) {
                alert('发生错误，上传失败！').delay(3);
            }
        });
        uploader.bind('Error', function (_uploader, error) {
            var message = '未知错误';
            if (typeof error === 'string') {
                message = error;
            }
            else if (typeof error === 'object') {
                switch (error.code) {
                    case -601:
                        message = '文件类型不正确，请上传' + filefilter.mime_types[0].extensions + '类型文件';
                        break;
                    case -600:
                        message = '文件大小超出限制，请重新选择';
                        break;
                    case -400:
                    default:
                        message = error.message;
                        break;
                }
            }
            alert(message).delay(3);
        });
        uploader.init();
        return uploader;
    };

    var emptySettingData = {
        label: '',
        placeholder: '',
        required: false,
        type: '',
        unit: '',
        selects: [],
        imginfo: '',
        linkage: '',
        alias: '',
        useAlias: false,
        isInteger: false,
        toSum: false,
        expression: '',
        mustnumber: false,
        canImport: false,
        readonly: false,
        widgetWidth: 12,
        //EventButton属性
        withParam: '',
        submitType: '1'
    };
    var emptySettingJson = {
        describeName: '',      //名称
        isRequired: '',        //必填 1是 0否
        isEffective: '',       //是否可以导入 1可以 0不可以
        isMerge: '',           //是否只读 1是 0否
        reName: '',            //id + 序号
        sequence: '',          //序号
        controlId: '',         //id
        exp: '',               //说明文字
        value: '',             //默认值,当前版本配置恒定为空字符串
        jsonData: '',          //选择控件 选项数据json
        alias: '',
        useAlias: false
    };
    //控件配置表
    var widgetConfig = {
        'TextField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'alias', 'widget-width', 'required', 'canimport', 'readonly'],
            defaultData: {
                label: '单行输入框',
                placeholder: '请输入',
                required: false
            }
        },
        'TextareaField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'alias', 'required', 'canimport', 'readonly'],
            defaultData: {
                label: '多行输入框',
                placeholder: '请输入',
                required: false
            }
        },
        'NumberField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'alias', 'widget-width', 'required', 'integer', 'tosum', 'canimport', 'readonly'], //'unit', 没有对应字段注释掉
            defaultData: {
                label: '数字输入框',
                placeholder: '请输入',
                unit: '',
                required: false
            }
        },
        'DDSelectField': {
            template: 'normal',
            settings: ['label', 'options', 'alias', 'widget-width', 'required', 'canimport'],
            defaultData: {
                label: '单选框',
                placeholder: '请选择',
                required: false,
                icon: 'enter',
                selects: ['选项1','选项2','选项3']
            }
        },
        'DDMultiSelectField': {
            template: 'normal',
            settings: ['label', 'options', 'alias', 'widget-width', 'required', 'canimport'],
            defaultData: {
                label: '多选框',
                placeholder: '请选择',
                required: false,
                icon: 'enter',
                selects: ['选项1','选项2','选项3']
            }
        },
        'DDDateField': {
            template: 'normal',
            settings: ['label', 'timeformat', 'alias', 'widget-width', 'required'],
            defaultData: {
                label: '日期',
                placeholder: '请选择',
                required: false,
                timeformat: '0',
                icon: 'enter'
            }
        },
        'DDDateRangeField': {
            template: 'datarange',
            settings: ['label', 'timeformat', 'alias', 'widget-width', 'required'],
            defaultData: {
                label: ['开始时间', '结束时间'],
                placeholder: '请选择',
                required: false,
                timeformat: '0',
            }
        },
        'DDPhotoField': {
            template: 'normal',
            settings: ['label', 'required'],
            defaultData: {
                label: '图片',
                placeholder: '',
                required: false,
                icon: 'camera'
            }
        },
        'TableField': {
            template: 'tablefield',
            settings: ['label', 'actionname'],
            defaultData: {
                label: '明细',
                placeholder: '增加明细'
            }
        },
        'TextNote': {
            template: 'textnote',
            settings: ['textnote', 'canimport'],
            defaultData: {
                placeholder: '请输入说明文字'
            }
        },
        'MoneyField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'alias', 'widget-width', 'required', 'tosum', 'canimport', 'readonly'],
            defaultData: {
                label: '金额',
                placeholder: '请输入',
                required: false
            }
        },
        'DDAttachment': {
            template: 'normal',
            settings: ['label', 'required'],
            defaultData: {
                label: '附件',
                placeholder: '',
                required: false,
                icon: 'chakanfujian'
            }
        },
        'PictureNote': {
            template: 'picturenote',
            settings: ['textnote', 'picture'],
            defaultData: {
                placeholder: '请输入说明文字'
            }
        },
        'LinkageSelectField': {
            template: 'datarange',
            settings: ['label', 'widget-width', 'required', 'linkageoptions', 'canimport'],
            defaultData: {
                label: ['父类', '子类'],
                placeholder: '请选择',
                required: false,
                mustnumber: true,
                linkage: JSON.stringify({
                    selects: [
                        { value: '选项1', children: [ { value: '100'}, {value: '200'}, {value: '300' } ] },
                        { value: '选项2', children: [ { value: '100'}, {value: '200'}, {value: '300' } ] },
                        { value: '选项3', children: [ { value: '100'}, {value: '200'}, {value: '300' } ] }
                    ]
                })
            }
        },
        'ComputeField': {
            template: 'normal',
            settings: ['label', 'alias', 'widget-width', 'compute', 'explain-compute'],
            defaultData: {
                label: '数值计算'
            }
        },
        'ValidField': {
            template: 'valid',
            settings: ['alert', 'compute', 'explain-valid'],
            defaultData: {
                placeholder: '提示信息'
            }
        },
        'EventButton': {
            template: 'button',
            settings: ['label', 'event', 'withparam', 'submittype'],
            defaultData: {
                label: '事件按钮',
                placeholder: '',
                required: false
            }
        },
        'UserSelect': {
            template: 'normal',
            settings: ['label', 'multi', 'alias'],
            defaultData: {
                label: '人员选择',
                placeholder: '',
                multi: false,
                required: false,
                icon: 'add'
            }
        },
        'TabPage': {
            template: 'tabpage',
            settings: ['label'],
            defaultData: {
                label: '标签页',
                placeholder: ''
            }
        },
        'RTEditor': {
            template: 'rteditor',
            settings: ['label', 'placeholder', 'alias', 'required', 'canimport'],
            defaultData: {
                label: '富文本编辑框',
                placeholder: '请输入',
                required: false
            }
        },
        'DocumentView': {
            template: 'normal',
            settings: ['document-template'],
            defaultData: {
                label: '文档模板',
                placeholder: 'official-documents'
            }
        }
    };

    //表单提交
    var validMap = {
        required: function (val) { return $.trim(val) !== ''; },
        mobile: function (val) { return /^1[3,4,5,7,8](\d{9})$/.test(val); },
        email: function (val) { return /^\S+@\S+.\S+$/.test(val); },
        length: function (val, param) {
            return ($.trim(val).length >= param[0] && $.trim(val).length <= param[1]);
        }
    };
    var validOption = {
        'label': [
            { validItem: 'required', message: '标题不能为空' }
        ]
    };
    var validFormOption = {
        'name': [
            { validItem: 'required', message: '表单名称不能为空' }
        ]
    };
    var checkValidItem = function (element, optionItem) {
        var success = true,
            errornote,
            value = element.val();
        for (var i = 0; i < optionItem.length; i++) {
            if (!validMap[optionItem[i].validItem](value, optionItem[i].param)) {
                success = false;
                element.addClass('has-error');
                errornote = element.next();
                if (!errornote.hasClass('error-note')) {
                    errornote = $('<span class="error-note"></span>');
                    element.after(errornote);
                }
                errornote.text(optionItem[i].message);
                break;
            }
        }
        return success;
    };
    var checkValid = function (form, options) {
        var success = true;
        var elements;
        for (var item in options) {
            elements = form.find('[name=' + item + ']');
            if (elements.length > 0) {
                elements.each(function (index, elem) {
                    if (!checkValidItem($(elem), options[item])) {
                        success = false;
                    }
                });
            }
        }
        return success;
    };

    var jsonDataParser = {
        'DDSelectField': function () {
            return {
                options: typeof this.data.selects === 'string' ? [this.data.selects] : this.data.selects
            };
        },
        'DDMultiSelectField': function () {
            return {
                options: typeof this.data.selects === 'string' ? [this.data.selects] : this.data.selects
            };
        },
        'PictureNote': function () {
            return {
                imginfo: this.data.imginfo.length > 0 ? JSON.parse(this.data.imginfo) : ''
            };
        },
        'NumberField': function () {
            return {
                integer: this.data.isInteger,
                toSum: this.data.toSum
            };
        },
        'MoneyField': function () {
            return {
                toSum: this.data.toSum
            };
        },
        'LinkageSelectField': function () {
            var linkageOption = this.setting.getLinkAgeData();
            if (linkageOption === false) {
                this.active(true);
                return false;
            }
            else {
                return {
                    linkageOption: linkageOption
                };
            }
        },
        'ComputeField': function () {
            var expressOption = this.setting.getComputeExpression();
            if (expressOption.success === false) {
                if (expressOption.toActive) expressOption.toActive.active(true);
                else this.active(true);
                return false;
            }
            else {
                return expressOption;
            }
        },
        'ValidField': function () {
            var expressOption = this.setting.getValidExpression();
            if (expressOption.success === false) {
                if (expressOption.toActive) expressOption.toActive.active(true);
                else this.active(true);
                return false;
            }
            else {
                return expressOption;
            }
        }
    };
    var parseButtonData = function (data) {
        return {
            withParam: data.withParam,
            submitType: data.submitType
        };
    };

    var latters =  'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
    var pageGlobalState = {};
    var doWidgetChecking = function (yes) {
        if (yes) {
            pageGlobalState['widgetChecking'] = true;
            $(document.body).css('cursor', 'crosshair');
        }
        else {
            pageGlobalState['widgetChecking'] = false;
            $(document.body).css('cursor', '');
        }
    };

    var couldComputeMap = {
        'NumberField': true,
        'MoneyField': true,
        'LinkageSelectField': true,
        'ComputeField': true
    };
    var computeSetting = function () {
        var _this = this;
        this.paramMap = {};
        this.$('.checkwidgets').on('click', function (e) {
            doWidgetChecking(true);
            pageGlobalState['ListeningCompute'] = _this;

            e.stopPropagation();
        });

        this.appendComputeWidget = function (widget) {
            var _this = this;
            var paramName = '', paramCount = 0;
            for (var key in this.paramMap) {
                if (this.paramMap[key].widget === widget) {
                    alert('该控件已在运算控件列表中\r\n请不要重复添加');
                    return;
                }
                paramCount++;
            }
            if (paramCount >= 20) {
                alert('参与运算的控件不能超过20个！');
                return;
            }

            for (var i = 0; i < latters.length; i++) {
                if (!this.paramMap[latters[i]]) {
                    paramName = latters[i];
                    break;
                }
            }

            this.appendToParamMap(paramName, widget);
            this.renderComputeWidgets();
        };
        this.appendToParamMap = function (paramName, widget) {
            this.paramMap[paramName] = { paramName: paramName, widget: widget };
            widget.on('removed', function () {
                _this.removeComputeWidget(paramName);
            });
        };
        this.removeComputeWidget = function (paramName) {
            delete this.paramMap[paramName];
            //this.$('input[name=expression]').val('');
            this.renderComputeWidgets();
        };
        this.renderComputeWidgets = function () {
            this.$('.compute-widgets > tbody').html(computeWidgetRender({ paramMap: this.paramMap }));
        };

        this.$('.compute-widgets').on('click', 'a', function (e) {
            var paramName = $(this).data('param');
            _this.removeComputeWidget(paramName);
        });

        var org_show = this.$el.show;
        this.$el.show = function () {
            _this.renderComputeWidgets();
            org_show.apply(_this.$el);
        };
    };
    var docPreviewSetting = function () {
        var _this = this;
        var previewModal = new DocPreviewModal();
        this.$('[role="preview"]').on('click', function () {
            previewModal.show({ tmpl: _this.belong.data.placeholder });
        });
    };

    var checkboxNames = ['required', 'toSum', 'canImport', 'readonly', 'isInteger', 'mustnumber', 'multi', 'useAlias'];
    var SettingView = Backbone.View.extend({
        tagName: 'form',
        events: {
            'input input[type=text]': 'change',
            'change input[type=checkbox]': 'change',
            'change select': 'change',
            'input textarea': 'change',
            'click .action': 'changeSelects'
        },
        initialize: function (options) {
            this.belong = options.belong;
            this.type = options.type;
            var settingRender = getSettingRender(options.type, options.settings);
            options.data.inTablefield = options.inTablefield;
            this.$el.html(settingRender(options.data));

            if (options.settings.indexOf('options') >= 0 || options.settings.indexOf('linkageoptions') >= 0) {
                this.$selectList = this.$('.selection-list');
            }
            if (options.settings.indexOf('picture') >= 0) {
                this.initPicUpload();
            }
            if (options.settings.indexOf('compute') >= 0) {
                this.initCompute();
            }
            if (options.settings.indexOf('alias') >= 0) {
                this.$alias = this.$('[name="alias"]');
            }
            if (options.settings.indexOf('document-template') >= 0) {
                this.initDocPreview();
            }
        },
        change: function (e) {
            if ($(e.target).is('[name="useAlias"]')) {
                this.$alias.prop('disabled', !$(e.target).prop('checked'));
                if ($(e.target).prop('checked')) {
                    this.$alias.val(this.belong.data.label);
                }
                else {
                    this.$alias.val('');
                }
            }

            var formData = this.$el.serializeArray();
            this.trigger('change', formData);
        },
        changeSelects: function (e) {
            var elem = $(e.currentTarget);
            var action = elem.data('action');
            var length = elem.parent().parent().children().length; //this.$selectList.children().length;

            var isChild = elem.parent().is('.linkage-children');
            var hasChildren = elem.parent().find('.children-list').length > 0;

            if (action === 'del') {
                if (length <= 1) { alert('至少需要1个选项！'); }
                else { elem.parent().remove(); }
            }
            else {
                if (length >= 50) { alert('最多只能设置50个选项!'); }
                else {
                    var isEmpty = elem.is('.empty-plus');
                    elem.parent().after(selectRender({
                        index: length + (isEmpty ? 0 : 1),
                        isChild: isChild,
                        hasChildren: hasChildren
                    }));
                    if (isEmpty) {
                        elem.parent().remove();
                    }
                }
            }

            this.change();
        },
        initPicUpload: function () {
            var _this = this;
            var btn = this.$('a');
            setUploader(btn, urls.uploadPic, {
                    mime_types: [{
                        title: '图片文件',
                        extensions: 'jpg,jpeg,png,gif'
                    }],
                    max_file_size: '4096kb'
                }, function (result) {
                    var model = result.model;
                    _this.$('input[name=imginfo]')
                        .val(JSON.stringify({ id: model.id , addr: model.addr }));
                    _this.change();
                });
        },
        initCompute: function () {
            computeSetting.apply(this);
        },
        initDocPreview: function () {
            docPreviewSetting.apply(this);
        },
        valid: function () {
            var success = checkValid(this.$el, validOption);
            return success;
        },
        removeError: function (e) {
            var elem = $(e.currentTarget);
            elem.removeClass('has-error');
            elem.next('.error-note').remove();
        },
        getLinkAgeData: function () {
            var mustnumber = this.$el.find('input[name=mustnumber]').prop('checked');
            var selects = [];

            var success = true;
            this.$selectList.children().each(function (index, item) {
                var value = $(item).find('>input').val();
                var children = [];
                $(item).find('.children-list input').each(function (childIndex, child) {
                    var childVal = $(child).val();
                    if (mustnumber && !isDecimal(childVal)) {
                        alert('联动选择框子项配置为数字时，必须输入数字！');
                        success = false; return false;
                    }
                    children.push({ value: childVal });
                });
                selects.push({ value: value, children: children });
                if (!success) { return false; }
            });

            if (!success) return success;
            else return { mustnumber: mustnumber, selects: selects };
        },
        getComputeExpression: function () {
            var isRight = true,toActive;
            var expressionData = {};
            var expression = $.trim(this.$('input[name=expression]').val());
            var atomList;

            if (expression.length === 0) {
                alert('表达式不能为空！');
            }
            else if (!(/[A-Z0-9\+\-\*\/\(\)]/g.test(expression))) {
                isRight = false;
                alert('表达式有语法错误，请返回检查！');
            }
            else {
                try {
                    atomList = computeExpression.getAtomList(expression);
                } catch (e) {
                    alert('表达式有语法错误，请返回检查！');
                    isRight = false;
                }
            }

            if (isRight) {
                var i = 0;
                for (i = 0; i < atomList.length; i++) {
                    if (atomList[i].type === computeExpression.atomMap.PARAM) {
                        var paramItem = this.paramMap[atomList[i].value];
                        if (!paramItem) {
                            isRight = false;
                            alert('计算表达式没有参数' + atomList[i].value + '对应的控件！\r\n请返回检查！');
                            break;
                        }
                        else if (paramItem.widget.type === 'LinkageSelectField'
                            && paramItem.widget.data.mustnumber !== true) {
                            isRight = false;
                            alert('作为表达式参数时联动选择框必须勾选“限制子类为数字”');
                            toActive = paramItem.widget;
                            break;
                        }
                    }
                }

                for (i in this.paramMap) {
                    expressionData[i] = this.paramMap[i].widget.type + this.paramMap[i].widget.sequence;
                }
            }

            return isRight ? { expression: expression, expressionData: expressionData }
                : { success: isRight, toActive: toActive };
        },
        getValidExpression: function () {
            var isRight = true, toActive;
            var expressionData = {};
            var expression =  $.trim(this.$('input[name=expression]').val());
            var atomList;

            if (expression.length === 0) {
                alert('表达式不能为空！');
            }
            else if (!(/[A-Z0-9\+\-\*\/\(\)\>\<\=\&\|]/g.test(expression))) {
                isRight = false;
                alert('表达式有语法错误，请返回检查！');
            }
            else {
                try {
                    atomList = computeExpression.getAtomList(expression);
                } catch (e) {
                    alert('表达式有语法错误，请返回检查！');
                    isRight = false;
                }
            }

            if (isRight) {
                var i = 0;
                var checkParamMap = {}, checkResult;
                for (i = 0; i < atomList.length; i++) {
                    if (atomList[i].type === computeExpression.atomMap.PARAM) {
                        var paramItem = this.paramMap[atomList[i].value];
                        if (!paramItem) {
                            isRight = false;
                            alert('验证表达式没有参数' + atomList[i].value + '对应的控件！\r\n请返回检查！');
                            break;
                        }
                        else if (paramItem.widget.type === 'LinkageSelectField'
                            && paramItem.widget.data.mustnumber !== true) {
                            isRight = false;
                            alert('作为表达式参数时联动选择框必须勾选“限制子类为数字”');
                            toActive = paramItem.widget;
                            break;
                        }
                        else {
                            checkParamMap[atomList[i].value] = 1;
                        }
                    }
                }
                checkResult = computeExpression.compute(expression, checkParamMap);
                if (typeof checkResult !== 'boolean') {
                    alert('验证表达式返回值必须是布尔类型');
                    isRight = false;
                }

                for (i in this.paramMap) {
                    expressionData[i] = this.paramMap[i].widget.type + this.paramMap[i].widget.sequence;
                }
            }

            return isRight ? { expression: expression, expressionData: expressionData }
                : { success: isRight, toActive: toActive };
        }
    });
    var WidgetView = Backbone.View.extend({
        tagName: 'div',
        className: 'design-component',
        events: {
            'click': 'click'
        },
        initialize: function (options) {
            this.type = options.type;
            this.inTablefield = options.inTablefield;
            this.defaultData = _.extend({}, emptySettingData, options.config.defaultData);
            this.data = _.extend({}, this.defaultData, options.initData || {});
            this.$el.addClass('design-component-' + options.type.toLowerCase());
            this.settingPanel = options.settingPanel;

            this.widgetRender = getWidgetRender(options.config.template);
            this.render();

            this.setting = new SettingView({
                data: this.data,
                type: this.type,
                belong: this,
                inTablefield: this.inTablefield,
                settings: options.config.settings
            });
            this.settingPanel.append(this.setting.$el);

            this.listenTo(this.setting, 'change', this.change);
        },
        click: function (e) {
            e.stopPropagation();

            if (pageGlobalState['widgetChecking']) {
                Backbone.trigger('widgetChecked', this);
                doWidgetChecking(false);
                return;
            }

            var isRemove = $(e.target).is('.design-remove');
            if (isRemove) { this.deleteSelf(); }
            else { this.active(); }
        },
        change: function (formData) {
            var newData = {};
            _.each(formData, function (item) {
                if (newData.hasOwnProperty(item.name)) {
                    if (!(newData[item.name] instanceof Array)) {
                        newData[item.name] = [newData[item.name]];
                    }
                    newData[item.name].push(item.value);
                }
                else {
                    newData[item.name] = item.value;
                }
            });
            _.each(checkboxNames, function (checkboxname) {
                if (!newData[checkboxname] || newData[checkboxname] !== 'on') {
                    newData[checkboxname] = false;
                }
                else {
                    newData[checkboxname] = true;
                }
            });

            this.data = _.extend({}, this.defaultData, newData);
            this.render();
        },
        appendTo: function (parent, index) {
            if (typeof index !== 'undefined') {
                parent.$container.children().eq(index).replaceWith(this.$el);
            } else {
                parent.$container.append(this.$el);
            }
            this.parentContainer = parent;
        },
        deleteSelf: function () {
            if (this.$el.is('.active')) {
                this.settingPanel.activeForm();
            }

            this.$el.remove();
            this.setting.$el.remove();
            this.parentContainer.remove(this);

            this.trigger('removed');
        },
        active: function (force) {
            if (!this.$el.is('.active') || force === true) {
                this.parentContainer.clearActive();
                this.$el.addClass('active');

                this.settingPanel.hideWidgets();
                this.settingPanel.activeWidget(this);
                this.setting.$el.show();
            }
        },
        getIndex: function () {
            return this.$el.index();
        },
        toDataJSON: function () {
            if (!this.setting.valid()) {
                this.active(true);
                return false;
            }
            else {
                var jsonData = '';
                if (jsonDataParser.hasOwnProperty(this.type)) {
                    jsonData = jsonDataParser[this.type].apply(this);
                    if (jsonData === false) {
                        return false;
                    }
                }

                var model = _.extend({}, emptySettingJson, {
                    describeName: this.data.label instanceof Array ? this.data.label.join(',') : this.data.label,
                    isRequired: (this.data.required === true) ? '1' : '0',
                    isEffective: (this.data.canImport === true) ? '1' : '0',
                    isMerge: (this.data.readonly === true) ? '1' : '0',
                    reName: this.type + this.sequence,
                    sequence: this.sequence,
                    controlId: this.type,
                    exp: this.type === 'TextNote' ? this.data.placeholder.replace(/(\r\n|\n)/ig, '<br>') : this.data.placeholder,
                    value: '', //jsonData,
                    jsonData: jsonData,
                    alias: this.data.alias,
                    useAlias: this.data.useAlias,
                    multi: this.data.multi,
                    isStorage: this.data.timeformat,
                    field1: this.data.widgetWidth,   //field1字段用于保存PC端控件宽度信息
                    field2: this.type === 'EventButton' ? parseButtonData(this.data) : null,
                    field3: null   //预留字段，有新属性使用这3个预留字段，不要再加新字段
                });
                return model;
            }
        },
        render: function () {
            this.$el.css('width', (this.data.widgetWidth * 8.333333) + '%');
            this.$el.html(this.widgetRender(this.data));
        }
    });
    var TableFieldView = WidgetView.extend({
        initialize: function (options) {
            this.constructor.__super__.initialize.apply(this, [options]);
            this.widgetsList = [];
            this.$container = this.$('.design-componentgroup');
            this.$container.sortable({
                stop: function (event, ui) {
                    if (ui.item.is('.design-widgetitem')) {
                        var type = ui.item.data('type'),
                            index = ui.item.index();
                        var parent = ui.item.parent();
                        //ui.item.remove();
                        Backbone.trigger('addWidget', type, index, parent);
                    }
                }
            });

            this.$label = this.$('.design-componentview-label');
            this.$actionName = this.$('.design-componentview-adddetail');
        },
        change: function (formData) {
            var newData = {};
            _.each(formData, function (item) {
                if (newData[item.name]) {
                    if (!(newData[item.name] instanceof Array)) {
                        newData[item.name] = [newData[item.name]];
                    }
                    newData[item.name].push(item.value);
                }
                else {
                    newData[item.name] = item.value;
                }
            });
            this.data = _.extend({}, this.defaultData, newData);

            this.$label.text(this.data.label);
            this.$actionName.text(this.data.placeholder);
        },
        append: function (widget, index) {
            widget.appendTo(this, index);
            this.widgetsList.push(widget);
        },
        clearActive: function () {
            this.parentContainer.clearActive();
        },
        remove: function (widget) {
            var index = 0;
            for (var i = 0; i < this.widgetsList.length; i++) {
                if (this.widgetsList[i] === widget) {
                    index = i; break;
                }
            }
            this.widgetsList.splice(index, 1);
        },
        sortWidgets: function () {
            this.widgetsList.sort(function (a,b) { return a.$el.index() - b.$el.index(); });
            for (var i = 0; i < this.widgetsList.length; i++) {
                this.widgetsList[i].sequence = this.sequence + i + 1;
            }
        },
        deleteSelf: function () {
            while (this.widgetsList.length > 0) {
                this.widgetsList[0].deleteSelf();
            }
            this.constructor.__super__.deleteSelf.apply(this);
        },
        toDataJSON: function () {
            var models = [], tempModel;
            this.sortWidgets();
            var selfModel = this.constructor.__super__.toDataJSON.apply(this);
            if (!selfModel) { models = false; }
            else {
                models.push(selfModel);
                for (var i = 0; i < this.widgetsList.length; i++) {
                    tempModel = this.widgetsList[i].toDataJSON();
                    if (!tempModel) { models = false; break; }
                    models.push(tempModel);
                }
            }
            return models;
        }
    });
    var TabPageView = WidgetView.extend({
        initialize: function (options) {
            this.constructor.__super__.initialize.apply(this, [options]);
            this.widgetsList = [];
            this.$container = this.$('.design-componentgroup');
            this.$container.sortable({
                stop: function (event, ui) {
                    if (ui.item.is('.design-widgetitem')) {
                        var type = ui.item.data('type'),
                            index = ui.item.index();
                        var parent = ui.item.parent();
                        //ui.item.remove();
                        Backbone.trigger('addWidget', type, index, parent);
                    }
                }
            });

            this.$label = this.$('.design-componentview-label');
        },
        change: function (formData) {
            var newData = {};
            _.each(formData, function (item) {
                if (newData[item.name]) {
                    if (!(newData[item.name] instanceof Array)) {
                        newData[item.name] = [newData[item.name]];
                    }
                    newData[item.name].push(item.value);
                }
                else {
                    newData[item.name] = item.value;
                }
            });
            this.data = _.extend({}, this.defaultData, newData);

            this.$label.text(this.data.label);
        },
        append: function (widget, index) {
            widget.appendTo(this, index);
            this.widgetsList.push(widget);
        },
        clearActive: function () {
            this.parentContainer.clearActive();
        },
        remove: function (widget) {
            var index = 0;
            for (var i = 0; i < this.widgetsList.length; i++) {
                if (this.widgetsList[i] === widget) {
                    index = i; break;
                }
            }
            this.widgetsList.splice(index, 1);
        },
        sortWidgets: function () {
            this.widgetsList.sort(function (a,b) { return a.$el.index() - b.$el.index(); });
            for (var i = 0; i < this.widgetsList.length; i++) {
                this.widgetsList[i].sequence = this.sequence + i + 1;
            }
        },
        deleteSelf: function () {
            while (this.widgetsList.length > 0) {
                this.widgetsList[0].deleteSelf();
            }
            this.constructor.__super__.deleteSelf.apply(this);
        },
        toDataJSON: function () {
            var models = [], tempModel;
            this.sortWidgets();
            var selfModel = this.constructor.__super__.toDataJSON.apply(this);
            if (!selfModel) { models = false; }
            else {
                models.push(selfModel);
                for (var i = 0; i < this.widgetsList.length; i++) {
                    tempModel = this.widgetsList[i].toDataJSON();
                    if (!tempModel) { models = false; break; }
                    models.push(tempModel);
                }
            }
            return models;
        }
    });
    var WidgetViewsMap = {
        TableField: TableFieldView,
        TabPage: TabPageView
    };

    var WidgetPanelView = Backbone.View.extend({
        template: template($('#tmpl-widgets').html()),
        initialize: function (options) {
            this.$el.html(this.template(options.renderData));

            this.$el.find('.design-widgetitem').draggable({
                helper: function (event) {
                    return $(event.currentTarget).clone().css({
                        'background': 'rgba(0,0,0,.5)',
                        'position': 'fixed'
                    });
                },
                connectToSortable: '.design-formcanvas-body, .design-componentgroup',
                zIndex: 99
            });
        }
    });
    var SettingPanelView = Backbone.View.extend({
        events: {
            'click a.tabitem': 'tabclick',
            'click .iconitem': 'iconclick',
            'focus .has-error': 'removeError'
        },
        initialize: function (options) {
            this.elements = {
                widget: this.$('.design-widgetsettings'),
                form: this.$('.design-formsettings'),
                tabwidget: this.$('.tab-widget'),
                tabform: this.$('.tab-form'),
                tablist: this.$('.tab-list'),
                list: this.$('.design-listsettings')
            };
            this.currentWidget = null;
            this.$settingForm = this.$('.formsetting-form');

            var iconhtml = template($('#tmpl-icons').html())({ icons: iconImages });
            this.elements.form.find('.design-iconselect').html(iconhtml);
            this.$iconId = this.$('#iconid');

            this.initSubview(options);
        },
        initSubview: function (options) {
            this.subview = {};
            this.subview.widgetPanel = new WidgetPanelView({
                el: this.$('.design-listsettings'),
                renderData: { widgets: options.widgets }
            });
        },
        append: function (settingElem) {
            this.elements.widget.append(settingElem);
        },
        iconclick: function (e) {
            this.$('.iconitem').removeClass('selected');
            var iconId = $(e.currentTarget).addClass('selected').data('id');

            this.$iconId.val(iconId);
        },
        tabclick: function (e) {
            var tabname = $(e.target).data('tabname');
            this.tabswitch(tabname);
        },
        tabswitch: function (tabname) {
            this.$('.design-form').hide();
            this.$('.tabitem').removeClass('current');

            this.elements[tabname].show();
            this.elements['tab' + tabname].addClass('current');
        },
        activeForm: function () {
            this.tabswitch('form');
        },
        activeWidget: function (widget) {
            this.tabswitch('widget');
            this.currentWidget = widget;
        },
        hideWidgets: function () {
            this.elements.widget.children().hide();
        },
        inactiveWidget: function () {
            this.currentWidget = null;
            this.hideWidgets();
            this.activeForm();
        },
        bindInitData: function (data) {
            this.$settingForm.find('[name=name]').val(data.formName);
        },
        removeError: function (e) {
            var elem = $(e.currentTarget);
            elem.removeClass('has-error');
            elem.next('.error-note').remove();
        },
        getFormData: function () {
            if (!checkValid(this.$settingForm, validFormOption)) {
                this.activeForm(); return false;
            }
            return this.$settingForm.serializeObject();
        }
    });
    var FormCanvasView = Backbone.View.extend({
        events: {
            'click .design-formcanvas-body': 'blurWidgets'
        },
        initialize: function (options) {
            this.widgetsList = [];
            this.$container = this.$('.design-formcanvas-body');
            this.initDrag();
        },
        initDrag: function () {
            var _this = this;
            this.$container.sortable({
                stop: function (event, ui) {
                    if (ui.item.is('.design-widgetitem')) {
                        var type = ui.item.data('type'),
                            index = ui.item.index();
                        //ui.item.remove();
                        Backbone.trigger('addWidget', type, index);
                    }
                },
                start: function (event, ui) {
                    ui.placeholder.css({
                        float: 'left',
                        width: ui.item.css('width')
                    });
                }
            });
        },
        append: function (widget, index) {
            widget.appendTo(this, index);
            this.widgetsList.push(widget);
        },
        clearActive: function () {
            this.$container.find('.active').removeClass('active');
        },
        remove: function (widget) {
            var index = 0;
            for (var i = 0; i < this.widgetsList.length; i++) {
                if (this.widgetsList[i] === widget) {
                    index = i; break;
                }
            }
            this.widgetsList.splice(index, 1);
        },
        blurWidgets: function () {
            this.clearActive();
            this.trigger('blurWidgets');
        },
        sortWidgets: function () {
            this.widgetsList.sort(function (a,b) { return a.$el.index() - b.$el.index(); });
            for (var i = 0; i < this.widgetsList.length; i++) {
                this.widgetsList[i].sequence = (i + 1) * 100;
            }
        },
        toJSON: function () {
            var models = [];
            this.sortWidgets();
            for (var i = 0; i < this.widgetsList.length; i++) {
                var model = this.widgetsList[i].toDataJSON();
                if (!model) { models = false; break; }
                else if (model instanceof Array) {
                    _.each(model, function (item) {
                        models.push(item);
                    });
                }
                else {
                    models.push(model);
                }
            }
            return models;
        }
    });

    var getParamters = function (id, flowData, jsonObj) {
        var forms = flowData.forms;
        var paramters = {}, isAliasRepeat = false, repeatMessage = '';
        var temp, tempParams;
        f1: for (var i = 0; i < jsonObj.length; i++) {
            temp = jsonObj[i];
            if (temp.useAlias && $.trim(temp.alias).length > 0) {
                for (var key in forms) {
                    if (id && key === id) { continue; }
                    tempParams = forms[key].paramters;
                    if (tempParams[temp.alias]) {
                        isAliasRepeat = true;
                        repeatMessage = '变量"' + temp.alias + '"已被其他表单使用！请返回检查！';
                        break f1;
                    }
                }
                if (paramters[temp.alias]) {
                    isAliasRepeat = true;
                    repeatMessage = '变量"' + temp.alias + '"重复使用！请返回检查！';
                    break f1;
                }

                paramters[temp.alias] = {
                    name: temp.alias,
                    type: (temp.controlId === 'NumberField' || temp.controlId === 'MoneyField') ? 'number' : 'string',
                    controlID: temp.reName
                };
            }
        }

        return { success: !isAliasRepeat, repeatMessage: repeatMessage, paramters: paramters };
    };
    var parseJsonToSetting = function (jsonData) {
        var tempdata,
            jsonDataSelects = [],
            jsonImginfo = '',
            jsonLinkage = '',
            n_isInteger = false,
            n_toSum = false,
            mustnumber = false,
            expression = '',
            expressionData = {};
        if (typeof jsonData.jsonData === 'string' && jsonData.jsonData.length > 0) {
            tempdata = JSON.parse(jsonData.jsonData);
        } else if (typeof jsonData.jsonData === 'object' && jsonData.jsonData !== null) {
            tempdata = jsonData.jsonData;
        }

        if (tempdata) {
            jsonDataSelects = tempdata.options;
            jsonImginfo = JSON.stringify(tempdata.imginfo);
            jsonLinkage = JSON.stringify(tempdata.linkageOption);
            mustnumber = tempdata.linkageOption ? tempdata.linkageOption.mustnumber : false;
            n_isInteger = tempdata.integer;
            n_toSum = tempdata.toSum;
            expression = tempdata.expression;
            expressionData = tempdata.expressionData;
        }

        return _.extend({}, emptySettingData, {
            label: (jsonData.controlId === 'DDDateRangeField' || jsonData.controlId === 'LinkageSelectField')
                ? jsonData.describeName.split(',') : jsonData.describeName,
            placeholder: jsonData.controlId === 'TextNote' ? jsonData.exp.replace(/<br>/g, '\r\n') : jsonData.exp,
            required: jsonData.isRequired === '1',
            type: jsonData.controlId,
            unit: '',
            selects: jsonDataSelects,
            imginfo: jsonImginfo,
            linkage: jsonLinkage,
            mustnumber: mustnumber,
            isInteger: n_isInteger,
            toSum: n_toSum,
            expression: expression,
            expressionData: expressionData,
            canImport: jsonData.isEffective === '1',
            readonly: jsonData.isMerge === '1',
            alias: jsonData.alias,
            useAlias: jsonData.useAlias,
            multi: jsonData.multi,
            timeformat: jsonData.isStorage,
            widgetWidth: jsonData.field1,
            withParam: jsonData.field2 ? jsonData.field2.withParam : '',
            submitType: jsonData.field2 ? jsonData.field2.submitType : ''
        });
    };
    var initPage = function (widgets, flowData, id) {
        //预处理
        _.each(widgets, function (item) {
            if (!widgetConfig[item.id]) {
                item.disabled = true;
            }
        });

        var formCanvas = new FormCanvasView({ el: '.design-formcanvas' });
        var settingPanel = new SettingPanelView({
            el: '.design-settingpanel',
            widgets: widgets
        });

        Backbone.on('addWidget', function (type, index, parentEl) {
            var WidgetClass = WidgetViewsMap[type] || WidgetView;
            var widget = new WidgetClass({
                type: type,
                config: widgetConfig[type],
                settingPanel: settingPanel,
                inTablefield: (!!parentEl) && type !== 'TableField'
            });

            if (parentEl) {
                if (type === 'TableField' || type === 'TabPage') {
                    formCanvas.append(widget);
                }
                else {
                    var elIndex = parentEl.parents('.design-component').index();
                    for (var i = 0; i < formCanvas.widgetsList.length; i++) {
                        var w = formCanvas.widgetsList[i];
                        if (w.getIndex() === elIndex) {
                            w.append(widget, index);
                            break;
                        }
                    }
                }
            }
            else {
                formCanvas.append(widget, index);
            }

            widget.active();
        });
        formCanvas.on('blurWidgets', function () {
            settingPanel.inactiveWidget();
        });

        $(document.body).on('click', function () {
            doWidgetChecking(false);
        });
        Backbone.on('widgetChecked', function (widget) {
            if (couldComputeMap[widget.type] !== true) {
                alert('该控件不能进行数值计算!'); return;
            }
            else if (pageGlobalState['ListeningCompute'].belong === widget) {
                alert('不能添加自身作为变量！'); return;
            }
            //else if (widget.parentContainer !== pageGlobalState['ListeningCompute'].belong.parentContainer) {
            //    alert('明细内和明细外的控件不能参与互相运算!'); return;
            //}

            pageGlobalState['ListeningCompute'].appendComputeWidget(widget);
        });

        //编辑
        if (id != null) {
            var data = flowData.forms[id];
            settingPanel.bindInitData(data);

            var widgetData = data.widgets;
            widgetData.sort(function (a,b) { return a.sequence - b.sequence; });
            var tempContainerMap = {};
            var tempWidgetMap = {}, tempComputeList = [];
            _.each(widgetData, function (item, index) {
                var inTablefield = item.sequence % 100 !== 0;
                var WidgetClass = WidgetViewsMap[item.controlId] || WidgetView;
                var widget = new WidgetClass({
                    type: item.controlId,
                    config: widgetConfig[item.controlId],
                    settingPanel: settingPanel,
                    initData: parseJsonToSetting(item),
                    inTablefield: inTablefield
                });
                tempWidgetMap[item.reName] = widget;
                if (widget.type === 'TableField' || widget.type === 'TabPage') {
                    tempContainerMap['S' + item.sequence] = widget;
                }
                if (widget.type === 'ComputeField' || widget.type === 'ValidField') {
                    tempComputeList.push(widget);
                }

                if (inTablefield) {
                    var parentSeq = Math.floor(item.sequence / 100) * 100;
                    tempContainerMap['S' + parentSeq].append(widget);
                }
                else {
                    formCanvas.append(widget);
                }
            });
            if (tempComputeList.length > 0) {
                _.each(tempComputeList, function (item) {
                    for (var k in item.data.expressionData) {
                        item.setting.appendToParamMap(k, tempWidgetMap[item.data.expressionData[k]]);
                    }
                });
            }

            settingPanel.inactiveWidget();
        }

        window.isSaved = false;
        $('.button-save').on('click', function () {
            var formData = settingPanel.getFormData();
            if (!formData) { return; }
            var jsonObj = formCanvas.toJSON();
            if (!jsonObj) { return; }

            var paramObj = getParamters(id, flowData, jsonObj);
            if (!paramObj.success) {
                alert(paramObj.repeatMessage); return;
            }

            var formJson = {
                formName: formData.name,
                formID: id ? id : ('flowform' + (new Date).getTime()),
                widgets: jsonObj,
                paramters: paramObj.paramters
            };

            flowData.forms[formJson.formID] = formJson;

            sessionHelper.setItem('flowData', flowData);
            sessionHelper.setItem('flow:lasttime', (new Date).getTime());
            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
        });

        $('.button-exit').on('click', function () {
            sessionHelper.setItem('flow:lasttime', (new Date).getTime());
            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
        });
    };

    var run = function () {
        var id = getParam('id');
        var flowData = sessionHelper.getItem('flowData');
        if (id && id.length > 0) {
            initPage(constWidgets, flowData, id);
        }
        else {
            initPage(constWidgets, flowData);
        }
    };

    module.exports = {
        run: run
    };
});
