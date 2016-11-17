define(function (require) {
    var $ = require('jquery');
    var _ = require('underscore');
    var Backbone = require('backbone');
    require('bootstrap');
    require('ztree');
    require('plupload');

    var template = require('template'),
        confirm = require('component/Confirm'),
        alert = require('component/Alert');

    function avatarColor (str) {
        var code = 0;
        var temp = 0;
        var codeStr = '';
        var i = 0;
        for (i = 0; i < str.length; i++) {
            code += str.charCodeAt(i);
        }
        for (i = 0; i < 3; i++) {
            temp = code % 192;
            codeStr += ('00' + temp.toString(16)).slice(-2);
            code = Math.round(code * temp / 127);
        }
        return codeStr;
    }
    var specialNodeName = {
        '$$$': '批量发起人员'
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

    /*
     * 模型 - 部门成员
     */
    var DeptUserModel = Backbone.Model.extend({
        defaults: {
            type: 'user', // user|role，默认为 user
            code: '',
            userId: 0,
            userName: '',
            headUrl: '',
            avatar: ''
        },
        initialize: function () {
            var avatar = this.get('userName');
            if (avatar.length > 2) avatar = avatar.substr(-2);
            this.set('avatar', avatar);
        }
    });
    /*
     * 集合 - 用户
     */
    var DeptUserCollection = Backbone.Collection.extend({
        model: DeptUserModel
    });
    /*
     * 模型 - 部门
     */
    var DeptModel = Backbone.Model.extend({
        defaults: {
            id: 0,
            orgName: ''
        },
        initialize: function () {
            // 部门下的用户集合
            this.users = new DeptUserCollection;
        }
    });
    /*
     * 集合 - 部门
     */
    var DeptCollection = Backbone.Collection.extend({
        model: DeptModel
    });
    /*
     * 模型 - 审批人
     */
    var ApprovalUserModel = DeptUserModel.extend({
        initialize: function () {
            var userName = this.get('userName');
            var avatar = userName;
            if (avatar.length > 2) avatar = userName.substr(-2);
            this.set('avatar', avatar);

            var color = '#' + avatarColor(userName);
            this.set('$color', color);
        },
        destroy: function () {
            this.collection.remove(this);
        }
    });
    /*
     * 集合 - 审批人列表
     */
    var ApprovalUserCollection = Backbone.Collection.extend({
        model: ApprovalUserModel
    });

    var CollectionView = Backbone.View.extend({
        ItemView: Backbone.View,
        initialize: function (options) {
            options || (options = {});
            if (options.itemView) this.ItemView = options.itemView;
            this.listenTo(this.collection, 'reset', this.reset);
        },
        reset: function (collection, options) {
            var previousModels = options.previousModels;
            _.each(previousModels, function (model) {
                model.trigger('remove');
            });

            this.$items.empty();
            if (this.collection.length == 0) {
                this.noData();
            } else {
                this.addAll();
            }

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
        noData: function () {

        }
    });
    var deptUserRender = template($('#tmpl-deptUser').html());
    var deptRender = template($('#tmpl-dept').html());

    var DeptTree = Backbone.View.extend({
        initialize: function () {
            this.cacheEls();
            this.initData();
        },
        beforeNodeClick: function (treeId, treeNode, clickFlag) {
            if (treeNode.isParent) return false;
        },
        onNodeClick: function (event, treeId, treeNode, clickFlag) {
            if (!treeNode.isParent) {
                var avatar = treeNode.name;
                if (avatar.length > 2) avatar = avatar.substr(-2);

                var model = new DeptUserModel({
                    type: treeNode.userType,
                    code: treeNode.code,
                    userId: treeNode.userId,
                    userName: treeNode.name,
                    headUrl: treeNode.headUrl,
                    avatar: avatar
                });
                //type', 'code', 'userName', 'orgId', 'headUrl', 'avatar'
                this.trigger('addApprovalUser', model);
            }
        },
        initTree: function (data) {
            var _this = this;
            var beforeClick = _.bind(this.beforeNodeClick, this);
            var onClick = _.bind(this.onNodeClick, this);

            function addDiyDom (treeId, node) {
                // console.log(arguments);
                var tId = node.tId;
                var $node = $('#' + tId);
                var type = node.userType;
                $node.addClass('node-' + type);
                if (!node.isParent) {
                    $node.addClass('node-child');
                    var avatar = node.name;
                    if (avatar.length > 2) avatar = avatar.substr(-2);

                    var color = '#' + avatarColor(node.name);
                    var markup = deptUserRender({
                        type: node.userType,
                        code: node.code,
                        userId: node.userId,
                        userName: node.name,
                        headUrl: node.headUrl,
                        avatar: avatar,
                        $color: color
                    });

                    $node.find('.node_name').html(markup);
                }
            }

            var setting = {
                view: {
                    showLine: false,
                    showIcon: false,
                    selectedMulti: false,
                    txtSelectedEnable: false,
                    addDiyDom: addDiyDom
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                async: {
                    enable: true,
                    dataType: 'json',
                    url: CONTEXT_PATH + '/users/getUserByDeptId.do',
                    autoParam: ['deptId'],
                    otherParam: {
                        companyId: COMPANY_ID
                    },
                    dataFilter: function (treeId, parentNode, response) {
                        _.defaults(response, {
                            success: false,
                            model: []
                        });

                        var data = [];
                        if (response.success) {
                            var users = typeof response.model === 'string' ? [] : response.model;
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
                        data.sort(function (a,b) { a.showindex - b.showindex; });

                        _.each(_this.cacheData, function (item) {
                            if (item.pId === parentNode.id) {
                                data.push(item);
                            }
                        });

                        return data;
                    }
                },
                callback: {
                    beforeClick: beforeClick,
                    onClick: onClick
                }
            };

            this.tree = $.fn.zTree.init(this.$tree, setting, data);
            this.treeId = this.tree.setting.treeId;
        },
        initData: function () {
            $.ajax({
                context: this,
                url: CONTEXT_PATH + '/users/getAllDept.do',
                data: {
                    companyId: COMPANY_ID
                },
                success: function (response) {
                    _.defaults(response, {
                        success: false,
                        message: '',
                        model: []
                    });
                    if (response.success) {
                        var data = [], rootData = [];
                        _.each(specialNodeName, function (name, key) {
                            var tempData = {
                                isParent: false,
                                userType: 'placeholder',
                                userId: key,
                                code: key,
                                name: name,
                                showindex: -1,
                                headUrl: ''
                            };
                            data.push(tempData);
                            rootData.push(tempData);
                        });

                        _.each(response.model, function (item) {
                            var pId = item.previousId;
                            var id = 'dept' + item.id;
                            if (pId != '' && pId != null) pId = 'dept' + pId;
                            data.push({
                                userType: 'dept',
                                isParent: true,
                                id: id,
                                pId: pId,
                                name: item.orgName,
                                orgId: item.id,
                                deptId: item.id,
                                showindex: item.showindex
                            });

                            if (pId == '' || !pId) {
                                rootData.push({
                                    userType: 'dept',
                                    isParent: true,
                                    id: id,
                                    pId: pId,
                                    name: item.orgName,
                                    orgId: item.id,
                                    deptId: item.id,
                                    showindex: item.showindex
                                });
                            }
                        });

                        data.sort(function (a,b) { return a.showindex - b.showindex; });
                        rootData.sort(function (a,b) { return a.showindex - b.showindex; });

                        this.cacheData = data;
                        this.initTree(rootData);
                    }
                },
                error: function () {
                    alert('部门数据获取失败').delay(3);
                }
            });
        },
        cacheEls: function () {
            this.$tree = this.$('.dept-tree');
        },
        remove: function () {
            if (this.tree) $.fn.zTree.destroy(this.treeId);
            DeptTree.__super__.remove.apply(this, arguments);
        }
    });

    /*
     * 审批人视图
     */
    var approvalUserItemRender = template($('#tmpl-approvalUser').html());
    var lastApprovalUserRender = template($('#tmpl-lastApprovalUser').html());
    var startUserRender = template($('#tmpl-startUser').html());
    var batchApprovalModalRender = template($('#tmpl-batchApprovalModal').html());

    var ApprovalUserItem = Backbone.View.extend({
        template: approvalUserItemRender,
        className: function () {
            var type = this.model.get('type');
            if (type == '') type = 'user';
            return 'item-approver item-' + type;
        },
        events: {
            'click [data-do="remove"]': 'doRemove'
        },
        initialize: function () {
            this.listenTo(this.model, 'remove', this.remove);
        },
        doRemove: function () {
            this.model.destroy();
        },
        render: function () {
            var markup = this.template(this.model.toJSON());
            this.$el.html(markup);
            return this;
        }
    });
    var LastApprovalUserView = ApprovalUserItem.extend({
        template: lastApprovalUserRender,
        className: 'item-approver',
        initialize: function () {
            this.listenTo(this.model, 'change', this.render);
        },
        doRemove: function () {
            this.model.clear();
            this.render();
        },
        render: function () {
            var data = this.model.toJSON();
            if (_.isEmpty(data)) {
                this.$el.empty();
            } else {
                this.$el.html(this.template({
                    model: data,
                    noDealWay: true
                }));
            }
            return this;
        }
    });
    var StartUserView = ApprovalUserItem.extend({
        template: startUserRender,
        className: 'item-approver',
        initialize: function () {
            this.listenTo(this.model, 'change', this.render);
        },
        render: function () {
            var data = this.model.toJSON();
            if (_.isEmpty(data)) {
                this.$el.empty();
            } else {
                this.$el.html(this.template({
                    model: data
                }));
            }
            return this;
        }
    });
    var ApprovalUserView = CollectionView.extend({
        ItemView: ApprovalUserItem,
        events: {
            // 'sortupdate': 'updateIndex'
        },
        initialize: function () {
            this.collection = new ApprovalUserCollection;

            ApprovalUserView.__super__.initialize.apply(this, arguments);
            this.listenTo(this.collection, 'update', this.onUpdate);
            this.listenTo(this.collection, 'add', this.addOne);

            this.listenTo(this.model, 'request', this.onRequest);
            this.listenTo(this.model, 'sync', this.onSync);
            this.listenTo(this.model, 'error', this.onError);

            //this.listenTo(Backbone, 'addApprovalUser', this.addApprovalUser);

            this.cacheEls();

            this.$el.on('shown.bs.tab', _.bind(this.changeMode, this));
            this.$items.sortable();

            this.lastApprovalUser = new ApprovalUserModel;
            new LastApprovalUserView({
                el: this.$lastApprovalUser,
                model: this.lastApprovalUser
            });

            this.startUser = new ApprovalUserModel;
            new StartUserView({
                el: this.$startUser,
                model: this.startUser
            });

            //默认插入【批量发起人员】
            var defaultMode = this.mode;
            this.mode = 1;
            var defaultUser = new DeptUserModel({
                type: 'placeholder',
                code: '$$$',
                userId: '$$$',
                userName: specialNodeName['$$$'],
                headUrl: '',
                avatar: ''
            });
            this.addApprovalUser(defaultUser);
            this.mode = defaultMode;
        },
        toggleList: function () {
            this.$items.toggle();
        },
        changeMode: function (event) {
            var $target = $(event.target);
            var href = $target.attr('href');
            if (href == '#defApprovalUser') {
                this.mode = 1;
            } else if (href == '#lastApprovalUser') {
                this.mode = 2;
            } else if (href == '#startUser') {
                this.mode = 3;
            }
        },
        onUpdate: function () {
            if (this.collection.length == 0) {
                this.$items.html('请在列表选择');
            }
        },
        onRequest: function () {
            this.$items.html('数据加载中...');
        },
        onSync: function (arguments) {
            var approvalUsers = this.model.get('approvalUsers');
            var lastApprovalUser = this.model.get('lastApprovalUser');

            var attrs = _.pick(lastApprovalUser, 'id', 'headUrl', 'userName', 'lastDealWay');

            if (!_.isEmpty(attrs)) {
                var avatar = attrs.userName;
                if (avatar.length > 2) avatar = avatar.substr(-2);
                attrs.avatar = avatar;
                attrs.userId = attrs.id;
                attrs.$color = '#' + avatarColor(attrs.userName);
            }

            this.lastApprovalUser.set(attrs);
            this.collection.reset(approvalUsers);
        },
        onError: function (model, xhr, options) {
            var message = '无法获取数据';
            var textStatus = options.textStatus;
            var errorThrown = options.errorThrown;
            if (_.isObject(errorThrown)) message = errorThrown.message;
            else message = errorThrown;

            this.$items.html('数据请求失败：' + message);
        },
        mode: 3,
        addApprovalUser: function (model) {
            if (model instanceof DeptUserModel) {
                var attrs = model.pick('type', 'userId', 'code', 'userName', 'headUrl', 'avatar');
                var color = '#' + avatarColor(attrs.userName);
                attrs.$color = color;
                if (this.mode == 1) {
                    // if (attrs.type == 'role') code = '$$$';
                    // else attrs.code = model.id;
                    if (this.collection.length == 0) this.$items.empty();
                    this.collection.add(attrs);
                } else if (this.mode == 2) {
                    if (attrs.type == 'user') {
                        this.lastApprovalUser.set(attrs);
                    }
                } else if (this.mode == 3) {
                    this.startUser.set(attrs);
                }
            }
        },
        cacheEls: function () {
            this.$items = this.$('#defApprovalUser');
            this.$startUser = this.$('#startUser');
            this.$lastApprovalUser = this.$('#lastApprovalUser');
        },
        noData: function () {
            this.$items.html('请在左侧列表选择');
        }
    });

    var BatchApprovalModal = Backbone.View.extend({
        template: batchApprovalModalRender,
        className: 'modal-batch-approval modal',
        attributes: {
            tabindex: -1,
            role: 'dialog'
        },
        subviews: {},
        events: {
            'click [data-do="save"]': 'doSave',
            //'click [data-do="import"]': 'doImport',
            'click [data-do="next"]': 'doStepChange',
            'click [data-do="prev"]': 'doStepChange'
        },
        step: 2,
        initialize: function () {
            this.$el.on('hidden.bs.modal', _.bind(this.onHidden, this));
            this.render();
        },
        setStep: function (step) {
            if (this.step === 1 && step === 2 && this.$batchIds.val() === '') {
                alert('请先导入要发起流程的人员！').delay(3); return;
            }

            this.step = step;
            this.applyStep();
        },
        applyStep: function () {
            var oneAction = this.step === 1 ? 'show' : 'hide',
                twoAction = this.step === 2 ? 'show' : 'hide';

            this.$batchApprival[oneAction]();
            this.$nextButton[oneAction]();

            this.$approvalSetting[twoAction]();
            this.$submitButton[twoAction]();
            this.$prevButton[twoAction]();
        },
        render: function () {
            var data = {};//this.model.toJSON();
            var markup = this.template(data);
            this.$el.html(markup).appendTo(document.body);
            this.cacheEls();
            this.initSubviews();
            this.initUploader();
            return this;
        },
        cacheEls: function () {
            this.$depts = this.$('[role="depts"]');
            this.$approvers = this.$('[role="approval-users"]');
            this.$batchIds = this.$('.batch-ids');
            this.$batchInfo = this.$('.batch-info');

            this.$approvalSetting = this.$('.approval-setting-body');
            this.$batchApprival = this.$('.batch-approval-body');

            this.$submitButton = this.$('[data-do=save]');
            this.$nextButton = this.$('[data-do=next]');
            this.$prevButton = this.$('[data-do=prev]');
        },
        initSubviews: function () {
            var _this = this;
            this.subviews.depts = new DeptTree({
                el: this.$depts
            });
            //var attrs = this.model.pick('id', 'name');
            var model = new Backbone.Model();
            model.parse = function (resp) {
                var parsed = {
                    approvalUsers: [],
                    lastApprovalUser: null
                };
                if (resp.success) {
                    var model = resp.model;
                    var defUserList = model.defUserList;
                    var lastUser = model.lastUser;

                    var approvalUsers = [];
                    _.each(defUserList, function (model) {
                        if (_.isObject(model)) {
                            var attrs = _.pick(model, ['type', 'code', 'userName', 'headUrl', 'avatar']);
                            attrs.code = model.id;
                            if ( specialNodeName[model.id] ) {
                                attrs.code = model.id;
                                attrs.userName = specialNodeName[model.id];
                                attrs.type = 'placeholder';
                            }
                            approvalUsers.push(attrs);
                        }
                    });
                    if (_.isObject(lastUser)) lastUser.lastDealWay = model.lastDealWay;
                    parsed.lastApprovalUser = lastUser;
                    parsed.approvalUsers = approvalUsers;
                }
                return parsed;
            };
            this.subviews.approvers = new ApprovalUserView({
                el: this.$approvers,
                model: model
            });

            this.subviews.depts.on('addApprovalUser', function (model) {
                _this.subviews.approvers.addApprovalUser(model);
            });
        },
        initUploader: function () {
            var _this = this;
            var uploadOption = {
                url: CONTEXT_PATH + '/approval/importUserInfo.do',
                //url: CONTEXT_PATH + '/file/importData.do',
                button: this.$('[data-do="import"]'),
                callback: function (res) {
                    if (res.success) {
                        _this.doImport(res.model);
                    }
                }
            };
            setUploader(uploadOption);
            //this.$('[data-do="import"]').on('click', function () { _this.doImport('123'); });
        },
        doImport: function (userId) {
            alert('导入成功！').delay(3);
            this.$batchIds.val(userId.join(','));
            this.$batchInfo.text('共导入' + userId.length + '名人员');
        },
        doSave: function () {
            _this = this;
            //var taskId = this.model.get('id');
            //var batchIds = this.$batchIds.val();
            var data = this.$approvers.serializeObject();
            /*if (data.code.length === 0) {
                alert('请先配置审批流程！'); return;
            }*/
            if (!data.startUserId || data.startUserId.length === 0) {
                alert('请先选择发起人！'); return;
            }
            /*if (data.startUserId !== '$$$' && data.code.indexOf('$$$') < 0) {
                alert('批量发起人员必须在流程中！'); return;
            }*/

            /*if (typeof data.code === 'string') {
                data.code = [data.code];
            }
            data.code.reverse();*/
            /*$.ajax({
                url: CONTEXT_PATH + '/approval/batchInsertApproval.do',
                dataType: 'json',
                type: 'post',
                data: {
                    taskId: taskId,
                    typeId: id,
                    //userIds: JSON.stringify({ ids: batchIds.split(',') }),
                    //approvalIds: JSON.stringify({ ids: data.code }),
                    //readId: data.lastUserId,
                    startUserID: data.startUserId
                },
                success: function (res) {
                    if (res.success) {
                        alert('批量发起成功！', function () {
                            _this.hide();
                        });
                    }
                    else {
                        alert(res.message || '发生错误！');
                    }
                },
                error: function () {
                    alert('发生错误！');
                }
            });*/
            //this.model.setApprovalUser(data);
            //console.log(data);

            this.model.trigger('submit-publish', this.model, data.startUserId, this);
        },
        doStepChange: function (e) {
            var step = $(e.target).data('do') === 'next' ? 2 : 1;
            this.setStep(step);
        },
        show: function (model) {
            //this.model = model;
            this.setStep(2);
            this.$el.modal('show');
        },
        hide: function () {
            this.$el.modal('hide');
        },
        onHidden: function () {
            this.reset();
        },
        reset: function () {
            _.each(this.subviews, function (view) {
                view.remove();
            });
            this.remove();
        }
    });

    return BatchApprovalModal;
});
