define(function (require, exports, module) {
    var $ = require('jquery');
    require('jqueryui');
    require('jquery-util');
    var _ = require('underscore');
    var Backbone = require('backbone');
    var confirm = require('component/Confirm');
    var alert = require('component/Alert');
    var template = require('template');
    var ApprovalSettingModal = require('component/ApprovalSettingModal');
    var BatchApprovalModal = require('component/BatchApprovalModal');
    require('plupload');
    require('bootstrap-datetimepicker');
    require('bootstrap-datetimepicker-zh-CN');
    var sessionHelper = require('storageHelper').session;
    var parseQueryString = require('util/parseQueryString').parseQueryString;

    var getParam = function (key) {
        var href = location.href;
        var queryString = href.slice(href.indexOf('?') + 1);
        var params = parseQueryString(queryString);

        return params[key] || '';
    };
    var setUploader = function (options) {
        var uploader = new plupload.Uploader({
            browse_button: options.button[0],
            url: options.url,
            multi_selection: false,
            filters: {
                mime_types: [{
                    title: 'excel文件',
                    extensions: 'xls,xlsx'
                }],
                max_file_size: '10240kb'
            },
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
                    options.callback && options.callback(result);
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

    var globalShowCount = 3;
    var getPageList = function (pageNo, pageCount, showCount) {
        var pages = [];
        if (pageCount <= showCount) {
            var i = 1;
            while (i <= pageCount) { pages.push(i++); }
        }
        else {
            var radius = Math.floor(showCount / 2),
                upLimit = pageNo + radius,
                downLimit = pageNo - radius;
            while (upLimit > pageCount) { upLimit--; downLimit--; }
            while (downLimit < 1) { upLimit++; downLimit++; }
            if (upLimit >= pageCount) { upLimit = pageCount - 1; }
            if (downLimit <= 1) { downLimit = 2; }

            pages.push(1);
            while (downLimit <= upLimit) { pages.push(downLimit++); }
            pages.push(pageCount);
        }
        return pages;
    };
    var PageModel = Backbone.Model.extend({
        defaults: {
            pageNum: 1,
            pageSize: 10,
            total: 0,
            pages: 1
        }
    });
    var pageRender = template($('#tmpl-pagination').html());
    var PageView = Backbone.View.extend({
        template: pageRender,
        events: {
            'click [data-do="pagechange"]': 'pageChange',
            'click [data-do="first"]': 'pageFirst',
            'click [data-do="prev"]': 'pagePrev',
            'click [data-do="next"]': 'pageNext',
            'click [data-do="last"]': 'pageLast',
        },
        initialize: function (options) {
            this.query = options.query;
            this.listenTo(this.model, 'response', this.render);
            this.render();
        },
        pageChange: function (e) {
            var currentPage = this.model.get('pageNum');
            var newPage = parseInt($(e.target).text());
            if (newPage !== currentPage) {
                this.gotoPage(newPage);
            }
        },
        pageFirst: function (e) {
            var currentPage = this.model.get('pageNum');
            if (currentPage > 1) {
                this.gotoPage(1);
            }
        },
        pagePrev: function (e) {
            var currentPage = this.model.get('pageNum');
            if (currentPage > 1) {
                this.gotoPage(currentPage - 1);
            }
        },
        pageLast: function (e) {
            var currentPage = this.model.get('pageNum'),
                pages = this.model.get('pages');
            if (currentPage < pages) {
                this.gotoPage(pages);
            }
        },
        pageNext: function (e) {
            var currentPage = this.model.get('pageNum'),
                pages = this.model.get('pages');
            if (currentPage < pages) {
                this.gotoPage(currentPage + 1);
            }
        },
        gotoPage: function (pageNum) {
            this.query.set('pageNum', pageNum);
        },
        render: function () {
            var json = this.model.toJSON();
            json.pagelist = getPageList(json.pageNum, json.pages, globalShowCount);
            this.$el.html(this.template(json));
        }
    });

    /*
     * 模型 - 审批表单
     */
    var Approval = Backbone.Model.extend({
        defaults: {
            name: '',
            des: '',
            date: '',
            status: 0,
            checked: false
        },
        pending: false,
        destroy: function () {
            if (this.pending) return false;

            $.ajax({
                context: this,
                url: CONTEXT_PATH + '/microApp/customForm/removeApprovel.do',
                type: 'post',
                dataType: 'json',
                data: {
                    id: this.id
                },
                beforeSend: function () {
                    this.pending = true;
                },
                success: function (resp) {
                    _.defaults(resp, {
                        success: false,
                        message: '未知错误'
                    });
                    if (resp.success === true) {
                        this.stopListening();
                        this.trigger('destroy', this, this.collection);
                    } else {
                        alert(resp.message).delay(3);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(textStatus + '：' + errorThrown).delay(3);
                },
                complete: function () {
                    this.pending = false;
                }
            });
        },
        toggleStatus: function () {
            if (this.pending) return false;

            $.ajax({
                context: this,
                url: CONTEXT_PATH + '/microApp/customForm/stopApprovel.do',
                type: 'post',
                dataType: 'json',
                data: {
                    id: this.id
                },
                beforeSend: function () {
                    this.pending = true;
                },
                success: function (resp) {
                    _.defaults(resp, {
                        success: false,
                        message: '未知错误'
                    });
                    if (resp.success === true) {
                        var status = this.get('status');
                        if (status == 1) status =2;
                        else if (status == 2) status = 1;
                        this.set('status', status);
                    } else {
                        alert(resp.message).delay(3);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(textStatus + '：' + errorThrown).delay(3);
                },
                complete: function () {
                    this.pending = false;
                }
            });
        },
        toggleDefault: function () {
            if (this.pending) return false;

            $.ajax({
                context: this,
                url: CONTEXT_PATH + '/users/isDefaultCollection.do',
                type: 'post',
                dataType: 'json',
                data: {
                    typeId: this.id
                },
                beforeSend: function () {
                    this.pending = true;
                },
                success: function (resp) {
                    _.defaults(resp, {
                        success: false,
                        message: '未知错误'
                    });
                    if (resp.success === true) {
                        var isDefault = this.get('isDefault');
                        if (isDefault > 0 && isDefault < 1000) {
                            isDefault = 0;
                        }
                        else {
                            isDefault = 1;
                        }
                        this.set('isDefault', isDefault);
                    } else {
                        alert(resp.message).delay(3);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(textStatus + '：' + errorThrown).delay(3);
                },
                complete: function () {
                    this.pending = false;
                }
            });
        },
        revokeBatch: function () {
            $.ajax({
                context: this,
                url: CONTEXT_PATH + '/approval/cancelApprovals.do',
                type: 'post',
                dataType: 'json',
                data: {
                    typeId: this.id
                },
                beforeSend: function () {
                    this.pending = true;
                },
                success: function (resp) {
                    _.defaults(resp, {
                        success: false,
                        message: '未知错误'
                    });
                    if (resp.success === true) {
                        alert('撤销成功！').delay(3);
                    } else {
                        alert(resp.message).delay(3);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(textStatus + '：' + errorThrown).delay(3);
                },
                complete: function () {
                    this.pending = false;
                }
            });
        },
        getApprovalUser: function () {

        },
        setApprovalUser: function (data, callback) {
            _.defaults(data, {
                code: []
            });
            var defaultApprovalUserIds = data.code;
            if (_.isArray(data.code)) {
                data.code.reverse();
                defaultApprovalUserIds = data.code.join(',');
            }

            $.ajax({
                context: this,
                url: CONTEXT_PATH + '/microApp/customForm/setDefApprovalUsers.do',
                type: 'post',
                dataType: 'json',
                data: {
                    id: this.id,
                    defaultApprovalUserIds: defaultApprovalUserIds,
                    lastUserId: data.lastUserId,
                    lastDealWay: data.lastDealWay
                },
                beforeSend: function () {
                    this.pending = true;
                },
                success: function (resp) {
                    _.defaults(resp, {
                        success: false,
                        message: '未知错误'
                    });
                    var message = resp.message;
                    if (resp.success === true) {
                        message = '保存成功';
                    }
                    alert(message, function () {
                        callback && callback();
                    }).delay(3);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(textStatus + '：' + errorThrown).delay(3);
                },
                complete: function () {
                    this.pending = false;
                }
            });
        },
        checked: function (checked, checktype) {
            this.set('checked', checked);
            this.trigger(checktype, checked);
        }
    });
    /*
     * 集合 - 审批表单
     */
    var ApprovalCollection = Backbone.Collection.extend({
        model: Approval,
        parse: function (resp) {
            return resp;
        },
        filter: function (query) {
            query.rnd = (new Date).getTime();
            this.fetch({
                reset: true,
                data: query
            });
        }
    });
    /*
     * 数据行
     *
     * 监听对象的 remove 和 change 事件更新视图
     */
    var itemRender = template($('#tmpl-approvalItem').html());
    var ApprovalItemView = Backbone.View.extend({
        tagName: 'tr',
        template: itemRender,
        events: {
            'click [data-do="delete"]': 'doDelete',
            'click [data-do="setApprovalUser"]': 'doSetApprovalUser',
            'click [data-do="disable"]': 'doDisable',
            'click [data-do="enable"]': 'doEnable',
            'click [data-do="defaultOn"]': 'doDefaultOn',
            'click [data-do="defaultOff"]': 'doDefaultOff',
            'click [data-do="import"]': 'doImport',
            'click [data-do="batch"]': 'doBatch',
            'click [data-do="revoke"]': 'doRevoke',
            'click [data-do="check"]': 'doCheck',
            'click [data-do="export"]': 'doExport',
            'click [data-do="manageFlow"]': 'doManageFlow',
            'click [data-do="deploy"]': 'doDeploy'
        },
        initialize: function () {
            this.listenTo(this.model, 'remove', this.remove);
            this.listenTo(this.model, 'change', this.render);
        },
        doDelete: function () {
            var model = this.model;
            confirm('确认删除？', function () {
                model.destroy();
            });
        },
        doSetApprovalUser: function () {
            this.model.trigger('setting', this.model);
        },
        doDisable: function () {
            this.model.toggleStatus();
        },
        doEnable: function () {
            this.model.toggleStatus();
        },
        doDefaultOn: function () {
            this.model.toggleDefault();
        },
        doDefaultOff: function () {
            this.model.toggleDefault();
        },
        doImport: function () {
            this.model.trigger('import', this.model);
        },
        doBatch: function () {
            this.model.trigger('batchApproval', this.model);
        },
        doExport: function () {
            this.model.trigger('exportReport', this.model);
        },
        doRevoke: function () {
            var model = this.model;
            confirm('确认撤销？', function () {
                model.revokeBatch();
            });
        },
        doCheck: function (e) {
            var checked = $(e.target).prop('checked');
            this.model.checked(checked, 'checkone');
        },
        doManageFlow: function (e) {
            var id = this.model.get('id');
            var edition = this.model.get('scene') == '3' ? '' : '-e4';
            sessionHelper.removeItem('flowData');
            location.href = CONTEXT_PATH + '/flow' + edition + '/flowManager.do?flowid=' + id + '&wyyId=' + this.model.get('wyyId');
        },
        doDeploy: function () {
            var id = this.model.get('id');
            $.ajax({
                url: CONTEXT_PATH + '/activiti/deployActivitiById.do',
                type: 'post',
                dataType: 'json',
                data: { id: id },
                success: function (res) {
                    if (res.success) {
                        alert('部署成功');
                    }
                    else {
                        alert(res.message || '部署失败');
                    }
                },
                error: function () {
                    alert('部署错误');
                }
            });
        },
        render: function () {
            var markup = this.template(this.model.toJSON());
            this.$el.html(markup);
            return this;
        }
    });
    /*
     * 表格
     *
     * 监听集合的 reset 事件更新视图
     */
    var ApprovalTable = Backbone.View.extend({
        events: {
            'click [role="checkall"]': 'checkAll'
        },
        initialize: function () {
            this.listenTo(this.collection, 'reset', this.reset);
            this.listenTo(this.collection, 'checkone', this.allCheck);
            this.cacheEls();
        },
        addOne: function (model, collection, options) {
            var itemView = new ApprovalItemView({
                model: model
            });
            this.$items.append(itemView.render().el);
        },
        reset: function (collection, options) {
            var previousModels = options.previousModels;
            _.each(previousModels, function (model) {
                model.trigger('remove');
            });

            this.$items.empty();

            collection.each(function (model, index) {
                model.set({
                    $index: index
                });
                this.addOne(model, collection);
            }, this);
        },
        checkAll: function () {
            var checked = this.$checkAll.prop('checked');
            this.collection.each(function (model) {
                model.checked(checked, 'checkall');
            });
            checked ? this.$checkAll.parent().addClass('checked') : this.$checkAll.parent().removeClass('checked');
        },
        allCheck: function () {
            var allChecked = this.collection.every(function (model) {
                    return model.get('checked') === true;
                });
            this.$checkAll.prop('checked', allChecked);
            allChecked ? this.$checkAll.parent().addClass('checked') : this.$checkAll.parent().removeClass('checked');
        },
        cacheEls: function () {
            this.$items = this.$('[role="items"]');
            this.$checkAll = this.$('[role="checkall"]');
        }
    });

    var newApprovalRender = template($('#tmpl-createDialog').html());
    var NewApprovalDialog = Backbone.View.extend({
        template: newApprovalRender,
        className: 'modal-approval-create modal',
        events: {
            'change #version-id': 'verChange',
            'click [data-do=ok]': 'targetTo'
        },
        initialize: function (options) {
            //this.$el.on('hidden.bs.modal', _.bind(this.onHidden, this));
            this.wyyId = options.wyyId;
            this.render();
        },
        render: function () {
            var data = this.model.toJSON();
            var markup = this.template(data);
            this.$el.html(markup).appendTo(document.body);
            this.cacheEls();
            return this;
        },
        cacheEls: function () {
            this.$select = this.$('#version-id');
            this.$note = this.$('#version-note');
        },
        verChange: function (e) {
            var id = this.$select.val(), i = 0;
            var versions = this.model.get('versions');
            for (i = 0; i < versions.length; i++) {
                if (versions[i].id === id) break;
            }
            this.$note.html(versions[i].note);
        },
        targetTo: function () {
            var id = this.$select.val(), i = 0;
            var versions = this.model.get('versions');
            for (i = 0; i < versions.length; i++) {
                if (versions[i].id === id) break;
            }

            var type = $('#inputApprovalType').val();
            location.href = versions[i].url + '?mostTypeKey=' + type + '&wyyId=' + this.wyyId;
        },
        show: function () {
            this.$el.modal('show');
        },
        hide: function () {
            this.$el.modal('hide');
        }
    });

    var exportReportRender = template($('#tmpl-exportReport').html());
    var ExportReportModal = Backbone.View.extend({
        template: exportReportRender,
        className: 'modal-export-report modal',
        initialize: function (options) {
            this.$el.on('hidden.bs.modal', _.bind(this.onHidden, this));
            this.render();
        },
        render: function () {
            var data = this.model.toJSON();
            var markup = this.template(data);
            this.$el.html(markup).appendTo(document.body);
            this.initDatePicker();
            return this;
        },
        initDatePicker: function () {
            var defaultSetting = {
                    format: 'yyyy-mm-dd',
                    language: 'zh-CN',
                    minView: 2
                };

            var $applyStartTimePicker = this.$('#applyStartTimePicker');
            var $applyEndTimePicker = this.$('#applyEndTimePicker');
            var $applyStartTime = this.$('#applyStartTime');
            var $applyEndTime = this.$('#applyEndTime');

            $applyStartTimePicker.datetimepicker(defaultSetting);
            $applyEndTimePicker.datetimepicker(defaultSetting);

            $applyStartTimePicker.on('hide', function (event) {
                var val = $.trim($(this).val());
                if (val != '') {
                    $applyStartTime.val(val + ' 00:00:00');
                    // $applyEndTime.datetimepicker('setStartDate', val).datetimepicker('update');
                }
            });
            $applyEndTimePicker.on('hide', function (event) {
                var val = $.trim($(this).val());
                if (val != '') {
                    $applyEndTime.val(val + ' 23:59:59');
                    // $applyStartTime.datetimepicker('setEndDate', val).datetimepicker('update');
                }
            });
        },
        show: function () {
            this.$el.modal('show');
        },
        hide: function () {
            this.$el.modal('hide');
        },
        onHidden: function () {
            this.reset();
        },
        reset: function () {
            //_.each(this.subviews, function (view) {
            //    view.remove();
            //});
            this.remove();
        }
    });

    var createLeftMenu = function (wyyId) {
        $.ajax({
            url: CONTEXT_PATH + '/microApp/customForm/findWyy.do',
            dataType: 'json',
            type: 'get',
            success: function (res) {
                if (res.success) {
                    var html = template($('#tmpl-leftMenu').html())({
                        wyyId: wyyId,
                        list: res.model
                    });
                    $('.menu-spadmin .list-group').html(html);
                }
                else {
                    alert(res.message || '读取菜单失败！');
                }
            },
            error: function () {
                alert('读取菜单出错！');
            }
        });
    };
    function run () {
        var wyyId = getParam('wyyId');
        //if (wyyId === '') wyyId = 'wyy0001';
        createLeftMenu(wyyId);
        // 审批集合
        var approvalList = new ApprovalCollection();

        var defaultQuery = {
            pageNum: 1,
            pageSize: 10,
            id: ''
        };
        var searchQuery = new Backbone.Model(defaultQuery);
        var pageModel = new PageModel();

        approvalList.url = CONTEXT_PATH + '/microApp/customForm/findApprovel.do?wyyId=' + wyyId;
        approvalList.parse = function (resp) {
            var data = [];
            if (_.isObject(resp) && resp.success) {
                data = resp.model.list;
                pageModel.set({
                    pageNum: resp.model.pageNum,
                    pageSize: resp.model.pageSize,
                    pages: resp.model.pages < 1 ? 1 : resp.model.pages,
                    total: resp.model.total
                });
                pageModel.trigger('response');
            }
            _.each(data, function (item) { item.wyyId = wyyId; });
            return data;
        };

        // 审批表格

        var approvalTable = new ApprovalTable({
            el: '#table-approval',
            collection: approvalList
        });
        var pageView = new PageView({
            el: '.panel-footer',
            model: pageModel,
            query: searchQuery
        });

        var approvalCreater = new NewApprovalDialog({
            wyyId: wyyId,
            model: new Backbone.Model({
                versions: [
                    { id: '1', name: '场景1', note: '适用于表单控件之间无逻辑运算和关联关系的流程创建。', url: CONTEXT_PATH + '/microApp/customForm/input-e1.do' },
                    { id: '2', name: '场景2', note: '可以配置控件之间的运算和逻辑关系，并且对单个控件进行条件限制，也可以个性化初始员工数据。', url: CONTEXT_PATH + '/microApp/customForm/input-e2.do' },
                    { id: '3', name: '场景3', note: '企业管理员可个性化配置企业流程的表单、流转，根据角色、用户组设置流程节点的审批人。', url: CONTEXT_PATH + '/flow/flowManager.do' },
                    { id: '4', name: '场景4(PC端)', note: '', url: CONTEXT_PATH + '/flow-e4/flowManager.do' }
                ]
            })
        });
        // 类型筛选
        var $inputUrl = $('#input-url');
        //var $appTypeList = $('#approvalTypeList');
        var $approvalType = $('#inputApprovalType');

        $inputUrl.on('click', function (e) {
            approvalCreater.show();
        });

        $approvalType.on('change', function () {
            var type = $(this).val();
            searchQuery.set(_.extend({}, defaultQuery, { id: type }));
        });
        searchQuery.on('change', function (model, option) {
            approvalList.filter(model.toJSON());
        });

        /*$appTypeList.on('click', 'a', function (e) {
            var $el = $(this);
            var type = $el.data('id');
            approvalList.filter({
                id: type,
                rnd: (new Date).getTime()
            });

            $appTypeList.find('.active').removeClass('active');
            $el.parent().addClass('active');
        });*/

        var type = $approvalType.val();
        approvalList.filter({
            id: type,
            rnd: (new Date).getTime()
        });

        // 设置审批人操作
        approvalList.on('setting', function (model) {
            var modal = new ApprovalSettingModal({
                model: model
            });
            modal.show();
        });

        //var batchModal = new BatchApprovalModal();
        approvalList.on('batchApproval', function (model) {
            var modal = new BatchApprovalModal({
                model: model
            });
            modal.show();
        });
        approvalList.on('exportReport', function (model) {
            var modal = new ExportReportModal({
                model: model
            });
            modal.show();
        });

        var uploadOption = {
            url: CONTEXT_PATH + '/microApp/customForm/importData.do',
            button: $('#import-trigger'),
            callback: function (res) {
                alert('导入成功！').delay(5);
            }
        };
        var uploader = setUploader(uploadOption);
        approvalList.on('import', function (model) {
            var option = uploader.getOption('multipart_params');
            option.id = model.get('id');
            uploader.setOption('multipart_params', option);

            $('#import-trigger').click();
        });
    }

    exports.run = run;
});
