package cmcc.mobile.admin.vo;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;

/**
 *
 * @author renlinggao
 * @Date 2016年5月19日
 */
public class ActivitiTypeVo extends ApprovalType {
	String forms;
	String flow;
	String flowConfigID;
	public String getFlowConfigID() {
		return flowConfigID;
	}

	public void setFlowConfigID(String flowConfigID) {
		this.flowConfigID = flowConfigID;
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

	public ActivitiTypeVo(){
		
	}
	
	public ActivitiTypeVo(ApprovalType approvalType){
		this.setId(approvalType.getId());
		this.setDes(approvalType.getDes());
		this.setIcon(approvalType.getIcon());
		this.setName(approvalType.getName());
		this.setRemark(approvalType.getRemark());
		this.setApprovalTableConfigId(approvalType.getApprovalTableConfigId());
		this.setApprovalMostTypeId(approvalType.getApprovalMostTypeId());
		this.setThirdApprovalStartLink(approvalType.getThirdApprovalStartLink());
		this.setIsDefault(approvalType.getIsDefault());
		this.setThirdCompanyId(approvalType.getThirdCompanyId());
		this.setIsBoutique(approvalType.getIsBoutique());
		this.setStatus(approvalType.getStatus());
		this.setThirdConfigLink(approvalType.getThirdConfigLink());
	}


}
