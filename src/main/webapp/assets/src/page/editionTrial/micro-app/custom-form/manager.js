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

    /*
     * 模型 - 审批表单
     */
    var Approval = Backbone.Model.extend({
        defaults: {
            name: '',
            des: '',
            date: '',
            status: 0
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
        getApprovalUser: function () {

        },
        setApprovalUser: function (data) {
            _.defaults(data, {
                code: []
            });
            var defaultApprovalUserIds = data.code;
            if (_.isArray(data.code)) {
                data.code.reverse();
                defaultApprovalUserIds = data.code.join(',');
            }

            alert('体验用户不能保存！');
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
            'click [data-do="thirdlink"]': 'doThirdlink'
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
        doThirdlink: function () {
            alert('该功能不对体验用户开放！');
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
        initialize: function () {
            this.listenTo(this.collection, 'reset', this.reset);
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
        cacheEls: function () {
            this.$items = this.$('[role="items"]');
        }
    });

    function run () {
        // 审批集合
        var approvalList = new ApprovalCollection();
        approvalList.url = CONTEXT_PATH + '/microApp/customForm/findApprovel.do';
        approvalList.parse = function (resp) {
            var data = [];
            if (_.isObject(resp) && _.isArray(resp.model)) {
                data = resp.model;
            }
            return data;
        };

        // 审批表格
        var approvalTable = new ApprovalTable({
            el: '#table-approval',
            collection: approvalList
        });

        // 类型筛选
        var $inputUrl = $('#input-url');
        var inputUrl = $('#input-url').attr('href');

        var $approvalType = $('#inputApprovalType');
        $approvalType.on('change', function () {
            var type = $(this).val();
            $inputUrl.attr('href', inputUrl + '?mostTypeKey=' + type);
            approvalList.filter({
                id: type,
                rnd: (new Date).getTime()
            });
        });
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
    }

    exports.run = run;
});
