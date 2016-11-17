define(function(require, exports) {
    var $ = require('jquery');
    require('bootstrap-datetimepicker');
    require('bootstrap-datetimepicker-zh-CN');

    function run() {
        var defaultSetting = {
            format: 'yyyy-mm-dd',
            language: 'zh-CN',
            minView: 2
        };
        var $applyStartTimePicker = $('#applyStartTimePicker');
        var $applyEndTimePicker = $('#applyEndTimePicker');
        var $approveStartTimePicker = $('#approveStartTimePicker');
        var $approveEndTimePicker = $('#approveEndTimePicker');

        var $applyStartTime = $('#applyStartTime');
        var $applyEndTime = $('#applyEndTime');
        var $approveStartTime = $('#approveStartTime');
        var $approveEndTime = $('#approveEndTime');

        $applyStartTimePicker.datetimepicker(defaultSetting);
        $applyEndTimePicker.datetimepicker(defaultSetting);
        $approveStartTimePicker.datetimepicker(defaultSetting);
        $approveEndTimePicker.datetimepicker(defaultSetting);

        $applyStartTimePicker.on('hide', function(event) {
            var val = $.trim($(this).val());
            if(val != '') {
                $applyStartTime.val(val + ' 00:00:00');
                // $applyEndTime.datetimepicker('setStartDate', val).datetimepicker('update');
            }
        });
        $applyEndTimePicker.on('hide', function(event) {
            var val = $.trim($(this).val());
            if(val != '') {
                $applyEndTime.val(val + ' 23:59:59');
                // $applyStartTime.datetimepicker('setEndDate', val).datetimepicker('update');
            }
        });
        $approveStartTimePicker.on('hide', function(event) {
            var val = $.trim($(this).val());
            if(val != '') {
                $approveStartTime.val(val + ' 00:00:00');
                // $approveEndTime.datetimepicker('setStartDate', val).datetimepicker('update');
            }
        });
        $approveEndTimePicker.on('hide', function(event) {
            var val = $.trim($(this).val());
            if(val != '') {
                $approveEndTime.val(val + ' 23:59:59');
                // $approveStartTime.datetimepicker('setStartDate', val).datetimepicker('update');
            }
        });
    }

    exports.run = run;
});