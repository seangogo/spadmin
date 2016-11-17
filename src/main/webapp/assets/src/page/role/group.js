define(function(require, exports, module) {
    var $ = require('jquery');
    var _ = require('underscore');
    var Backbone = require('backbone');
    var template = require('template');
    var FormModal = require('component/FormModal');
    var alert = require('component/Alert');
    var confirm = require('component/Confirm');
    require('jquery-util');
    require('ztree');
    require('ztreeCheck');

    var STATUS_ENABLED = 1;
    var STATUS_DISABLED = 2;

    var Model = require('./Model');
    var Group = Model.Group;
    var Role = Model.Role;
    var RoleUser = Model.RoleUser;

    var TreeView = Backbone.View.extend({
        initialize: function() {
            this.cacheEls();
            this.treeOptions = this.treeOptions();
        },
        treeSetting: {
            view: {
                showLine: false,
                showIcon: false,
                showSelectStyle: true,
                txtSelectedEnable: true
            },
            data: {
                simpleData: {
                    enable: true,
                    rootPId: 0
                }
            }
        },
        treeOptions: function() {
            var methods = ['beforeCheck', 'beforeClick', 'beforeRemove', 'onCheck', 'onClick', 'onRemove'];
            var callback = {};
            _.each(methods, function(method) {
                if (this[method]) callback[method] = _.bind(this[method], this);
            }, this);

            function addDiyDom(treeId, node) {
                var tId = node.tId;
                var $node = $('#' + tId);
                var type = node.type;
                $node.addClass('node-item node-' + type);
            }

            var setting = _.clone(this.treeSetting);
            setting.view.addDiyDom = addDiyDom;
            setting.callback = callback;

            return setting;
        },
        addOne: function(model, collection, options) {
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
        removeOne: function(model, collection, options) {
            var id = model.id;
            var node = this.getNodeById(model.id);
            this.tree.removeNode(node);
        },
        createOne: function(attrs) {
            this.collection.add(attrs);
        },
        reset: function(collection, options) {
            this.initTree(collection.toJSON());
        },
        initTree: function(data) {
            if (!_.isArray(data)) data = [];
            this.tree = $.fn.zTree.init(this.$tree, this.treeOptions, data);
            this.treeId = this.tree.setting.treeId;
        },
        cacheEls: function() {
            this.$tree = this.$el;
        },
        remove: function() {
            if (this.tree) $.fn.zTree.destroy(this.treeId);
            TreeView.__super__.remove.apply(this, arguments);
        }
    })

    var RoleTreeView = TreeView.extend({
        initialize: function(options) {
            options || (options = {});

            this.listenTo(this.collection, 'add', this.addOne);
            this.listenTo(this.collection, 'remove', this.removeOne);
            this.listenTo(this.collection, 'destroy', this.removeOne);
            this.listenTo(this.collection, 'change', this.changeOne);
            this.listenTo(this.collection, 'sync', this.sync);
            this.listenTo(this.collection, 'reset', this.reset);

            this.listenTo(Backbone, 'save:role', this.createOne);

            this.cacheEls();
            this.treeOptions = this.treeOptions();
            this.initTree();
        },
        changeOne: function(model, options) {
            var node = this.getNodeById(model.id);
            node.name = model.get('name');
            this.tree.updateNode(node);
        },
        getNodeById: function(id) {
            return this.tree.getNodeByParam('id', id);
        },
        onClick: function(event, treeId, treeNode, clickFlag) {
            var model = this.collection.get(treeNode.id);
            if (model) Backbone.trigger('select:role', model);
        }
    });

    var UserTreeView = TreeView.extend({
        treeSetting: {
            view: {
                showLine: false,
                showIcon: false,
                showSelectStyle: true,
                txtSelectedEnable: true
            },
            data: {
                simpleData: {
                    enable: true,
                    rootPId: 0
                }
            },
            check: {
                enable: true
            }
        },
        initialize: function() {
            this.collection = new Backbone.Collection();
            _.extend(this.collection, {
                url: CONTEXT_PATH + '/role/getAllDepet.do',
                parse: function(resp) {
                    resp = _.extend({
                        success: false,
                        model: {}
                    }, resp);

                    var model = resp.model;
                    var parsed = [];

                    if (_.isObject(model)) {
                        var orgs = model.org;
                        var users = model.user;
                        var pId;
                        if (_.isArray(orgs)) {
                            _.each(orgs, function(org) {
                                pId = org.previousId == null ? 0 : org.previousId;
                                parsed.push({
                                    type: 'org',
                                    dataId: org.id,
                                    id: 'o_' + org.id,
                                    name: org.orgName,
                                    pId: pId,
                                    isParent: true
                                });
                            });
                        }
                        if (_.isArray(users)) {
                            _.each(users, function(user) {
                                pId = user.orgId == null ? 0 : user.orgId;
                                parsed.push({
                                    type: 'user',
                                    dataId: user.id,
                                    id: 'u_' + user.id,
                                    name: user.userName,
                                    pId: 'o_' + pId,
                                    isParent: false
                                });
                            });
                        }
                    }
                    return parsed;
                }
            });

            this.listenTo(this.collection, 'add', this.addOne);
            this.listenTo(this.collection, 'remove', this.removeOne);
            this.listenTo(this.collection, 'destroy', this.removeOne);
            this.listenTo(this.collection, 'sync', this.sync);
            this.listenTo(this.collection, 'reset', this.reset);

            this.cacheEls();
            this.treeOptions = this.treeOptions();
            this.initData();
        },
        cacheEls: function() {
            this.$tree = this.$('.user-tree');
            this.$tree.attr('id', this.cid);
        },
        initData: function() {
            this.collection.fetch({
                type: 'post',
                parse: true,
                reset: true
            });
        },
        getCheckedUsers: function() {
            var nodes = this.tree.getCheckedNodes();
            nodes = _.filter(nodes, function(node) {
                return node.type == 'user';
            });
            return nodes;
        }
    });

    var roleCreateModalRender = template($('#tmpl-roleCreateModal').html());
    var RoleCreateModal = FormModal.extend({
        template: roleCreateModalRender,
        submit: function(event) {
            var $target = $(event.target);
            var params = this.$form.serializeObject();
            this.model.set(params);
            $.ajax({
                url: CONTEXT_PATH + '/role/addRole.do',
                type: 'post',
                dataType: 'json',
                context: this,
                data: params,
                success: function(resp) {
                    resp = _.extend({
                        success: false,
                        message: '操作失败'
                    }, resp);

                    if (resp.success) {
                        alert('操作成功').delay(1);
                        if (_.isObject(resp.model)) {
                            var model = resp.model;
                            var attrs = _.pick(model, 'id', 'companyId', 'type', 'status');
                            attrs.pId = model.parentId;
                            attrs.name = model.roleName;
                            Backbone.trigger('save:role', attrs);
                        }

                        this.hide();
                    } else {
                        alert(resp.message);
                    }

                },
                error: function() {
                    alert('请求失败');
                },
                beforeSend: function() {
                    $target.prop('disabled', true);
                },
                complete: function() {
                    $target.prop('disabled', false);
                }
            });
        }
    });

    var roleGroupEditModalRender = template($('#tmpl-roleGroupEditModal').html());
    var RoleEditModal = FormModal.extend({
        template: roleGroupEditModalRender,
        submit: function(event) {
            var params = this.$form.serializeObject();
            var $target = $(event.target);

            $.ajax({
                url: CONTEXT_PATH + '/role/editRole.do',
                type: 'post',
                dataType: 'json',
                context: this,
                data: params,
                success: function(resp) {
                    resp = _.extend({
                        success: false,
                        message: '操作失败'
                    }, resp);

                    if (resp.success) {
                        alert('操作成功').delay(1);
                        var attrs = _.pick(params, 'roleName');
                        this.model.set({
                            name: attrs.roleName
                        });
                        this.hide();
                    } else {
                        alert(resp.message);
                    }

                },
                error: function() {
                    alert('请求失败');
                },
                beforeSend: function() {
                    $target.prop('disabled', true);
                },
                complete: function() {
                    $target.prop('disabled', false);
                }
            });
        }
    });

    var roleGroupCreateModalRender = template($('#tmpl-roleGroupCreateModal').html());
    var groupCreateModalRender = template($('#tmpl-groupCreateModal').html());
    var RoleGroupCreateModal = FormModal.extend({
        template: groupCreateModalRender,
        submit: function(event) {
            var $target = $(event.target);
            var params = this.$form.serializeObject();
            this.model.set(params);
            $.ajax({
                url: CONTEXT_PATH + '/role/addRole.do',
                type: 'post',
                dataType: 'json',
                context: this,
                data: params,
                success: function(resp) {
                    resp = _.extend({
                        success: false,
                        message: '操作失败'
                    }, resp);

                    if (resp.success) {
                        alert('操作成功').delay(1);
                        if (_.isObject(resp.model)) {
                            var model = resp.model;
                            var attrs = _.pick(model, 'id', 'companyId', 'type', 'status');
                            attrs.pId = model.parentId;
                            attrs.name = model.roleName;
                            this.collection.add(attrs);
                        }

                        this.hide();
                    } else {
                        alert(resp.message);
                    }

                },
                error: function() {
                    alert('请求失败');
                },
                beforeSend: function() {
                    $target.prop('disabled', true);
                },
                complete: function() {
                    $target.prop('disabled', false);
                }
            });
        }
    });

    var roleUserListItemRender = template($('#tmpl-roleUserListItem').html());
    var RoleUserListItem = Backbone.View.extend({
        template: roleUserListItemRender,
        className: 'item',
        events: {
            'click [data-do="remove"]': 'doRemove'
        },
        initialize: function() {
            this.listenTo(this.model, 'remove', this.remove);
        },
        doRemove: function() {
            var model = this.model;
            confirm('确认移除该用户？', function() {
                var data = {
                    id: model.get('roleId'),
                    userId: model.get('id')
                };
                $.ajax({
                    url: CONTEXT_PATH + '/role/deleteRolePerson.do',
                    type: 'post',
                    dataType: 'json',
                    data: data,
                    success: function(resp) {
                        resp = _.extend({
                            success: false,
                            message: '操作失败'
                        }, resp);
                        if (resp.success) {
                            model.trigger('destroy', model, model.collection);
                            // model.collection.remove(model);
                            alert('操作成功').delay(1);
                        } else {
                            alert(resp.message);
                        }
                    },
                    error: function() {
                        alert('请求失败');
                    }
                });
            })

        },
        render: function() {
            var markup = this.template({
                model: this.model.toJSON()
            });
            this.$el.html(markup);
            return this;
        }
    });
    var RoleUserList = Backbone.View.extend({
        noData: template('<p class="nodata">暂无数据</p>'),
        loading: template('<p class="loading">数据记载中...</p>'),
        itemView: Backbone.View,
        initialize: function(options) {
            options || (options = {});
            _.extend(this, _.pick(options, 'itemView'));

            this.collection = new Backbone.Collection();
            _.extend(this.collection, {
                url: CONTEXT_PATH + '/role/rolePersonList.do',
                parse: function(resp) {
                    resp = _.extend({
                        success: false,
                        model: []
                    }, resp);

                    var models = resp.model;
                    var parsed = [];
                    if (_.isArray(models)) {
                        _.each(models, function(model) {
                            parsed.push({
                                id: model.userId,
                                name: model.userName,
                                roleId: model.roleId
                            });
                        })
                    }
                    return parsed;
                }
            });

            this.listenTo(this.collection, 'request', this.request);
            this.listenTo(this.collection, 'add', this.addOne);
            this.listenTo(this.collection, 'reset', this.reset);

            this.listenTo(Backbone, 'add:roleUser', this.addBatch);

            this.initData();
        },
        request: function() {
            // 请求时清除数据
            this.collection.reset(null, {
                silent: true
            });
            // 提示
            this.$el.html(this.loading());
        },
        addBatch: function(users, role) {
            console.log('trigger');
            if (_.isArray(users) && users.length > 0) {
                var roleId = role.get('id');
                _.each(users, function(user) {
                    var attrs = {
                        id: user.dataId,
                        roleId: role.id,
                        name: user.name
                    };

                    this.collection.add(new Backbone.Model(attrs));
                }, this);
            }
        },
        addOne: function(model, collection, options) {
            var row = new this.itemView({
                model: model
            });
            this.$el.append(row.render().el);
        },
        reset: function(collection, options) {
            var previousModels = options.previousModels;
            _.each(previousModels, function(model) {
                model.trigger('remove');
            });

            this.$el.empty();

            this.collection.each(function(model, index) {
                model.set('$index', index);
                this.addOne(model, collection);
            }, this);

            /*if (this.collection.length == 0) {
                this.$el.html(this.noData());
            } else {
                this.collection.each(function(model, index) {
                    model.set('$index', index);
                    this.addOne(model, collection);
                }, this);
            }*/
        },
        initData: function() {
            this.collection.fetch({
                type: 'post',
                reset: true,
                parse: true,
                data: {
                    id: this.model.id
                }
            });
        }
    });

    var roleUserListPanelRender = template($('#tmpl-roleUserListPanel').html());
    var RoleUserListPanel = Backbone.View.extend({
        template: roleUserListPanelRender,
        subviews: {},
        events: {
            'click [data-do="edit"]': 'doEdit',
            'click [data-do="create"]': 'doCreate',
            'click [data-do="disable"]': 'doDisable',
            'click [data-do="enable"]': 'doEnable',

            'click [data-do="delete"]': 'doDelete',
            'click [data-do="setUser"]': 'doSetUser'
        },
        initialize: function() {
            this.listenTo(this.model, 'change', this.render);
        },
        doDelete: function(event) {
            var data = {
                id: this.model.id
            };
            var model = this.model;
            confirm('确认删除', function() {
                $.ajax({
                    url: CONTEXT_PATH + '/role/deleteRole.do',
                    dataType: 'json',
                    data: data,
                    success: function(resp) {
                        resp = _.extend({
                            success: false,
                            message: '操作失败'
                        }, resp);

                        if (resp.success) {
                            model.trigger('destroy', model);
                            alert('操作成功').delay(1);
                        } else {
                            alert(resp.message);
                        }
                    },
                    error: function() {
                        alert('请求失败');
                    }
                })
            });
        },
        doEdit: function(event) {
            var modal = new RoleEditModal({
                model: this.model
            });
            modal.render().$el.appendTo(document.body);
            modal.show();
        },
        doEnable: function() {
            this.updateStatus(STATUS_ENABLED);
        },
        doDisable: function() {
            this.updateStatus(STATUS_DISABLED);
        },
        updateStatus: function(status) {
            var id = this.model.id;
            status = status == STATUS_ENABLED ? STATUS_ENABLED : STATUS_DISABLED;
            var data = {
                id: id,
                status: status
            };
            $.ajax({
                url: CONTEXT_PATH + '/role/updateRoleStatus.do',
                dataType: 'json',
                data: data,
                context: this,
                success: function(resp) {
                    resp = _.extend({
                        success: false,
                        message: '操作失败'
                    }, resp);

                    if (resp.success) {
                        this.model.set('status', status);
                        alert('操作成功').delay(1);
                    } else {
                        alert(resp.message);
                    }
                }
            });
        },
        doCreate: function(event) {
            var attrs = {
                companyId: COMPANY_ID,
                parentId: this.model.id
            };
            var modal = new RoleCreateModal({
                model: new Role(attrs)
            });
            modal.render().$el.appendTo(document.body);
            modal.show();
        },
        doSetUser: function(event) {
            var $target = $(event.target);
            var users = this.userList.getCheckedUsers();
            var userId = [];
            var roleId = this.model.id;
            _.each(users, function(user) {
                userId.push(user.dataId);
            });
            $.ajax({
                url: CONTEXT_PATH + '/role/addRolePerson.do',
                traditional: true,
                type: 'post',
                dataType: 'json',
                context: this,
                data: {
                    roleId: roleId,
                    userId: userId
                },
                beforeSend: function() {
                    $target.prop('disabled', false);
                },
                success: function(resp) {
                    resp = _.extend({
                        success: false,
                        message: '操作失败'
                    }, resp);

                    if (resp.success) {
                        alert('操作成功');
                        Backbone.trigger('add:roleUser', users, this.model);
                    } else {
                        alert(resp.message);
                    }
                },
                error: function() {
                    alert('请求失败');
                },
                complete: function() {
                    $target.prop('disabled', false);
                }
            })
            console.log(users);
        },
        cacheEls: function() {
            this.$roleUserList = this.$('.role-user-list');
            this.$userList = this.$('.user-list');
        },
        render: function() {
            var markup = this.template({
                model: this.model.toJSON()
            });
            this.$el.html(markup);
            this.cacheEls();
            this.initSubViews();
            return this;
        },
        initSubViews: function() {
            this.roleUserList = new RoleUserList({
                el: this.$roleUserList,
                model: this.model,
                itemView: RoleUserListItem
            });;

            this.userList = new UserTreeView({
                el: this.$userList,
                model: this.model
            });
        },
        remove: function() {
            _.each(this.subviews, function(view) {
                view.remove();
            });

            RoleUserListPanel.__super__.remove.call(this);

            this.removed = true;
        }
    });

    function run() {
        // 角色/群组集合
        var roleList = new Backbone.Collection();
        _.extend(roleList, {
            url: CONTEXT_PATH + '/role/roleList.do',
            parse: function(resp) {
                resp = _.extend({
                    model: []
                }, resp);

                var models = resp.model;
                var parsed = [];
                if (_.isArray(models)) {
                    _.each(models, function(model) {
                        var attrs = _.pick(model, 'id', 'companyId', 'status', 'type');
                        attrs.pId = model.parentId;
                        attrs.name = model.roleName;
                        parsed.push(attrs);
                    });
                }

                return parsed;
            }
        });
        // 角色/群组树
        var tree = new RoleTreeView({
            el: $('#role-tree'),
            collection: roleList
        });

        var $content = $('#role-users');
        // 角色用户列表视图
        var roleUserListPanel;
        Backbone.on('select:role', function(model) {
            if (roleUserListPanel && !roleUserListPanel.removed) roleUserListPanel.remove();

            roleUserListPanel = new RoleUserListPanel({
                className: 'role-users',
                model: model
            });

            $content.html(roleUserListPanel.render().el);
        });

        // 创建一级角色或群组
        $('[data-do="create:orgOrGroup"]').on('click', function() {
            var attrs = {
                companyId: COMPANY_ID,
                parentId: 0
            };
            var modal = new RoleGroupCreateModal({
                model: new Group(attrs),
                collection: roleList
            });
            modal.render().$el.appendTo(document.body);
            modal.show();
        });

        // 获取角色/群组数据
        roleList.fetch({
            type: 'post',
            reset: true,
            parse: true,
            data: {
                type: 0
            }
        });
    }

    exports.run = run;
});