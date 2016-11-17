<%@ page language="java" pageEncoding="utf-8"%>
<script type="text/html" id="tmpl-loginModal">
<div class="modal-dialog">
    <div class="modal-content">
        <form class="form-login form-sole" autocomplete="off">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">用户登录</h4>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label>账号</label>
                    <input type="text" class="form-control" name="userName" placeholder="请输入手机号/邮箱/用户名">
                </div>
                <div class="form-group">
                    <label>密码</label>
                    <input type="password" class="form-control" name="password" placeholder="请输入密码">
                </div>
                <div class="clearfix">
                    <div class="checkbox pull-left">
                        <label>
                            <input type="checkbox" name="rememberme" value="1">七天内自动登录
                        </label>
                    </div>
                    <a href="${contextPath}/forget-password.action" class="pull-right">忘记密码？</a>
                </div>
                <div class="error-login alert alert-danger" role="alert" style="display: none"></div>
                <button type="submit" class="btn-login btn-submit btn">登&nbsp;&nbsp;录</button>
            </div>
        </div>
    </form>
</div>
</script>
<script type="text/html" id="tmpl-feedbackModal">
<div class="modal-dialog">
    <div class="modal-content">
        <form class="form-feedback form-sole" autocomplete="off">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">在线反馈</h4>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label>反馈类型：</label>
                    <label class="radio-inline">
                        <input type="radio" name="messageType" value="1" checked="checked"> 个人业务
                    </label>
                    <label class="radio-inline">
                        <input type="radio" name="messageType" value="2"> 企业业务
                    </label>
                    <label class="radio-inline">
                        <input type="radio" name="messageType" value="3"> 运营商业务
                    </label>
                    <label class="radio-inline">
                        <input type="radio" name="messageType" value="4"> 求职
                    </label>
                    <label class="radio-inline">
                        <input type="radio" name="messageType" value="5"> 其他
                    </label>
                </div>
                <div class="form-group">
                    <span class="required">*</span><input type="text" class="form-control" name="name" placeholder="姓名">
                </div>
                <div class="form-group">
                    <span class="required">*</span><input type="text" class="form-control" name="email" placeholder="邮箱">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" name="companyName" placeholder="公司名称">
                </div>
                <div class="form-group">
                    <input type="text" class="form-control" name="mobile" placeholder="手机号码">
                </div>
                <div class="form-group">
                    <span class="required">*</span><textarea class="form-control" name="messageQuestion" rows="3" placeholder="请输入您的问题"></textarea>
                </div>
                <div class="alert" role="alert" style="display: none"></div>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn-feedback btn-submit btn">提&nbsp;&nbsp;交</button>
            </div>
        </div>
    </form>
</div>
</script>