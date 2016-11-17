package cmcc.mobile.admin.service;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.User;

public interface CompanyService {
	
	
	List<Organization> selectCompanyInfoByCompanyId(String companyId);
	
	List<User> selectUserInfoByOrgId(String orgId);
	
	int updatePwdByPrimaryKey(Map<String, String>map);
	
	List<Organization> selectAllDept(String companyId);
	
	List<User> selectAllByOrgId(String id);
	
	String selectPwdByMobile(String mobile);
	
	int updateIsDefaultByTypeId1(Map<String, String>map);
	    
	int updateIsDefaultByTypeId2(Map<String, String>map);
	
	ApprovalType  selectDefaultByTypeId(Map<String, String>map);

	List<User> selectUserInfoByOrg(Map<String, Object> map);

	List<User> selectAllByOrg(Map<String, Object> map);

}
