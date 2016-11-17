package cmcc.mobile.admin.service;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalRunManage;

public interface ApprovalRunManageService {
	
	String dateDiff(String id);
	
	List<ApprovalRunManage> selectByApprovalId(String flowId);

}
