define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore');
    var Backbone = require('backbone');
    var template = require('template');
    var confirm = require('component/Confirm');
    var alert = require('component/Alert');
    var sessionHelper = require('storageHelper').session;
    require('jquery-util');

    var urls = {
        flowchart: CONTEXT_PATH + '/flow-e4/flowchart.do',
        editForm: CONTEXT_PATH + '/flow-e4/editform-pc.do',

        save: CONTEXT_PATH + '/activiti/customActiviti.do',
        getFlow: CONTEXT_PATH + '/activiti/getActivitiById.do',
        deploy: CONTEXT_PATH + '/activiti/deployActivitiById.do',

        manager: CONTEXT_PATH + '/microApp/customForm/manager.do'
    };
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

    var formItemsRender = template($('#tmpl-formItem').html());
    var FormListView = Backbone.View.extend({
        events: {
            'click [data-do="edit"]': 'doEdit',
            'click [data-do="remove"]': 'doRemove'
        },
        initialize: function (options) {
            this.forms = options.forms;
            this.$body = this.$('tbody');

            this.render();
        },
        render: function () {
            this.$body.html(formItemsRender({ forms: this.forms }));
        },
        doEdit: function (e) {
            var url = urls.editForm + '?id=' + $(e.target).data('id');
            this.trigger('edit', url);
        },
        doRemove: function (e) {
            var _this = this;
            confirm('确认删除该表单?', function () {
                var id = $(e.target).data('id');
                _this.trigger('remove', id);
            });
        }
    });

    var formInfoRender = template($('#tmpl-forminfo').html());
    var initInfoValue = function (flowData) {
        var $info = $('#flowinfo');

        var data = _.pick(flowData, 'id', 'name', 'des', 'mostTypeKey', 'icon');
        data.icons = iconImages;
        $info.html(formInfoRender(data));
    };
    var initIcons = function () {
        var $infos = $('#flowinfo');
        $infos.on('click', '.iconitem', function (e) {
            $infos.find('.iconitem').removeClass('selected');
            var iconId = $(this).addClass('selected').data('id');

            $infos.find('[name=icon]').val(iconId);
        });
    };
    var initPage = function (flowData) {
        var formListView = new FormListView({
            el: $('#table-flowforms'),
            forms: flowData.forms
        });
        initIcons();

        var setFlowInfo = function () {
            var infos = $('#flowinfo').serializeObject();
            flowData.id = infos.id;
            flowData.mostTypeKey = infos.mostTypeKey;
            flowData.name = infos.name;
            flowData.icon = infos.icon;
            flowData.des = infos.des;
        };
        var toEdit = function (url) {
            setFlowInfo();
            sessionHelper.setItem('flowData', flowData);
            if (url.indexOf('?') >= 0) {
                url = url + '&wyyId=' + getParam('wyyId');
            } else {
                url = url + '?wyyId=' + getParam('wyyId');
            }
            location.href = url;
        };
        var checkFormsUse = function (id) {
            var isUse = false;
            _.each(flowData.flow.Tasks, function (item, key) {
                if (id === item.form.formID) {
                    isUse = true;
                }
            });

            return isUse;
        };
        var toRemove = function (id) {
            if (checkFormsUse(id)) {
                alert('该表单已在流程中使用，请先修改流程再执行删除！');
            }
            else {
                delete flowData.forms[id];
                sessionHelper.setItem('flowData', flowData);
                formListView.render();
            }
        };

        Backbone.listenTo(formListView, 'edit', toEdit);
        Backbone.listenTo(formListView, 'remove', toRemove);
        $('#flow-chart').on('click', function () {
            toEdit(urls.flowchart);
        });
        $('#new-form').on('click', function () {
            toEdit(urls.editForm);
        });

        initInfoValue(flowData);

        var pending = false;
        $('#flowinfo').on('submit', function (e) {
            e.preventDefault();
            setFlowInfo();
            if ($.trim(flowData.name) === '') {
                alert('请输入流程名称！').delay(3); return;
            }

            var data = _.pick(flowData, 'id', 'name', 'des', 'mostTypeKey', 'icon','sence');
            data.flow = JSON.stringify(flowData.flow);
            data.forms = JSON.stringify({ forms: flowData.forms });
            data.sence = 4;
            
            $.ajax({
                url: urls.save,
                type: 'post',
                dataType: 'json',
                data: data,
                beforeSend: function () {
                    pending = true;
                },
                success: function (res) {
                    if (res.success) {
                        sessionHelper.removeItem('flowData');
                        alert('保存成功', function () {
                            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
                        });
                    }
                    else {
                        alert(res.errorMessage || '保存失败');
                    }
                },
                error: function () {
                    alert('发生错误');
                },
                complete: function () {
                    pending = false;
                }
            });
        });
        $('#btn-exit').on('click', function (e) {
            sessionHelper.removeItem('flowData');
            location.href = urls.manager + '?wyyId=' + getParam('wyyId');
        });
    };

    var defaultData = {
        id: '',
        mostTypeKey: '',
        name: '',
        icon: 'icon_bx',
        des: '',
        forms: {

        },
        flow: {

        }
    };
    var loadById = function (id) {
        $.ajax({
            url: urls.getFlow,
            type: 'get',
            dataType: 'json',
            data: {
                id: id,
                rnd: (new Date).getTime()
            },
            success: function (res) {
                if (res.success) {
                    var flowData = _.pick(res.model, 'id', 'name', 'des', 'icon');
                    flowData.mostTypeKey = res.model.approvalMostTypeId;
                    flowData.forms = res.model.forms ? JSON.parse(res.model.forms) : {};
                    if (flowData.forms.forms) flowData.forms = flowData.forms.forms;

                    flowData.flow = res.model.flow ? JSON.parse(res.model.flow) : {};
                    initPage(flowData);
                }
                else {
                    alert(res.errorMessage || '读取流程信息出错！');
                }
            }
        });
    };
    var run = function () {
        var id = getParam('flowid'),
            wyyId = getParam('wyyId');
        var flowData = sessionHelper.getItem('flowData');

        if (!flowData) {
            if (id && id.length > 0) {
                loadById(id);
            }
            else {
                flowData = _.clone(defaultData);
                initPage(flowData);
            }
        }
        else {
            var flowLastTime = sessionHelper.getItem('flow:lasttime');
            if (flowLastTime && (new Date).getTime() - flowLastTime > 60000) {
                confirm('您有未保存的流程草稿，需要载入吗？',
                function () {
                    initPage(flowData);
                }, function () {
                    sessionHelper.removeItem('flowData');
                    flowData = _.clone(defaultData);
                    initPage(flowData);
                });
            }
            else {
                initPage(flowData);
            }
        }
    };

    module.exports = {
        run: run
    };
});
