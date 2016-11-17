package cmcc.mobile.admin.vo;

/**
 * 数据导出
 * 
 * @author zhangxs
 *
 */
public class ExportVO {
	//数据类型Id
	private String id;
	// 发起时间
	private String applyStartTime;
	// 发起结束时间
	private String applyEndTime;
	// 导出数据类型
	private String tableConfigId;
	// 数据类型
	private String name;
	// 标题关键字
	private String title;
	// 完成开始时间
	private String approveStartTime;
	// 完成结束时间
	private String approveEndTime;
	// 审批编号
	private String flowId;
	// 状态
	private String status;
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getApplyStartTime() {
		return applyStartTime;
	}

	public void setApplyStartTime(String applyStartTime) {
		this.applyStartTime = applyStartTime;
	}

	public String getApplyEndTime() {
		return applyEndTime;
	}

	public void setApplyEndTime(String applyEndTime) {
		this.applyEndTime = applyEndTime;
	}

	public String getTableConfigId() {
		return tableConfigId;
	}

	public void setTableConfigId(String tableConfigId) {
		this.tableConfigId = tableConfigId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getApproveStartTime() {
		return approveStartTime;
	}

	public void setApproveStartTime(String approveStartTime) {
		this.approveStartTime = approveStartTime;
	}

	public String getApproveEndTime() {
		return approveEndTime;
	}

	public void setApproveEndTime(String approveEndTime) {
		this.approveEndTime = approveEndTime;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

}
