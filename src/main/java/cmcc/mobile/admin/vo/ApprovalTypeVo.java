package cmcc.mobile.admin.vo;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;

/**
 *
 * @author renlinggao
 * @Date 2016年5月19日
 */
public class ApprovalTypeVo extends ApprovalType {
	List<ApprovalTableConfigDetails> control;
	
	public ApprovalTypeVo(){
		
	}
	
	public ApprovalTypeVo(ApprovalType approvalType){
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

	public List<ApprovalTableConfigDetails> getControl() {
		return control;
	}

	public void setControl(List<ApprovalTableConfigDetails> control) {
		this.control = control;
	}

}
