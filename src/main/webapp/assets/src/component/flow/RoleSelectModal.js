define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore'),
        Backbone = require('backbone');
    require('ztree');
    var template = require('template');
    var alert = require('component/Alert');

    var modalTemplate = '<div class="modal-dialog">\
            <div class="modal-content">\
                <div class="modal-header">\
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">\
                        <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>\
                    </button>\
                    <h4 class="modal-title">选择角色/用户</h4>\
                </div>\
                <div class="modal-body">\
                    <ul class="nav nav-tabs" role="tablist">\
                        <li role="presentation" class="active"><a href="#tabUser" role="tab" data-toggle="tab">用户</a></li>\
                        <li role="presentation"><a href="#tabRole" role="tab" data-toggle="tab">角色</a></li>\
                        <li role="presentation"><a href="#tabGroup" role="tab" data-toggle="tab">群组</a></li>\
                    </ul>\
                    <div class="tab-content">\
                        <div role="tabpanel" class="tab-pane clearfix active" id="tabUser">\
                            <div class="panel-sidebar col-sm-4 col-md-4 col-lg-4">\
                                <div class="panel-sidebar-body" role="users">\
                                    <ul id="user-tree" class="select-tree ztree" role="user-tree"></ul>\
                                </div>\
                            </div>\
                            <div class="panel-main col-sm-8 col-md-8 col-lg-8">\
                                <form class="clearfix" role="selected-users">\
                                    <ul class="list-items" role="items"></ul>\
                                </form>\
                            </div>\
                        </div>\
                        <div role="tabpanel" class="tab-pane clearfix" id="tabRole">\
                            <div class="panel-sidebar col-sm-4 col-md-4 col-lg-4">\
                                <div class="panel-sidebar-body" role="roles">\
                                    <ul id="role-tree" class="select-tree ztree" role="role-tree"></ul>\
                                </div>\
                            </div>\
                            <div class="panel-main col-sm-8 col-md-8 col-lg-8">\
                                <form class="clearfix" role="selected-roles">\
                                    <ul class="list-items" role="items"></ul>\
                                </form>\
                            </div>\
                        </div>\
                        <div role="tabpanel" class="tab-pane clearfix" id="tabGroup">\
                            <div class="panel-sidebar col-sm-4 col-md-4 col-lg-4">\
                                <div class="panel-sidebar-body" role="groups">\
                                    <ul id="group-tree" class="select-tree ztree" role="group-tree"></ul>\
                                </div>\
                            </div>\
                            <div class="panel-main col-sm-8 col-md-8 col-lg-8">\
                                <form class="clearfix" role="selected-groups">\
                                    <ul class="list-items" role="items"></ul>\
                                </form>\
                            </div>\
                        </div>\
                    </div>\
                </div>\
                <div class="modal-footer">\
                    <button type="button" class="btn btn-primary" data-do="submit">确定</button>\
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>\
                </div>\
            </div>\
        </div>';
    var itemTemplate = '<span>{{name}}</span>\
        <a href="javascript:" data-do="remove">X</a>\
        <input type="hidden" name="id" value="{{id}}">';
    var modalRender = template(modalTemplate);
    var itemRender = template(itemTemplate);

    var urls = {
        getDepts: CONTEXT_PATH + '/users/getAllDept.do',
        getDeptUsers: CONTEXT_PATH + '/users/getUserByDeptId.do',

        getRoleList: CONTEXT_PATH + '/role/roleList.do'
    };

    var TreeView = Backbone.View.extend({
        initialize: function () {
            this.cacheEls();
            this.treeOptions = this.treeOptions();
        },
        treeOptions: function () {
            var methods = ['beforeCheck', 'beforeClick', 'beforeRemove', 'onCheck', 'onClick', 'onRemove'];
            var callback = {};
            _.each(methods, function (method) {
                if (this[method]) callback[method] = _.bind(this[method], this);
            }, this);

            function addDiyDom (treeId, node) {
                var tId = node.tId;
                var $node = $('#' + tId);
                var type = node.type;
                $node.addClass('node-item node-' + type);
            }

            var setting = {
                view: {
                    showLine: false,
                    showIcon: false,
                    showSelectStyle: false,
                    txtSelectedEnable: true,
                    expandSpeed: ''
                },
                data: {
                    simpleData: {
                        enable: true,
                        rootPId: 0
                    }
                }
            };
            setting.view.addDiyDom = addDiyDom;
            setting.callback = callback;

            return setting;
        },
        addOne: function (model, collection, options) {
            var pId = model.get('pId');
            if (pId == 0) {
                this.tree.addNodes(null, model.toJSON());
            } else {
                var pNode = this.getNodeById(pId);
                if (pNode) {
                    this.tree.addNodes(pNode, model.toJSON());
                }
            }
        },
        removeOne: function (model, collection, options) {
            var id = model.id;
            var node = this.getNodeById(model.id);
            this.tree.removeNode(node);
        },
        createOne: function (attrs) {
            this.collection.add(attrs);
        },
        reset: function (collection, options) {
            this.initTree(collection.toJSON());
        },
        initTree: function (data) {
            if (!_.isArray(data)) data = [];
            this.tree = $.fn.zTree.init(this.$tree, this.treeOptions, data);
            this.treeId = this.tree.setting.treeId;
        },
        cacheEls: function () {
            this.$tree = this.$el;
        },
        remove: function () {
            if (this.tree) $.fn.zTree.destroy(this.treeId);
            TreeView.__super__.remove.apply(this, arguments);
        },
        expand: function (node) {
            if (!node) {
                this.tree.expandAll(true);
            }
            else {
                this.tree.expandNode(node, true);
            }
        },
        collapse: function (node) {
            if (!node) {
                this.tree.expandAll(false);
            }
            else {
                this.tree.expandNode(node, false);
            }
        }
    });
    var RoleTreeView = TreeView.extend({
        initialize: function (options) {
            options || (options = {});

            this.listenTo(this.collection, 'sync', this.sync);
            this.listenTo(this.collection, 'reset', this.reset);

            this.cacheEls();

            function addDiyDom (treeId, node) {
                var tId = node.tId;
                var $node = $('#' + tId);
                $node.addClass('node-item');

                var eidtBtn = $('<span class="add-role" style="margin-left: 5px; color: #337ab7" title="添加">添加</span>');
                if (node.id === '$$$') {
                    $node.find('#' + tId + '_span').css({
                        'color': '#22f',
                        'fontWeight': 'bold'
                    });
                }
                $node.find('#' + tId + '_span').after(eidtBtn);
            }
            var treeOptions = this.treeOptions();
            treeOptions.view.addDiyDom = addDiyDom;

            Object.defineProperty(this, 'treeOptions', { value: treeOptions });
        },
        reset: function (collection, options) {
            var treeJson = collection.toJSON();
            this.initTree(treeJson);
        },
        getNodeById: function (id) {
            return this.tree.getNodeByParam('id', id);
        },
        onClick: function (event, treeId, treeNode, clickFlag) {
            if ($(event.target).is('.add-role')) {
                var model = this.collection.get(treeNode.id);
                this.trigger('addRole', model);
            }
        }
    });
    var UserTreeView = TreeView.extend({
        initialize: function (options) {
            var _this = this;
            options || (options = {});

            this.listenTo(this.collection, 'sync', this.sync);
            this.listenTo(this.collection, 'reset', this.reset);

            this.cacheEls();

            function addDiyDom (treeId, node) {
                var tId = node.tId;
                var $node = $('#' + tId);
                $node.addClass('node-item');
                if (!node.isParent) {
                    var eidtBtn = $('<span class="add-user" style="margin-left: 5px; color: #337ab7" title="添加">添加</span>');
                    $node.find('#' + tId + '_span').after(eidtBtn);
                }
            }
            var treeOptions = this.treeOptions();
            treeOptions.view.addDiyDom = addDiyDom;
            treeOptions.async = {
                enable: true,
                dataType: 'json',
                url: urls.getDeptUsers,
                autoParam: ['deptId'],
                otherParam: {
                    companyId: COMPANY_ID
                    //pageNum: 1,
                    //pageSize: 100000
                },
                dataFilter: function (treeId, parentNode, response) {
                    _.defaults(response, {
                        success: false,
                        model: []
                    });

                    var data = [];
                    if (response.success) {
                        var users = response.model;//.list;
                        _.each(users, function (item) {
                            data.push({
                                userType: 'user',
                                userId: item.id,
                                code: item.id,
                                name: item.userName,
                                headUrl: item.headImg,
                                showindex: item.showindex
                            });
                        });
                    }
                    //data.sort(function (a,b) { a.showindex - b.showindex; });
                    _.each(_this.cacheData, function (item) {
                        if (item.pId === parentNode.id) {
                            data.push(item);
                        }
                    });

                    return data;
                }
            };

            Object.defineProperty(this, 'treeOptions', { value: treeOptions });
        },
        getNodeById: function (id) {
            return this.tree.getNodeByParam('id', id);
        },
        onClick: function (event, treeId, treeNode, clickFlag) {
            if ($(event.target).is('.add-user')) {
                //var model = this.collection.get(treeNode.id);
                var model = new Backbone.Model({
                    id: treeNode.userId,
                    name: treeNode.name
                });
                this.trigger('addUser', model);
            }
        },
        reset: function (collection, options) {
            var allDeptsData = collection.toJSON();
            var rootData = [];
            _.each(allDeptsData, function (item) {
                if (item.pId == '' || !item.pId) {
                    rootData.push(item);
                }
            });
            this.cacheData = allDeptsData;
            this.initTree(rootData);
        }
    });

    var SelectedItemView = Backbone.View.extend({
        tagName: 'li',
        template: itemRender,
        events: {
            'click [data-do="remove"]': 'doRemove'
        },
        initialize: function () {
            this.listenTo(this.model, 'remove', this.remove);
        },
        doRemove: function () {
            this.model.collection.remove(this.model);
        },
        render: function () {
            var markup = this.template(this.model.toJSON());
            this.$el.html(markup);
            return this;
        }
    });
    var SelecedPanelView = Backbone.View.extend({
        ItemView: SelectedItemView,
        initialize: function (options) {
            options || (options = {});
            if (options.itemView) this.ItemView = options.itemView;
            this.cacheEls();

            this.listenTo(this.collection, 'add', this.addOne);
            this.listenTo(this.collection, 'reset', this.reset);
        },
        reset: function (collection, options) {
            var previousModels = options.previousModels;
            _.each(previousModels, function (model) {
                model.trigger('remove');
            });

            this.$items.empty();
            if (this.collection.length > 0) {
                this.addAll();
            }
        },
        clear: function () {
            this.collection.reset();
        },
        addAll: function () {
            this.collection.each(function (model, index) {
                model.set('$index', index);
                this.addOne(model, this.collection);
            }, this);
        },
        addOne: function (model, collection, options) {
            var view = new this.ItemView({
                model: model
            });
            this.$items.append(view.render().el);
        },
        cacheEls: function () {
            this.$items = this.$('[role="items"]');
        },
        getData: function () {
            return this.collection.toJSON();
        }
    });

    var initRoleView = function () {
        var _this = this;
        var roleList = new Backbone.Collection();
        _.extend(roleList, {
            url: urls.getRoleList,
            parse: function (resp) {
                resp = _.extend({
                    model: []
                }, resp);

                var models = resp.model;
                var parsed = [];
                if (_.isArray(models)) {
                    _.each(models, function (model) {
                        var attrs = _.pick(model, 'id', 'companyId', 'status', 'type');
                        attrs.pId = model.parentId;
                        attrs.name = model.roleName;
                        parsed.push(attrs);
                    });
                }
                //添加特殊角色 发起者
                parsed.unshift({
                    id: '$$$',
                    companyId: '',
                    status: '',
                    type: '',
                    pId: '',
                    name: '发起者'
                });

                return parsed;
            }
        });
        this.subviews.roleTree = new RoleTreeView({
            el: this.$roleTree,
            collection: roleList
        });

        var selectedList = new Backbone.Collection();
        this.subviews.selectedRoleView = new SelecedPanelView({
            el: this.$selectedRoles,
            collection: selectedList
        });

        this.listenTo(this.subviews.roleTree, 'addRole', function (model) {
            var attr = _.pick(model.toJSON(), 'id', 'name');
            if (_this.multi === false) { selectedList.reset(); }
            selectedList.add(attr);
        });

        roleList.fetch({
            type: 'post',
            reset: true,
            parse: true,
            data: {
                type: 1
            }
        });
    };
    var initGroupView = function () {
        var _this = this;
        var groupList = new Backbone.Collection();
        _.extend(groupList, {
            url: urls.getRoleList,
            parse: function (resp) {
                resp = _.extend({
                    model: []
                }, resp);

                var models = resp.model;
                var parsed = [];
                if (_.isArray(models)) {
                    _.each(models, function (model) {
                        var attrs = _.pick(model, 'id', 'companyId', 'status', 'type');
                        attrs.pId = model.parentId;
                        attrs.name = model.roleName;
                        parsed.push(attrs);
                    });
                }

                return parsed;
            }
        });
        this.subviews.groupTree = new RoleTreeView({
            el: this.$groupTree,
            collection: groupList
        });

        var selectedList = new Backbone.Collection();
        this.subviews.selectedGroupView = new SelecedPanelView({
            el: this.$selectedGroups,
            collection: selectedList
        });

        this.listenTo(this.subviews.groupTree, 'addRole', function (model) {
            var attr = _.pick(model.toJSON(), 'id', 'name');
            if (_this.multi === false) { selectedList.reset(); }
            selectedList.add(attr);
        });

        groupList.fetch({
            type: 'post',
            reset: true,
            parse: true,
            data: {
                type: 0
            }
        });
    };
    var initUserView = function () {
        var _this = this;
        var userList = new Backbone.Collection();
        _.extend(userList, {
            url: urls.getDepts,
            parse: function (resp) {
                resp = _.extend({
                    model: []
                }, resp);

                var models = resp.model;
                var parsed = [];
                if (_.isArray(models)) {
                    _.each(models, function (model) {
                        var attrs = _.pick(model, 'id', 'companyId', 'status');
                        attrs.pId = model.previousId;
                        attrs.deptId = model.id;
                        attrs.name = model.orgName;
                        attrs.isParent = true;
                        parsed.push(attrs);
                    });
                }

                return parsed;
            }
        });
        this.subviews.userTree = new UserTreeView({
            el: this.$userTree,
            collection: userList
        });

        var selectedList = new Backbone.Collection();
        this.subviews.selectedUserView = new SelecedPanelView({
            el: this.$selectedUsers,
            collection: selectedList
        });

        this.listenTo(this.subviews.userTree, 'addUser', function (model) {
            var attr = _.pick(model.toJSON(), 'id', 'name');
            if (_this.multi === false) { selectedList.reset(); }
            selectedList.add(attr);
        });

        userList.fetch({
            type: 'post',
            reset: true,
            parse: true,
            data: {}
        });
    };

    var RoleSelectModal = Backbone.View.extend({
        template: modalRender,
        itemTemplate: itemRender,
        className: 'modal-role-select modal',
        attributes: {
            role: 'dialog'
        },
        events: {
            'click [data-do="submit"]': 'doSubmit'
        },
        initialize: function () {
            this.render();
            this.$('.modal-body').on('shown.bs.tab', _.bind(function (event) {
                var $target = $(event.target);
                var mode = $target.attr('href').replace('#tab', '');
                this.changeMode(mode);
            }, this));
        },
        render: function () {
            var markup = this.template({});
            this.$el.html(markup).appendTo(document.body);

            this.cacheEls();
            this.initSubviews();

            return this;
        },
        cacheEls: function () {
            this.$roleTree = this.$('[role="role-tree"]');
            this.$selectedRoles = this.$('[role="selected-roles"]');

            this.$groupTree = this.$('[role="group-tree"]');
            this.$selectedGroups = this.$('[role="selected-groups"]');

            this.$userTree = this.$('[role="user-tree"]');
            this.$selectedUsers = this.$('[role="selected-users"]');
        },
        changeMode: function (mode) {
            this.mode = mode;
            this.subviews['selected' + mode + 'View'].clear();
            this.subviews[mode.toLowerCase() + 'Tree'].collapse();
        },
        addItem: function (model) {
            var newItem = new SelectedItemView({ model: model });

        },
        initSubviews: function () {
            this.subviews = {};
            initRoleView.apply(this);
            initGroupView.apply(this);
            initUserView.apply(this);
        },
        show: function (multi, enableViews) {
            this.setShowState(multi, enableViews);
            this.$el.modal('show');
            this.reset();
        },
        setShowState: function (multi, enableViews) {
            this.multi = multi === false ? false : true;
            enableViews = _.defaults(enableViews || {}, {
                'User': true,
                'Role': true,
                'Group': true
            });

            this.$('[role="tablist"] a').each(function () {
                var enable = $(this).attr('href').replace('#tab', '');
                if (!enableViews[enable]) $(this).hide();
                else $(this).show();
            });
        },
        hide: function () {
            this.$el.modal('hide');
        },
        reset: function () {
            this.subviews.selectedRoleView.clear();
            this.subviews.selectedGroupView.clear();
            this.subviews.selectedUserView.clear();

            this.subviews.roleTree.collapse();
            this.subviews.groupTree.collapse();
            this.subviews.userTree.collapse();

            var defaultTab = this.$('[role="tablist"] a:visible').eq(0);
            if (defaultTab.parent().is('.active')) {
                this.changeMode(defaultTab.attr('href').replace('#tab', ''));
            }
            else {
                defaultTab.tab('show');
            }
        },
        doSubmit: function () {
            var mode = this.mode;
            var data = this.subviews['selected' + mode + 'View'].getData();
            this.trigger('selected', mode, data);

            this.hide();
        }
    });

    module.exports = RoleSelectModal;
});
