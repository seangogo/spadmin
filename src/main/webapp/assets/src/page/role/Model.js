define(function(require, exports) {
    var Backbone = require('backbone');
    var Group = Backbone.Model.extend({
        defaults: {
            type: 0,
            companyId: '',
            name: '',
            parentId: 0,
            status: 1
        }
    });

    var Role = Backbone.Model.extend({
        defaults: {
            type: 1,
            companyId: '',
            name: '',
            parentId: 0,
            status: 1
        }
    });

    var RoleUser = Backbone.Model.extend({
        defaults: {
            name: ''
        }
    });

    return {
        Group: Group,
        Role: Role,
        RoleUser: RoleUser
    }

});