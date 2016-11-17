define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore'),
        Backbone = require('backbone');
    var template = require('template');
    var confirm = require('component/Confirm');
    var alert = require('component/Alert');
    require('jqueryui');
    require('plupload');

    var urls = {
        save: 'customform.do',
        edit: 'editFrom.do',
        get: 'getApprovalById.do',
        manager: 'manager.do',
        mostType: 'findMostType.do',
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
        { id: 'PictureNote', code: '', des: '说明文字', en_name: 'PictureNote', name: '说明图片', type: '说明图片' }
        //{ id: 'LinkageSelectField', code: '', des: '联动选择框', en_name: 'LinkageSelectField', name: '联动选择框', type: '联动选择框' }
    ];
    var iconImages = ['icon_bx', 'icon_bxiao', 'icon_byj', 'icon_cc', 'icon_dh',
        'icon_fk', 'icon_htsp', 'icon_hysp', 'icon_jb', 'icon_kdjs',
        'icon_lxsq', 'icon_qj', 'icon_rb', 'icon_rbb', 'icon_sk',
        'icon_sp', 'icon_syzz', 'icon_ty', 'icon_wc', 'icon_wply',
        'icon_wprk', 'icon_wpsg', 'icon_xwbl', 'icon_yb', 'icon_ybb',
        'icon_ycsq', 'icon_ywlxd', 'icon_yz', 'icon_zb', 'icon_zbb'];

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
        required: '',
        type: '',
        unit: '',
        selects: [],
        imginfo: ''
    };
    var emptySettingJson = {
        describeName: '',      //名称
        isRequired: '',        //必填
        reName: '',            //id + 序号
        sequence: '',          //序号
        controlId: '',         //id
        exp: '',               //说明文字
        value: '',             //同json_data
        jsonData: ''           //选择控件 选项数据json
    };
    //控件配置表
    var widgetConfig = {
        'TextField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'required'],
            defaultData: {
                label: '单行输入框',
                placeholder: '请输入',
                required: false
            }
        },
        'TextareaField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'required'],
            defaultData: {
                label: '多行输入框',
                placeholder: '请输入',
                required: false
            }
        },
        'NumberField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'required'], //'unit', 没有对应字段注释掉
            defaultData: {
                label: '数字输入框',
                placeholder: '请输入',
                unit: '',
                required: false
            }
        },
        'DDSelectField': {
            template: 'normal',
            settings: ['label', 'options', 'required'],
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
            settings: ['label', 'options', 'required'],
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
            settings: ['label', 'timeformat', 'required'],
            defaultData: {
                label: '日期',
                placeholder: '请选择',
                required: false,
                icon: 'enter',
                timeformat: '0'
            }
        },
        'DDDateRangeField': {
            template: 'datarange',
            settings: ['label', 'timeformat', 'required'],
            defaultData: {
                label: ['开始时间', '结束时间'],
                placeholder: '请选择',
                required: false,
                timeformat: '0'
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
            settings: ['textnote'],
            defaultData: {
                placeholder: '请输入说明文字'
            }
        },
        'MoneyField': {
            template: 'normal',
            settings: ['label', 'placeholder', 'required'],
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
            { validItem: 'required', message: '审批名称不能为空' }
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
            var settingRender = getSettingRender(options.type, options.settings);
            this.$el.html(settingRender(options.data));

            if (options.settings.indexOf('options') >= 0) {
                this.$selectList = this.$('.selection-list');
            }
            if (options.settings.indexOf('picture') >= 0) {
                this.initPicUpload();
            }
        },
        change: function (e) {
            var formData = this.$el.serializeArray();
            this.trigger('change', formData);
        },
        changeSelects: function (e) {
            var elem = $(e.currentTarget);
            var action = elem.data('action');
            var length = this.$selectList.children().length;
            if (action === 'del') {
                if (length <= 1) { alert('至少需要1个选项！'); }
                else { elem.parent().remove(); }
            }
            else {
                if (length >= 50) { alert('最多只能设置50个选项!'); }
                else {
                    var isEmpty = elem.is('.empty-plus');
                    elem.parent().after(selectRender({ index: length + (isEmpty ? 0 : 1) }));
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
        valid: function () {
            return checkValid(this.$el, validOption);
        },
        removeError: function (e) {
            var elem = $(e.currentTarget);
            elem.removeClass('has-error');
            elem.next('.error-note').remove();
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
            this.defaultData = _.extend({}, emptySettingData, options.config.defaultData);
            this.data = _.extend({}, this.defaultData, options.initData || {});
            this.$el.addClass('design-component-' + options.type.toLowerCase());
            this.settingPanel = options.settingPanel;

            this.widgetRender = getWidgetRender(options.config.template);
            this.$el.html(this.widgetRender(this.data));
            this.setting = new SettingView({
                data: this.data,
                type: this.type,
                settings: options.config.settings
            });
            this.settingPanel.append(this.setting.$el);

            this.listenTo(this.setting, 'change', this.change);
        },
        click: function (e) {
            var isRemove = $(e.target).is('.design-remove');
            if (isRemove) { this.deleteSelf(); }
            else { this.active(); }

            e.stopPropagation();
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

            this.data = _.extend({}, this.defaultData, newData);
            this.$el.html(this.widgetRender(this.data));
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
        toDataJSON: function (sequence) {
            if (!this.setting.valid()) {
                this.active(true);
                return false;
            }
            else {
                var jsonData = '';
                if (this.type === 'DDSelectField' || this.type === 'DDMultiSelectField') {
                    jsonData = {
                        options: typeof this.data.selects === 'string' ? [this.data.selects] : this.data.selects
                    };
                }
                else if (this.type === 'PictureNote') {
                    jsonData = {
                        imginfo: this.data.imginfo.length > 0 ? JSON.parse(this.data.imginfo) : ''
                    };
                }

                var model = _.extend({}, emptySettingJson, {
                    describeName: this.data.label instanceof Array ? this.data.label.join(',') : this.data.label,
                    isRequired: !!this.data.required ? '1' : '0',
                    isStorage: this.data.timeformat,
                    reName: this.type + sequence,
                    sequence: sequence,
                    controlId: this.type,
                    exp: this.type === 'TextNote' ? this.data.placeholder.replace(/(\r\n|\n)/ig, '<br>') : this.data.placeholder,
                    value: '', //jsonData,
                    jsonData: jsonData
                });
                return model;
            }
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
        },
        deleteSelf: function () {
            while (this.widgetsList.length > 0) {
                this.widgetsList[0].deleteSelf();
            }
            this.constructor.__super__.deleteSelf.apply(this);
        },
        toDataJSON: function (sequence) {
            var models = [], tempModel;
            this.sortWidgets();
            var selfModel = this.constructor.__super__.toDataJSON.apply(this, [sequence]);
            if (!selfModel) { models = false; }
            else {
                models.push(selfModel);
                for (var i = 0; i < this.widgetsList.length; i++) {
                    tempModel = this.widgetsList[i].toDataJSON(sequence + i + 1);
                    if (!tempModel) { models = false; break; }
                    models.push(tempModel);
                }
            }
            return models;
        }
    });
    var WidgetViewsMap = {
        TableField: TableFieldView
    };

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
                tabform: this.$('.tab-form')
            };
            this.currentWidget = null;
            this.$settingForm = this.$('.formsetting-form');

            var iconhtml = template($('#tmpl-icons').html())({ icons: iconImages });
            this.elements.form.find('.design-iconselect').html(iconhtml);
            this.$iconId = this.$('#iconid');
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
            this.$settingForm.find('[name=name]').val(data.name);
            this.$settingForm.find('[name=des]').val(data.des);
            this.$settingForm.find('.iconitem').each(function (index, iconitem) {
                if ($(iconitem).data('id') === data.icon) {
                    $(iconitem).click(); return false;
                }
            });

            this.$settingForm.find('#mostTypeKey').val(data.approvalMostTypeId);
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
            return this.$settingForm.serializeArray();
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
        },
        toJSON: function () {
            var models = [];
            this.sortWidgets();
            for (var i = 0; i < this.widgetsList.length; i++) {
                var model = this.widgetsList[i].toDataJSON((i + 1) * 100);
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
    var WidgetPanelView = Backbone.View.extend({
        //events: {
        //    'click .design-widgetitem': 'addWidget'
        //},
        template: template($('#tmpl-widgets').html()),
        initialize: function (options) {
            this.$body = this.$('.design-panel-body');
            this.$body.html(this.template(options.renderData));

            this.$body.find('.design-widgetitem').draggable({
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
        //addWidget: function (e) {
        //    var type = $(e.currentTarget).data('type');
        //    Backbone.trigger('addWidget', type);
        //}
    });

    var parseJsonToSetting = function (jsonData) {
        var tempdata,
            jsonDataSelects = [],
            jsonImginfo = '';
        if (typeof jsonData.jsonData === 'string' && jsonData.jsonData.length > 0) {
            tempdata = JSON.parse(jsonData.jsonData);
            jsonDataSelects = tempdata.options;
            jsonImginfo = JSON.stringify(tempdata.imginfo);
        }

        return _.extend({}, emptySettingData, {
            label: jsonData.controlId === 'DDDateRangeField' ? jsonData.describeName.split(',') : jsonData.describeName,
            placeholder: jsonData.controlId === 'TextNote' ? jsonData.exp.replace(/<br>/g, '\r\n') : jsonData.exp,
            required: jsonData.isRequired === '1',
            type: jsonData.controlId,
            unit: '',
            selects: jsonDataSelects,
            imginfo: jsonImginfo,
            timeformat: jsonData.isStorage
        });
    };
    var initPage = function (widgets, data) {
        //预处理
        _.each(widgets, function (item) {
            if (!widgetConfig[item.id]) {
                item.disabled = true;
            }
        });

        var formCanvas = new FormCanvasView({ el: '.design-formcanvas' });
        var settingPanel = new SettingPanelView({ el: '.design-settingpanel' });
        var widgetPanel = new WidgetPanelView({
            el: '.design-widgetspanel',
            renderData: { widgets: widgets }
        });

        Backbone.on('addWidget', function (type, index, parentEl) {
            var WidgetClass = WidgetViewsMap[type] || WidgetView;
            var widget = new WidgetClass({
                type: type,
                config: widgetConfig[type],
                settingPanel: settingPanel
            });

            /*if (settingPanel.currentWidget && settingPanel.currentWidget.type === 'TableField'
                && widget.type !== 'TableField') {
                settingPanel.currentWidget.append(widget, index);
            }
            else if (settingPanel.currentWidget && settingPanel.currentWidget.parentContainer.type === 'TableField'
                && widget.type !== 'TableField') {
                settingPanel.currentWidget.parentContainer.append(widget, index);
            }
            else {
                formCanvas.append(widget, index);
            }*/
            if (parentEl) {
                if (type === 'TableField') {
                    formCanvas.append(widget);
                }
                else {
                    var elIndex = parentEl.parents('.design-component-tablefield').index();
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

        //编辑
        if (data != null) {
            settingPanel.bindInitData(data);

            var widgetData = data.control;
            widgetData.sort(function (a,b) { return a.sequence - b.sequence; });
            var tempContainerMap = {};
            _.each(widgetData, function (item, index) {
                var WidgetClass = WidgetViewsMap[item.controlId] || WidgetView;
                var widget = new WidgetClass({
                    type: item.controlId,
                    config: widgetConfig[item.controlId],
                    settingPanel: settingPanel,
                    initData: parseJsonToSetting(item)
                });
                if (widget.type === 'TableField') {
                    tempContainerMap['S' + item.sequence] = widget;
                }

                if (item.sequence % 100 !== 0) {
                    var parentSeq = Math.floor(item.sequence / 100) * 100;
                    tempContainerMap['S' + parentSeq].append(widget);
                }
                else {
                    formCanvas.append(widget);
                }
            });
            settingPanel.inactiveWidget();
        }

        window.isSaved = false;
        $('.button-save').on('click', function () {
            var formData = settingPanel.getFormData();
            if (!formData) { return; }
            var jsonObj = formCanvas.toJSON();
            if (!jsonObj) { return; }

            var url = urls.save;
            if (data != null) {
                formData.push({ name: 'id', value: data.id });
                url = urls.edit;
            }
            formData.push({ name: 'control', value: JSON.stringify(jsonObj) });
            formData.push({ name: 'scene', value: 1 });
            //formData.push({ name: 'mostTypeKey', value: mostTypeKey });

            $.ajax({
                url: url,
                type: 'post',
                dataType: 'json',
                data: formData,
                success: function (res) {
                    if (res.success) {
                        alert('保存成功！', function () {
                            window.onbeforeunload = null;
                            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
                        });
                    }
                    else {
                        alert('发生错误，保存失败！');
                    }
                },
                error: function () {
                    alert('发生错误，保存失败！');
                }
            });
        });
        $('.button-exit').on('click', function () {
            window.onbeforeunload = null;
            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
        });

        window.onbeforeunload = function (e) {
            return '您还没有保存，确定要离开该页面吗？';
        };
    };

    var getMostTypeKey = function (mostTypeKey, wyyId, callback) {
        $.ajax({
            url: urls.mostType,
            type: 'get',
            dataType: 'json',
            data: {
                rnd: (new Date).getTime(),
                wyyId: wyyId
            },
            success: function (res) {
                if (res.success) {
                    var html = _.map(res.model, function (item, index) {
                        var select = mostTypeKey !== '' ? item.id === mostTypeKey : index === 0;
                        return '<option value="' + item.id + '" ' + (select ? 'select' : '') + '>' + item.name + '</option>';
                    }).join('');
                    $('#mostTypeKey').html(html);
                    callback && callback();
                }
                else {
                    alert('无法读取审批类型！',function () {
                        location.href = urls.manager + '?wyyId=' + getParam('wyyId');
                    }).delay(3);
                }
            },
            error: function (res) {
                alert('无法读取审批类型！',function () {
                    location.href = urls.manager + '?wyyId=' + getParam('wyyId');
                }).delay(3);
            }
        });
    };
    var run = function () {
        var id = getParam('id');
        var mostTypeKey = getParam('mostTypeKey'),
            wyyId = getParam('wyyId');
        getMostTypeKey(mostTypeKey, wyyId, function () {
            if (id && id.length > 0) {
                $.ajax({
                    url: urls.get,
                    type: 'get',
                    dataType: 'json',
                    data: { id: id, rnd: (new Date).getTime() },
                    success: function (res) {
                        if (res.success) {
                            initPage(constWidgets, res.model);
                        }
                        else {
                            alert('无法读取该审批！', function () {
                                location.href = urls.manager + '?wyyId=' + getParam('wyyId');
                            }).delay(3);
                        }
                    },
                    error: function (res) {
                        alert('无法读取该审批！', function () {
                            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
                        }).delay(3);
                    }
                });
            }
            else {
                initPage(constWidgets, null);
            }
        });
    };

    module.exports = {
        run: run
    };
});
