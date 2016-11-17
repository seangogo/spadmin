define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore'),
        Backbone = require('backbone');
    var go = require('go');
    var makeGo = go.GraphObject.make;
    var sessionHelper = require('storageHelper').session;

    var urls = {
        'flowManager': CONTEXT_PATH + '/flow-e4/flowManager.do'
    };
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

    //这个配置需要和mobile项目中, edition-third下workflow-detail.js中的配置相匹配
    var buttonsSet = [
        { value: '1', text: '同意拒绝' },
        { value: '2', text: '同意拒绝转发' },
        { value: '3', text: '提交' },
        { value: '4', text: '同意驳回' },
        { value: '5', text: '同意驳回转发' },
        { value: '6', text: '同意拒绝驳回' },
        { value: '7', text: '同意转发' }
    ];

    var template = require('template');
    var alert = require('component/Alert');
    var FormSelectModal = require('component/flow/FormSelectModal');
    var RoleSelectModal = require('component/flow/RoleSelectModal');

    var showPorts = function (node, show) {
        var diagram = node.diagram;
        if (!diagram || diagram.isReadOnly || !diagram.allowLink) return;
        node.ports.each(function (port) {
            port.stroke = (show ? 'white' : null);
        });
    };
    var createLinkTemplate = function () {
        var linkConfig = {
                routing: go.Link.AvoidsNodes,
                curve: go.Link.JumpOver,
                corner: 5, toShortLength: 4,
                relinkableFrom: false,
                relinkableTo: true,
                reshapable: false,
                resegmentable: true,
                mouseEnter: function (e, link) { link.findObject('HIGHLIGHT').stroke = 'rgba(30,144,255,0.2)'; },
                mouseLeave: function (e, link) { link.findObject('HIGHLIGHT').stroke = 'transparent'; }
                //data: { condition: '', text: '同意', isCondition: true, category: 'Link' }
            };
        var linkPanel = makeGo(go.Panel, 'Auto',
                { visible: true, name: 'LABEL', segmentIndex: 2, segmentFraction: 0.5 },
                new go.Binding('visible', 'visible').makeTwoWay(),
                makeGo(go.Shape, 'RoundedRectangle',
                    { fill: '#F8F8F8', stroke: null }),
                makeGo(go.TextBlock, '同意',
                    {
                        name: 'NAMELABEL',
                        textAlign: 'center',
                        font: '10pt helvetica, arial, sans-serif',
                        stroke: '#333333',
                        editable: false
                    },
                    new go.Binding('text').makeTwoWay()
                )
            );

        var template = makeGo(go.Link, linkConfig,
            new go.Binding('points').makeTwoWay(),
            new go.Binding('toPortId').makeTwoWay(),
            new go.Binding('fromPortId').makeTwoWay(),
            makeGo(go.Shape, { isPanelMain: true, strokeWidth: 8, stroke: 'transparent', name: 'HIGHLIGHT' }),
            makeGo(go.Shape, { isPanelMain: true, stroke: 'gray', strokeWidth: 2 }),
            makeGo(go.Shape, { toArrow: 'standard', stroke: null, fill: 'gray' }),
            linkPanel
        );

        return template;
    };
    var createNodeTemplate = function (options) {
        options = options || {};
        var panelRect = makeGo(go.Shape, 'RoundedRectangle', {
                    stroke: null,
                },
                new go.Binding('figure', 'figure'),
                new go.Binding('fill', 'color'),
                new go.Binding('width', 'width'),
                new go.Binding('height', 'height')
            );
        var panelText = makeGo(go.TextBlock, {
                    name: 'NAMELABEL',
                    font: 'bold 11pt Helvetica, Arial, sans-serif',
                    stroke: 'whitesmoke',
                    margin: 5,
                    textAlign: 'center',
                    wrap: go.TextBlock.WrapFit,
                    editable: false
                },
                new go.Binding('text').makeTwoWay()
            );
        var nodePanel = makeGo(go.Panel, 'Auto', panelRect, panelText);

        var portConfig = {
            desiredSize: new go.Size(8, 8),
            fill: 'transparent',
            stroke: null,
            fromSpot: go.Spot.Right,
            toSpot: go.Spot.Left,
            fromMaxLinks: options.fromMaxLinks,
            toMaxLinks: options.toMaxLinks,
            cursor: 'pointer'
        };
        var inConfig = _.extend({}, portConfig, { fromLinkable: false, toLinkable: true }),
            outConfig = _.extend({}, portConfig, { fromLinkable: true, toLinkable: false });

        var nodeTemplate = makeGo(go.Node, 'Spot', [
                    new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
                    {
                        locationSpot: go.Spot.Center,
                        mouseEnter: function (e, obj) { showPorts(obj.part, true); },
                        mouseLeave: function (e, obj) { showPorts(obj.part, false); }
                    }
                ],
                nodePanel,
                makeGo(go.Shape, 'Circle', inConfig,
                    { portId: 'in', alignment: go.Spot.Left, alignmentFocus: go.Spot.Left }),
                makeGo(go.Shape, 'Circle', outConfig,
                    { portId: 'out', alignment: go.Spot.Right, alignmentFocus: go.Spot.Right })
            );

        return nodeTemplate;
    };

    var NodeStyles = {
        'Task': {
            figure: 'RoundedRectangle',
            color: '#00A9C9',
            width: 100, height: 50
        },
        'MultiTask': {
            figure: 'RoundedRectangle',
            color: '#FF943E',
            width: 100, height: 50
        },
        'CounterSignTask': {
            figure: 'RoundedRectangle',
            color: '#3FF4EA',
            width: 100, height: 50
        },
        'Xor': {
            figure: 'Diamond',
            color: '#52B97F',
            width: 120, height: 70
        },
        'TimeLimitedTask': {
            figure: 'Circle',
            color: '#9F13F6',
            width: 80, height: 80
        },
        'LoopTask': {
            figure: 'Octagon',
            color: '#F31129',
            width: 80, height: 80
        },
        'End': {
            figure: 'Circle',
            color: '#A03636',
            width: 60, height: 60
        }
    };
    var NodeList = {
        'normal': [
            _.defaults({
                    category: 'Task', text: '任务',
                    extendForm: false,
                    extendFormEditable: false,
                    groupsType: false,
                    user: {
                        mode: 0, //0:user 1:role/group
                        list: []
                    },
                    form: null,
                    buttons: '1'
                }, NodeStyles.Task),
            _.defaults({
                    category: 'MultiTask', text: '多人任务',
                    extendForm: false,
                    extendFormEditable: false,
                    groupsType: false,
                    user: {
                        mode: 0, //0:user 1:role/group
                        list: []
                    },
                    form: null,
                    buttons: '1',
                    sequential: 'false',
                    //add
                    suName: '',
                    suUser: {
                        mode: 0,
                        list: []
                    },
                    suForm: null,
                    suButtons: '1'
                }, NodeStyles.MultiTask),
            _.defaults({
                    category: 'CounterSignTask', text: '会签任务',
                    extendForm: false,
                    extendFormEditable: false,
                    groupsType: false,
                    user: {
                        mode: 0, //0:user 1:role/group
                        list: []
                    },
                    form: null,
                    buttons: '1'
                }, NodeStyles.CounterSignTask),
            _.defaults({
                    category: 'Xor', text: '条件', defaultLine: '',
                    conditionType: 0
                }, NodeStyles.Xor),
            _.defaults({
                    category: 'End', text: '结束'
                }, NodeStyles.End)
        ],
        'advanced': [
            _.defaults({
                    category: 'TimeLimitedTask', text: '限时任务',
                    mainUser: '',   //主办人员 变量名
                    assistUser: '', //协办人员 变量名
                    endTime: '',    //结束时间 变量名
                    remainTime: '', //距离结束多少小时短信提醒 变量名
                    form: null,
                    buttons: '1',
                    rvUser: { mode: 0, list: [] },  //审核人
                    etUser: { mode: 0, list: [] },  //延期人
                    delayForm: null
                }, NodeStyles.TimeLimitedTask),
            _.defaults({
                    category: 'LoopTask', text: '循环任务'
                }, NodeStyles.LoopTask),
        ]
    };
    var initDiagram = function () {
        var diagramSetting = {
            initialContentAlignment: go.Spot.Center,
            allowDrop: true,
            'animationManager.duration': 800
            /*layout: makeGo(go.TreeLayout, {
                layerSpacing: 90,
                nodeSpacing: 20,
                alignment: go.TreeLayout.AlignmentCenterSubtrees
            })*/
        };
        var diagram = makeGo(go.Diagram, 'diagram', diagramSetting);
        var paletteSetting = {
            'animationManager.isEnabled': false
        };
        var palette = makeGo(go.Palette, 'palette', paletteSetting);
        var advPalette = makeGo(go.Palette, 'adv-palette', paletteSetting);
        //normal node
        diagram.nodeTemplateMap.add('Task', createNodeTemplate({ fromMaxLinks: 1 }));
        diagram.nodeTemplateMap.add('MultiTask', createNodeTemplate({ fromMaxLinks: 1 }));
        diagram.nodeTemplateMap.add('CounterSignTask', createNodeTemplate({ fromMaxLinks: 1 }));
        diagram.nodeTemplateMap.add('Xor', createNodeTemplate());
        diagram.nodeTemplateMap.add('End', createNodeTemplate({ fromMaxLinks: 0 }));
        //link
        diagram.linkTemplateMap.add('Link', createLinkTemplate());
        //advanced node
        diagram.nodeTemplateMap.add('TimeLimitedTask', createNodeTemplate({ fromMaxLinks: 1 }));
        diagram.nodeTemplateMap.add('LoopTask', createNodeTemplate({ fromMaxLinks: 1 }));

        diagram.toolManager.linkingTool.temporaryLink.routing = go.Link.Orthogonal;
        diagram.toolManager.relinkingTool.temporaryLink.routing = go.Link.Orthogonal;

        palette.nodeTemplateMap = diagram.nodeTemplateMap;
        palette.model.nodeDataArray = NodeList.normal;
        advPalette.nodeTemplateMap = diagram.nodeTemplateMap;
        advPalette.model.nodeDataArray = NodeList.advanced;

        return diagram;
    };

    var renderCache = {};
    var getRender = function (category) {
        if (!renderCache[category]) {
            renderCache[category] = template($('#tmpl-' + category).html());
        }
        return renderCache[category];
    };

    var attrDefines = {
        'Task': ['text', 'extendForm', 'extendFormEditable', 'groupsType', 'buttons'],
        'MultiTask': ['text', 'extendForm', 'extendFormEditable', 'groupsType', 'buttons', 'sequential',
            'suName', 'suButtons'],
        'CounterSignTask': ['text', 'extendForm', 'extendFormEditable', 'groupsType', 'buttons'],
        'Xor': ['text', 'conditionType'],
        'End': ['text'],
        'Link': ['text', 'condition'],
        'TimeLimitedTask': ['text', 'mainUser', 'assistUser', 'endTime', 'remainTime', 'buttons'],
        'LoopTask': ['text']
    };
    var AttrView = Backbone.View.extend({
        events: {
            'input *': 'change',
            'change *': 'change',
            'click .btn-selectform': 'doSelectForm',
            'click .btn-selectrole': 'doSelectRole'
        },
        initialize: function (options) {
            this.forms = options.forms;
            this.initSubviews();
        },
        initSubviews: function () {
            this.subview = {
                formSelect: new FormSelectModal({ forms: this.forms }),
                roleSelect: new RoleSelectModal()
            };
            this.listenTo(this.subview.formSelect, 'selected', this.selectForm);
            this.listenTo(this.subview.roleSelect, 'selected', this.selectRole);
        },
        render: function () {
            if (this.selectedItem == null) {
                this.$el.empty();
            }
            else {
                var render = getRender(this.selectedItem.category);
                var renderData = _.extend({}, this.selectedItem.data, { buttonsSet: buttonsSet });
                this.$el.html(render(renderData));
            }
        },
        bind: function (item) {
            this.selectedItem = item;
            this.render();
        },
        clear: function () {
            this.bind(null);
        },
        serializeAttrs: function () {
            var $el = this.$el,
                data = {};
            var attrs = attrDefines[this.selectedItem.category];
            _.each(attrs, function (attr, index) {
                var $ctrl = $el.find('[name=' + attr + ']');
                data[attr] = $ctrl.is('[type=checkbox]') ? $ctrl.prop('checked') : $ctrl.val();
            });
            return data;
        },
        change: function (e) {
            var selectedItem = this.selectedItem;
            var formdata = this.serializeAttrs();
            _.each(formdata, function (attr, key) {
                selectedItem.data[key] = attr;
                if (key === 'text') {
                    var label = selectedItem.findObject('NAMELABEL');
                    if (label && label.visible) {
                        label.text = attr;
                    }
                }
            });
        },
        doSelectForm: function (e) {
            var forAttr = $(e.target).data('for');
            this.forAttr = forAttr;
            this.subview.formSelect.show();
        },
        selectForm: function (form) {
            if (this.selectedItem != null) {
                this.selectedItem.data[this.forAttr] = form;
                this.render();
            }
        },
        doSelectRole: function (e) {
            var forAttr = $(e.target).data('for');
            this.forAttr = forAttr;

            var multi = $(e.target).data('multi') == false ? false : true;
            var enableViews = null,
                enableStr = $(e.target).data('enable');
            if (enableStr && enableStr.length > 0) {
                enableViews = {
                    'User': false,
                    'Role': false,
                    'Group': false
                };
                _.each(enableStr.split(','), function (item) {
                    enableViews[item] = true;
                });
            }
            this.subview.roleSelect.show(multi, enableViews);
        },
        selectRole: function (mode, data) {
            var mode = mode.toLowerCase() === 'user' ? 0 : 1;
            this.selectedItem.data[this.forAttr] = {
                mode: mode,
                list: data
            };
            this.render();
        }
    });

    var nodeTypeConst = {
        'Task': 1,              //普通任务
        'MultiTask': 2,         //多人任务
        'Xor': 3,               //条件
        'TimeLimitedTask': 4,   //限时任务
        'CounterSignTask': 5,   //会签任务
        'End': 6,               //结束
        'LoopTask': 7           //循环任务
    };
    var converts = {
        'Task': function (id, node) {
            var user = {
                users: [],
                groups: [],
                groupsType: node.groupsType ? 1 : 0
            };
            if (node.user.mode === 0) {
                user.users = node.user.list;
            }
            else {
                user.groups = node.user.list;
            }

            var data = {
                id: id,
                name: node.text,
                user: user,
                form: {
                    success: node.extendForm,
                    writable: node.extendFormEditable,
                    formID: node.form ? (node.form.id || '') : ''
                },
                buttons: node.buttons,
                loc: node.loc
            };

            return data;
        },
        'MultiTask': function (id, node) {
            var user = {
                users: [],
                groups: [],
                groupsType: node.groupsType ? 1 : 0
            };
            if (node.user.mode === 0) {
                user.users = node.user.list;
            }
            else {
                user.groups = node.user.list;
            }

            var suUser = {
                users: [],
                groups: [],
                groupsType: 1
            };
            if (node.suUser.mode === 0) {
                suUser.users = node.suUser.list;
            }
            else {
                suUser.groups = node.suUser.list;
            }

            var data = {
                id: id,
                name: node.text,
                user: user,
                form: {
                    success: node.extendForm,
                    writable: node.extendFormEditable,
                    formID: node.form ? (node.form.id || '') : ''
                },
                buttons: node.buttons,
                sequential: node.sequential,
                loc: node.loc,

                suName: node.suName,
                suUser: suUser,
                suForm: {
                    success: true,
                    writable: false,
                    formID: node.suForm ? (node.suForm.id || '') : ''
                },
                suButtons: node.suButtons
            };

            return data;
        },
        'CounterSignTask': function (id, node) {
            var user = {
                users: [],
                groups: [],
                groupsType: node.groupsType ? 1 : 0
            };
            if (node.user.mode === 0) {
                user.users = node.user.list;
            }
            else {
                user.groups = node.user.list;
            }

            var data = {
                id: id,
                name: node.text,
                user: user,
                form: {
                    success: node.extendForm,
                    writable: node.extendFormEditable,
                    formID: node.form ? (node.form.id || '') : ''
                },
                buttons: node.buttons,
                loc: node.loc
            };

            return data;
        },
        'Xor': function (id, node) {
            var data = {
                id: id,
                name: node.text,
                loc: node.loc,
                defaultLine: '',
                conditionType: parseInt(node.conditionType)
            };
            return data;
        },
        'Link': function (id, link, nodeMap) {
            var fromNode = nodeMap[link.from.toString()],
                toNode = nodeMap[link.to.toString()];
            var data = {
                id: id,
                name: link.text,
                condition: link.condition,
                points: link.points,
                from: fromNode.id,
                from_type: nodeTypeConst[fromNode.category],
                to: toNode.id,
                to_type: nodeTypeConst[toNode.category]
            };

            return data;
        },
        'TimeLimitedTask': function (id, node) {
            var rvUser = {
                    users: node.rvUser.list,
                    groups: [],
                    groupsType: 1
                },
                etUser = {
                    users: node.etUser.list,
                    groups: [],
                    groupsType: 1
                };

            var data = {
                id: id,
                name: node.text,
                loc: node.loc,
                mainUser: node.mainUser,
                assistUser: node.assistUser,
                endTime: node.endTime,
                remainTime: node.remainTime,
                form: {
                    success: true,
                    writable: false,
                    formID: node.form ? (node.form.id || '') : ''
                },
                buttons: node.buttons,
                rvUser: rvUser,
                etUser: etUser,
                delayForm: {
                    success: true,
                    writable: false,
                    formID: node.delayForm ? (node.delayForm.id || '') : ''
                }
            };
            return data;
        },
        'LoopTask': function (id, node) {
            var data = {
                id: id,
                name: node.text,
                loc: node.loc
            };
            return data;
        },
        'End': function (id, node) {
            var data = {
                id: id,
                name: node.text,
                loc: node.loc
            };
            return data;
        }
    };
    var convertItem = function (id, item, nodeMap) {
        return converts[item.category](id, item, nodeMap);
    };
    var convertJsonData = function (json) {
        typeof json === 'string' ? json = JSON.parse(json) : void(0);
        var flowJson = {
            Lines: {},
            Tasks: {},
            MultiTasks: {},
            CounterSignTasks: {},
            Xors: {},
            Ends: {},
            TimeLimitedTasks: {},
            LoopTasks: {}
        };
        var nodeMap = {};
        var itemCount = 1;

        _.each(json.nodeDataArray, function (item) {
            var id = item.category + itemCount.toString();
            nodeMap[item.key.toString()] = {
                id: id,
                category: item.category
            };

            flowJson[item.category + 's'][id] = convertItem(id, item);
            itemCount++;
        });
        _.each(json.linkDataArray, function (item) {
            var id = item.category + itemCount.toString();
            flowJson.Lines[id] = convertItem(id, item, nodeMap);
            itemCount++;
        });

        return flowJson;
    };

    var reConvertUser = function (itemUser) {
        var user = { mode: 0 };
        if (itemUser.groups.length > 0) {
            user.mode = 1;
            user.list = itemUser.groups;
        }
        else {
            user.list = itemUser.users;
        }
        return user;
    };
    var reConverts = {
        'Tasks': function (item, forms) {
            var form = item.form.formID && item.form.formID !== ''
                ? { id: item.form.formID, name: forms[item.form.formID].formName }
                : null;

            return _.defaults({
                'category': 'Task',
                'text': item.name,
                'extendForm': item.form.success,
                'extendFormEditable': item.form.writable,
                'groupsType': item.user.groupsType === 1,
                'user': reConvertUser(item.user),
                'form': form,
                'buttons': item.buttons,
                'key': item.id,
                'loc': item.loc
            }, NodeStyles.Task);
        },
        'MultiTasks': function (item, forms) {
            var form = item.form.formID && item.form.formID !== ''
                ? { id: item.form.formID, name: forms[item.form.formID].formName }
                : null;

            var suForm = item.suForm.formID && item.suForm.formID !== ''
                ? { id: item.suForm.formID, name: forms[item.suForm.formID].formName }
                : null;

            return _.defaults({
                'category': 'MultiTask',
                'text': item.name,
                'extendForm': item.form.success,
                'extendFormEditable': item.form.writable,
                'groupsType': item.user.groupsType === 1,
                'user': reConvertUser(item.user),
                'form': form,
                'buttons': item.buttons,
                'key': item.id,
                'sequential': item.sequential,
                'loc': item.loc,
                'suName': item.suName,
                'suUser': reConvertUser(item.suUser),
                'suForm': suForm,
                'suButtons': item.suButtons
            }, NodeStyles.MultiTask);
        },
        'CounterSignTasks': function (item, forms) {
            var form = item.form.formID && item.form.formID !== ''
                ? { id: item.form.formID, name: forms[item.form.formID].formName }
                : null;

            return _.defaults({
                'category': 'CounterSignTask',
                'text': item.name,
                'extendForm': item.form.success,
                'extendFormEditable': item.form.writable,
                'groupsType': item.user.groupsType === 1,
                'user': reConvertUser(item.user),
                'form': form,
                'buttons': item.buttons,
                'key': item.id,
                'loc': item.loc
            }, NodeStyles.CounterSignTask);
        },
        'Xors': function (item, forms) {
            return _.defaults({
                category: 'Xor',
                key: item.id,
                text: item.name,
                loc: item.loc,
                defaultLine: item.defaultLine,
                conditionType: item.conditionType
            }, NodeStyles.Xor);
        },
        'Lines': function (item, forms) {
            return {
                from: item.from,
                to: item.to,
                category: 'Link',
                text: item.name,
                condition: item.condition,
                isCondition: item.from_type === 3,
                points: item.points
            };
        },
        'Ends': function (item, forms) {
            return _.defaults({
                category: 'End',
                key: item.id,
                text: item.name,
                loc: item.loc,
            }, NodeStyles.End);
        },

        'TimeLimitedTasks': function (item, forms) {
            var form = item.form.formID && item.form.formID !== ''
                ? { id: item.form.formID, name: forms[item.form.formID].formName }
                : null;
            var delayForm = item.delayForm.formID && item.delayForm.formID !== ''
                ? { id: item.delayForm.formID, name: forms[item.delayForm.formID].formName }
                : null;

            return _.defaults({
                category: 'TimeLimitedTask',
                key: item.id,
                text: item.name,
                loc: item.loc,
                mainUser: item.mainUser,
                assistUser: item.assistUser,
                endTime: item.endTime,
                remainTime: item.remainTime,
                form: form,
                buttons: item.buttons,
                rvUser: reConvertUser(item.rvUser),
                etUser: reConvertUser(item.etUser),
                delayForm: delayForm
            }, NodeStyles.TimeLimitedTask);
        },
        'LoopTasks': function (item, forms) {
            return _.defaults({
                category: 'LoopTask',
                key: item.id,
                text: item.name,
                loc: item.loc,
            }, NodeStyles.LoopTask);
        }
    };
    var reConvertJsonData = function (json, forms) {
        var goJson = {
            'class': 'go.GraphLinksModel',
            'nodeDataArray': [],
            'linkDataArray': []
        };

        _.each(json, function (val, key) {
            if (reConverts.hasOwnProperty(key)) {
                var arr = key === 'Lines' ? goJson.linkDataArray : goJson.nodeDataArray;
                _.each(val, function (item) {
                    arr.push(reConverts[key](item, forms));
                });
            }
        });

        return goJson;
    };

    var setDiagramsHeight = function () {
        //height: 500px;
        var diagramsHeight = $(document.body).height() - 100;   //窗口高度减去页头高度及留空
        $('#diagram').css('height', diagramsHeight + 'px');
        var paletteHeight = NodeList.normal.length * 75,      //TODO 根据实际拥有的节点数自动计算高度
            advPaletteHeight = NodeList.advanced.length * 95;
        $('#palette').css('height', paletteHeight + 'px');
        $('#adv-palette').css('height', advPaletteHeight + 'px');

        $('.attribute-box').css('maxHeight', diagramsHeight + 'px');
        $('#palette-container').css('maxHeight', diagramsHeight + 'px');
    };
    var initPage = function (flowData) {
        setDiagramsHeight();
        var diagram = initDiagram();
        var attrView = new AttrView({
            el: '#attribute-form',
            forms: flowData.forms
        });

        var showAttributeBox = function (e) {
            var selection = diagram.selection;
            if (selection.count === 1) {
                selection.each(function (item) {
                    attrView.bind(item);
                });
            }
            else {
                attrView.clear();
            }
        };
        var showLinkLabel = function (e) {
            var link = e.subject;
            //if (e.name === 'LinkDrawn') {
                link.toPortId = 'in';
                link.fromPortId = 'out';
                link.category = 'Link';
                link.data.text = link.findObject('NAMELABEL').text;
                link.data.condition = '';
                link.data.isCondition = link.fromNode.category === 'Xor';
            //}

            if (diagram.selection.count === 1 && diagram.selection.has(link)) {
                showAttributeBox();
            }
        };
        var partCreated = function (e) {
            var part = e.subject;
        };

        diagram.addDiagramListener('LinkDrawn', showLinkLabel);
        //diagram.addDiagramListener('LinkRelinked', showLinkLabel);
        diagram.addDiagramListener('ChangedSelection', showAttributeBox);
        //diagram.addDiagramListener('ExternalObjectsDropped', partCreated);

        window.diagram = diagram;

        $('#flowinfo').on('submit', function (e) {
            e.preventDefault();
            var flowJson = convertJsonData(diagram.model.toJson());

            flowData.flow = flowJson;
            sessionHelper.setItem('flowData', flowData);
            sessionHelper.setItem('flow:lasttime', (new Date).getTime());
            location.href = urls.flowManager + '?wyyId=' + getParam('wyyId');;
        });
        $('#btn-exit').on('click', function () {
            sessionHelper.setItem('flow:lasttime', (new Date).getTime());
            location.href = urls.flowManager + '?wyyId=' + getParam('wyyId');;
        });

        var diagramData = reConvertJsonData(flowData.flow, flowData.forms);
        diagram.model = go.Model.fromJson(diagramData);
    };

    var run = function () {
        var flowData = sessionHelper.getItem('flowData');
        initPage(flowData);
    };
    module.exports = {
        run: run
    };
});
