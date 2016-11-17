define(function (require, exports, module) {
    var $ = require('jquery');
    require('jqueryui');
    require('jquery-util');
    var _ = require('underscore');
    var Backbone = require('backbone');
    require('jquery-validate');
    require('plupload');

    var confirm = require('component/Confirm');
    var alert = require('component/Alert');
    var template = require('template');
    var parseQueryString = require('util/parseQueryString').parseQueryString;
    var BatchApprovalModal = require('component/BatchApprovalModal');

    var getParam = function (key) {
        var href = location.href;
        var queryString = href.slice(href.indexOf('?') + 1);
        var params = parseQueryString(queryString);

        return params[key] || '';
    };

    var urls = {
        findByTasks: CONTEXT_PATH + '/task/findByTasks.do',
        delete: CONTEXT_PATH + '/task/delete.do',
        saveTask: CONTEXT_PATH + '/task/addTask.do',
        updateTask: CONTEXT_PATH + '/task/edit.do',
        checkName: CONTEXT_PATH + '/task/checkName.do',

        importUserInfo: CONTEXT_PATH + '/approval/importUserInfo.do',
        importData: CONTEXT_PATH + '/microApp/customForm/importData.do',
        batchInsertApproval: CONTEXT_PATH + '/approval/batchInsertApproval.do',
        cancelApprovals: CONTEXT_PATH + '/approval/cancelApprovals.do',
        revokeApprovals: CONTEXT_PATH + '/task/revoke.do'
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

    var taskRender = template($('#tmpl-taskItem').html());
    var taskModalRender = template($('#tmpl-taskModal').html());

    /*
	 * Task Model & Collection of TaskModels
	 */
    var TaskModel = Backbone.Model.extend({
        pending: false,
        destroy: function () {
            if (this.pending) return false;

            $.ajax({
                context: this,
                url: urls.delete,
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
        }
    });
    var TaskCollection = Backbone.Collection.extend({
        model: TaskModel,
        url: urls.findByTasks,
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
	 * Task Row View & Table View
	 */
    var TaskView = Backbone.View.extend({
        tagName: 'tr',
        template: taskRender,
        events: {
            'click [data-do="edit"]': 'doEdit',
            'click [data-do="remove"]': 'doRemove',
            'click [data-do="import-user"]': 'doImportUser',
            'click [data-do="import-data"]': 'doImportData',
            'click [data-do="publish"]': 'doPublish',
            'click [data-do="undo"]': 'doUndo',
            'click [data-do="recovery"]': 'doRecovery',
            'click [data-do="report"]': 'doReport'
        },
        initialize: function () {
            this.listenTo(this.model, 'remove', this.remove);
            this.listenTo(this.model, 'change', this.render);
        },
        render: function () {
            var markup = this.template(this.model.toJSON());
            this.$el.html(markup);
            return this;
        },
        doEdit: function () {
            this.model.trigger('edit', this.model);
        },
        doRemove: function () {
            var model = this.model;
            confirm('确认删除？', function () {
                model.destroy();
            });
        },
        doImportUser: function () {
            this.model.trigger('importuser', this.model);
        },
        doImportData: function () {
            this.model.trigger('importdata', this.model);
        },
        doPublish: function () {
            this.model.trigger('publish', this.model);
        },
        doUndo: function () {
            this.model.trigger('undo', this.model);
        },
        doRecovery: function () {
            var model = this.model;
            confirm('确定回收所有流程？', function () {
                model.trigger('revoke', model);
            });
        },
        doReport: function () {
            // TODO 导出报表
        }
    });
    var TaskTableView = Backbone.View.extend({
        initialize: function () {
            this.listenTo(this.collection, 'reset', this.reset);
            this.cacheEls();
        },
        cacheEls: function () {
            this.$items = this.$('[role="items"]');
        },
        addOne: function (model, collection, options) {
            var itemView = new TaskView({
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
    });
    /**
	 * Pagenation
	 */
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

    var TaskModal = Backbone.View.extend({
        template: taskModalRender,
        className: 'modal',
        events: {
            'click [data-do="save"]': 'doSave'
        },
        initialize: function () {
            this.$el.on('hidden.bs.modal', _.bind(this.hide, this));
            this.$el.appendTo(document.body);
        },
        render: function (data) {
            var markup = this.template(data);
            this.$el.html(markup);

            this.$('form[role=edit-task]').validate({
                errorClass: 'error-valid',
                rules: {
                    'taskName': {
                        required: true,
                        maxlength: 50
                    }
                },
                messages: {
                    'taskName': {
                        required: '任务名称为必填项',
                        maxlength: '最多只能输入50个字符'
                    }
                }
            });
            return this;
        },
        doCheckName: function (modelJSON) {
            var dtd = $.Deferred();
            $.ajax({
                url: urls.checkName,
                type: 'post',
                dataType: 'json',
                data: modelJSON,
                success: function (res) {
                    if (res.success) {
                        dtd.resolve();
                    } else {
                        alert(res.message || '未知错误').delay(3);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert(textStatus + '：' + errorThrown).delay(3);
                }
            });
            return dtd;
        },
        doSave: function () {
            var _this = this;
            if (!this.$('form[role=edit-task]').valid()) { return; }
            var modelJSON = this.$('form[role=edit-task]').serializeObject();
            $.when(this.doCheckName(modelJSON)).done(function () {
                $.ajax({
                    url: _this.isEdit ? urls.updateTask : urls.saveTask,
                    type: 'post',
                    dataType: 'json',
                    data: modelJSON,
                    success: function (res) {
                        if (res.success) {
                            alert('保存成功!', function () {
                                _this.hide();
                                _this.trigger('modify', {
                                    success: true,
                                    edit: _this.isEdit,
                                    msg: '',
                                    model: modelJSON
                                });
                            }).delay(3);
                        } else {
                            alert(res.message || '未知错误').delay(3);
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        alert(textStatus + '：' + errorThrown).delay(3);
                    }
                });
            });
        },
        show: function (data) {
            this.isEdit = data.edit;
            this.render(data).$el.modal('show');
        },
        hide: function () {
            this.$el.modal('hide');
        }
    });

    var run = function () {
        var approvalTypeId = getParam('approvalTypeId');
        var defaultQuery = {
            pageNum: 1,
            pageSize: 10,
            approvalTypeId: approvalTypeId
        };
        var searchQuery = new Backbone.Model();
        var pageModel = new PageModel();

        var taskList = new TaskCollection();
        taskList.parse = function (resp) {
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
            // _.each(data, function (item) { item.wyyId = wyyId; });
            return data;
        };

        var taskTable = new TaskTableView({
            el: '#table-task',
            collection: taskList
        });
        var pagenation = new PageView({
            el: '.panel-footer',
            model: pageModel,
            query: searchQuery
        });

        searchQuery.on('change', function (model, option) {
            taskList.filter(model.toJSON());
        });
        searchQuery.set(defaultQuery);
        // 新增&编辑
        var taskEditModal = new TaskModal();
        $('#add-task').on('click', function () {
            taskEditModal.show({
                id: '',
                taskName: '',
                edit: false,
                approvalTypeId: approvalTypeId
            });
        });
        Backbone.listenTo(taskEditModal, 'modify', function (result) {
            if (result.success) {
                searchQuery.trigger('change', searchQuery);
            }
        });
        taskList.on('edit', function (model) {
			var data = model.toJSON();
			data.edit = true;
			taskEditModal.show(data);
        });
        // 导入人员
        var userImportOption = {
            url: urls.importUserInfo,
            button: $('#user-import-trigger'),
            callback: function (res) {
                alert('导入成功！', function () {
                    searchQuery.trigger('change', searchQuery);
                }).delay(5);
            }
        };
        var userImporter = setUploader(userImportOption);
        taskList.on('importuser', function (model) {
            var option = userImporter.getOption('multipart_params');
            option.taskId = model.get('id');
            option.typeId = approvalTypeId;
            userImporter.setOption('multipart_params', option);

            $('#user-import-trigger').click();
        });
        // 导入数据
        var dataImportOption = {
            url: urls.importData,
            button: $('#data-import-trigger'),
            callback: function (res) {
                if (res.success) {
                    alert('导入成功！', function () {
                        searchQuery.trigger('change', searchQuery);
                    }).delay(5);
                }
                else {
                    alert('发生错误！');
                }
            }
        };
        var dataImporter = setUploader(dataImportOption);
        taskList.on('importdata', function (model) {
            var option = dataImporter.getOption('multipart_params');
            option.taskId = model.get('id');
            option.id = approvalTypeId;
            dataImporter.setOption('multipart_params', option);

            $('#data-import-trigger').click();
        });
        // 撤销
        var undoOption = {
            url: urls.revokeApprovals,
            button: $('#undo-trigger'),
            callback: function (res) {
                if (res.success) {
                    alert(res.message || '操作成功', function () {
                        searchQuery.trigger('change', searchQuery);
                    }).delay(5);
                }
                else {
                    alert('发生错误！');
                }
            }
        };
        var undoImporter = setUploader(undoOption);
        taskList.on('undo', function (model) {
            var option = undoImporter.getOption('multipart_params');
            option.taskId = model.get('id');
            undoImporter.setOption('multipart_params', option);

            $('#undo-trigger').click();
        });
        // 回收
        taskList.on('revoke',function (model) {
            $.ajax({
                url: urls.cancelApprovals,
                data: {
                    taskId: model.get('id'),
                },
                type: 'post',
                dataType: 'json',
                success: function (res) {
                    if (res.success) {
                        alert(res.message || '操作成功', function () {
                            searchQuery.trigger('change', searchQuery);
                        });
                    }
                    else {
                        alert(res.message);
                    }
                },
                error: function () {
                    alert('发生错误！');
                }
            });
        });
        // 发布
        taskList.on('publish', function (model) {
            var modal = new BatchApprovalModal({
                model: model
            });
            modal.show();
        });
        taskList.on('submit-publish', function (model, startUserId, modal) {
            $.ajax({
                url: urls.batchInsertApproval,
                data: {
                    taskId: model.get('id'),
                    typeId: approvalTypeId,
                    startUserID: startUserId
                },
                type: 'post',
                dataType: 'json',
                success: function (res) {
                    if (res.success) {
                        alert('提交成功', function () {
                            modal.hide();
                            searchQuery.trigger('change', searchQuery);
                        });
                    }
                    else {
                        alert(res.message);
                    }
                },
                error: function () {
                    alert('发生错误！');
                }
            });
        });

        $('#btn-back').on('click', function () {
            location.href = CONTEXT_PATH + '/microApp/customForm/manager.do?wyyId=' + getParam('wyyId');
        });
    };

    module.exports = {
        run: run
    };
});
