package cmcc.mobile.admin.service;

import javax.servlet.http.HttpServletRequest;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.TotalUser;

public interface TotalUserService {
	
	
	public JsonResult deleteUser(HttpServletRequest request,String userId);
	
	public JsonResult addUser(TotalUser totalUser);
	
	public JsonResult updateUser(HttpServletRequest request,TotalUser totalUser);
}
