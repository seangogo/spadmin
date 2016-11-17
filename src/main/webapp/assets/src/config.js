/* 公共页 seajs 配置和其它参数配置 */
+ function () {
    var d = (new Date).getDate();
    seajs.config(SEA_CONFIG);
}();
/* 退出方法，所有页面都要用到，暂时写在这里 */
+ function () {
    var docReady = function (fn) {
        if (document.readyState === 'complete') { 
            fn(); 
        } else if (document.addEventListener) { 
            window.addEventListener('load', function () { docReady(fn); }, false);
        }
        else { 
            window.attachEvent('onload', function () { docReady(fn); });
        }
    };
    var enableExit = function () {
        var exitBtn = document.getElementById('exit-btn');
        if (!exitBtn) { return; }
        exitBtn.addEventListener('click', function (ev) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', CONTEXT_PATH + '/exit.do', true);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
            xhr.setRequestHeader('Accept', 'text/html');
            xhr.send('');

            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    location.href = CONTEXT_PATH + '/login.do';
                }
            };
        });
    };
    docReady(enableExit);
}();
