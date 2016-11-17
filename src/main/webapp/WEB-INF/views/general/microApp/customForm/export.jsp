<%@ page language="java" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/views/common/head-public.jsp"%>
<%@ include file="/WEB-INF/views/common/ie.jsp"%>
<link rel="stylesheet"
	href="${contextPath}/assets/dep/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />
</head>
<body>
	<!-- header -->
	<%@ include file="/WEB-INF/views/common/header.jsp"%>
	<!-- end header -->
	<div class="container-fluid container-spadmin">
		<div class="row row-spadmin">
			<div class="col-spadmin col-sm-3 col-md-2 col-lg-2">
				<div class="panel panel-spadmin menu-spadmin">
					<div class="panel-heading">产品</div>
					<div class="list-group">
						<a href="${contextPath}/microApp/customForm/manager.do" class="list-group-item">审批管理</a>
						<a href="javascript:" class="list-group-item active">数据导出</a>
					</div>
				</div>
			</div>
			<div class="col-spadmin col-sm-9 col-md-10 col-lg-10">
				<div class="panel panel-spadmin panel-spadmin-content">
					<div class="panel-heading">数据导出</div>
					<div class="panel-body">
						<form class="form-horizontal"
							action="${contextPath}/export/exportExcel.do" method="post">
							<div class="form-group">
								<label class="col-sm-2 control-label" for="inputType">导出类型</label>
								<div class="col-sm-3">
									<select name="id" id="inputType" class="form-control">
										<c:forEach items="${data}" var="item">
											<option value="${item.id}">${item.name}</option>
										</c:forEach>
									</select>
								</div>
								<div class="col-sm-3">
									<select name="status" class="form-control">
										<option value="">请选择类型状态</option>
										<c:forEach items="${status}" var="item">
											<option value="${item.status}">${item.name}</option>
										</c:forEach>
									</select>
								</div>
							</div>
							<!-- <div class="form-group">
								<label for="inputTitle" class="col-sm-2 control-label">标题</label>
								<div class="col-sm-6">
									<input type="text" class="form-control" name="title"
										id="inputTitle">
								</div>
							</div> -->
							<div class="form-group">
								<label for="inputFlowId" class="col-sm-2 control-label">审批编号</label>
								<div class="col-sm-6">
									<input type="text" class="form-control" name="flowId"
										id="inputFlowId">
								</div>
							</div>
							<div class="form-group">
								<label class="col-sm-2 control-label">发起时间</label>
								<div class="col-sm-3">
									<input type="text" class="form-control"
										id="applyStartTimePicker"> <input type="hidden"
										name="applyStartTime" id="applyStartTime">
								</div>
								<div class="col-sm-3">
									<input type="text" class="form-control" id="applyEndTimePicker">
									<input type="hidden" name="applyEndTime" id="applyEndTime">
								</div>
							</div>
							<!-- <div class="form-group">
								<label class="col-sm-2 control-label">完成时间</label>
								<div class="col-sm-3">
									<input type="text" class="form-control"
										id="approveStartTimePicker"> <input type="hidden"
										name="approveStartTime" id="approveStartTime">
								</div>
								<div class="col-sm-3">
									<input type="text" class="form-control"
										id="approveEndTimePicker"> <input type="hidden"
										name="approveEndTime" id="approveEndTime">
								</div>
							</div> -->
							<div class="form-group">
								<div class="col-sm-10 col-sm-offset-2">
									<button type="submit" class="btn btn-blue">导出</button>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- page scripts -->
	<script>
		seajs.use('page/general/micro-app/custom-form/export', function(page) {
			page.run();
		});
	</script>
	<!-- end page scripts -->
</body>
</html>