package cmcc.mobile.admin.service;

import javax.servlet.http.HttpServletRequest;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.entity.User;

public interface iThirdInterfaceService {
	
	//增加部门
	JsonResult addOrganzation(String companyId,Organization record,String dbName);
		
	//删除部门
	JsonResult deleteOrganzation(String companyId,String orgId);
	
	//更改人员部门
	JsonResult updateDeptByUserId(String companyId,String orgId,String userId);
	
	//编辑部门
	JsonResult updateDept(String compangId,String orgId,String orgName,String showindex);
	
	//增加人员
	JsonResult addUser(User user,String companyId);
		
	//删除人员
	JsonResult deleteUser(String userId,HttpServletRequest request);
	
	//编辑人员
	JsonResult updateUser(User user,String companyId);
	
}
