package cmcc.mobile.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.AssignPermissions;
import cmcc.mobile.admin.entity.VerifyThirdCompany;
import cmcc.mobile.admin.entity.VerifyThirdInfo;

public interface PublicExportService {
	
	 VerifyThirdCompany selectByThirdCompanyId(String thirdCompanyId);
	 
	 int insert(VerifyThirdInfo record);
	 
	 VerifyThirdInfo selectByThirdCompanyIdAndUnionKey(VerifyThirdInfo verifyThirdInfo);
	 
	 List<AssignPermissions> selectAssignPerssiByThirdCompnay(Map<String, String> map);
	 
	 ApprovalType selectByCompanyIdAndTypeId(Map<String, String>map);
	 
	 List<ApprovalData> selectByParams(HashMap<String, Object> map);
	 
	 List<ApprovalTableConfig> selectByApprovalTypeId(String typeId);
	 
	 List<ApprovalTableConfigDetails> getApprovalInfoById1(String id);
	
}
