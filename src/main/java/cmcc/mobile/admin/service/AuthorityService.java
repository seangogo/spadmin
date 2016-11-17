package cmcc.mobile.admin.service;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.ApprovalAuthority;

public interface AuthorityService {

	JsonResult getWyyList(String companyId);

	JsonResult getManaher(String companyId, String userId);

	JsonResult getApproval(ApprovalAuthority authority);

	JsonResult UpdateApproval(ApprovalAuthority authority);

	JsonResult getPerson(String id);

}
