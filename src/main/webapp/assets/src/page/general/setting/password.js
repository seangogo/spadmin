define(function (require, exports, module) {
    var $ = require('jquery'),
        _ = require('underscore');
    var Backbone = require('backbone');
    var template = require('template');
    var confirm = require('component/Confirm');
    var alert = require('component/Alert');
    require('jquery-validate');

    var PasswordFromView = Backbone.View.extend({
        events: {
            'submit': 'onSubmit'
        },
        locked: false,
        initialize: function () {
            this.model.url = this.$el.attr('action');
            this.$el.validate({
                errorClass: 'error-valid',
                rules: {
                    'prePwd': {
                        required: true
                    },
                    'newPwd': {
                        required: true
                    },
                    'surePwd': {
                        required: true,
                        equalTo: '#newPwd'
                    }
                },
                messages: {
                    'prePwd': {
                        required: '请输入原密码'
                    },
                    'newPwd': {
                        required: '请输入新密码'
                    },
                    'surePwd': {
                        required: '请确认新密码',
                        equalTo: '两次新密码必须相同'
                    },
                }
            });

            this.listenTo(this.model, 'request', this.lock);
            this.listenTo(this.model, 'sync', this.done);
            this.listenTo(this.model, 'error', this.error);
        },
        onSubmit: function () {
            if (!this.locked && this.$el.valid()) {
                var data = this.$el.serializeArray();
                data.push({ name: 'selfCompanyId', value: COMPANY_ID });
                data.push({ name: 'secretKey', value: SECRET_KEY });

                this.model.fetch({
                    type: 'post',
                    dataType: 'json',
                    data: data
                });
            }
            return false;
        },
        error: function (model, response, options) {
            this.alert('请求失败');
            // this.alert(response.statusText);
            this.locked = false;
        },
        lock: function (model, xhr, options) {
            this.locked = true;
        },
        done: function (model, response, options) {
            this.locked = false;
            _.defaults(response, {
				success: false,
				model: '',
				message: '设置失败'
			});

            var data, success = false;
            if (response.success) {
                try {
                    data = JSON.parse(response.model.data);
                } catch (e) { }
                if (data && data.code == '0') {
                    success = true;
                    this.$el[0].reset();
                    alert(data.message);
                }
            }

            if (!success) {
                alert(response.message);
            }
        }
    });

    var run = function () {
        var model = new Backbone.Model;
		var passwordForm = new PasswordFromView({
			el: '#password-form',
			model: model
		});
        passwordForm.$el.show();
    };

    module.exports = {
        run: run
    };
});
