package cmcc.mobile.admin.vo;

/**
 *
 * @author renlinggao
 * @Date 2016年5月16日
 */
public class ApprovalRequestParams {
	private String id;
	private String runId;
	private String selfCompanyId = "hy";
	private String companyId;
	private String examineDate;
	private String arriveDate;
	private String nodeStatus;
	private String userId;
	private String secretKey = "0";

	private String approvalTableConfigId;
	private String approvalName;
	private String startUserId;
	private String des;
	private String status;
	private String nextNodeId = "";
	private String link;
	private String startDate;
	private String data;
	private String type;

	private String remark1 = "";
	private String remark2 = "";
	private String remark3 = "";
	private String remark4 = "";
	private String remark5 = "";
	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = "hy"+"_"+runId;
	}

	public String getSelfCompanyId() {
		return selfCompanyId;
	}

	public void setSelfCompanyId(String selfCompanyId) {
		this.selfCompanyId = selfCompanyId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getExamineDate() {
		return examineDate;
	}

	public void setExamineDate(String examineDate) {
		this.examineDate = examineDate;
	}

	public String getArriveDate() {
		return arriveDate;
	}

	public void setArriveDate(String arriveDate) {
		this.arriveDate = arriveDate;
	}

	public String getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(String nodeStatus) {
		this.nodeStatus = nodeStatus;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getApprovalTableConfigId() {
		return approvalTableConfigId;
	}

	public void setApprovalTableConfigId(String approvalTableConfigId) {
		this.approvalTableConfigId = approvalTableConfigId;
	}

	public String getApprovalName() {
		return approvalName;
	}

	public void setApprovalName(String approvalName) {
		this.approvalName = approvalName;
	}

	public String getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(String startUserId) {
		this.startUserId = startUserId;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNextNodeId() {
		return nextNodeId;
	}

	public void setNextNodeId(String nextNodeId) {
		this.nextNodeId = nextNodeId;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return remark3;
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getRemark4() {
		return remark4;
	}

	public void setRemark4(String remark4) {
		this.remark4 = remark4;
	}

	public String getRemark5() {
		return remark5;
	}

	public void setRemark5(String remark5) {
		this.remark5 = remark5;
	}

}
