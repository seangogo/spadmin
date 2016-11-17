package cmcc.mobile.admin.entity;

public class ActivitiTableConfig {
    private String id;

    private String date;

    private String status;

    private String approvalTypeId;
    
    private String forms;
    
    private String flow;
    
	private String lastUserId;

    private String lastDealWay;

    private String userId;

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getApprovalTypeId() {
		return approvalTypeId;
	}

	public void setApprovalTypeId(String approvalTypeId) {
		this.approvalTypeId = approvalTypeId;
	}

	public String getForms() {
		return forms;
	}

	public void setForms(String forms) {
		this.forms = forms;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	public String getLastUserId() {
		return lastUserId;
	}

	public void setLastUserId(String lastUserId) {
		this.lastUserId = lastUserId;
	}

	public String getLastDealWay() {
		return lastDealWay;
	}

	public void setLastDealWay(String lastDealWay) {
		this.lastDealWay = lastDealWay;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}