define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore'),
        Backbone = require('backbone');
    var template = require('template');
    var alert = require('component/Alert');

    var modalTemplate = '<div class="modal-dialog">\
            <div class="modal-content">\
                <div class="modal-header">\
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">\
                        <span aria-hidden="true"><span class="glyphicon glyphicon-remove"></span></span>\
                    </button>\
                    <h4 class="modal-title">选择表单</h4>\
                </div>\
                <div class="modal-body">\
                    <div class="form-select-body">\
                        <div class="approvalList-box">\
                            <ul id="approvalList" class="approvalList"></ul>\
                        </div>\
                    </div>\
                </div>\
                <div class="modal-footer">\
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>\
                </div>\
            </div>\
        </div>';
    var formItemTemplate = '<# _.each(list, function (item) { #>\
            <li><span>{{item.formName}}</span>\
            <a href="javascript:" data-do="select" data-id="{{item.formID}}" data-name="{{item.formName}}">选择</a></li>\
        <# }) #>\
        <li><span></span><a href="javascript:" data-do="select" data-id="" data-name="">不关联表单</a></li>';
    var nodataTemplate = '<li style="background:transparent;"><h4>暂无表单，请先为流程配置表单</h4></li>';

    var modalRender = template(modalTemplate);
    var itemRender = template(formItemTemplate);

    var FormSelectModal = Backbone.View.extend({
        template: modalRender,
        itemTemplate: itemRender,
        className: 'modal-form-select modal',
        attributes: {
            role: 'dialog'
        },
        events: {
            'click [data-do="select"]': 'doSelect'
        },
        initialize: function (options) {
            this.forms = options.forms;
            this.render();
            this.setList();
        },
        render: function () {
            var markup = this.template({});
            this.$el.html(markup).appendTo(document.body);
            this.cacheEls();
            
            return this;
        },
        setList: function () {
            var count = 0;
            _.each(this.forms, function () {
                count++;
            });
            if (count > 0) {
                this.$list.html(itemRender({list: this.forms}));
            }
            else {
                this.$list.html(nodataTemplate);
            }
        },
        cacheEls: function () {
            this.$list = this.$('#approvalList');
            this.$type = this.$('#approvalType');
        },
        doSelect: function (e) {
            var selectForm = { 
                name: $(e.target).data('name'),
                id: $(e.target).data('id'),
            };
            if (selectForm.id === '') { selectForm = null; }
            this.trigger('selected', selectForm).hide();
        },
        show: function () {
            this.$el.modal('show');
        },
        hide: function () {
            this.$el.modal('hide');
        },
        reset: function () {

        }
    });

    module.exports = FormSelectModal;
});