package cmcc.mobile.admin.service;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.AdminUser;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.vo.UserInfoVo;


/**
 *
 * @author renlinggao
 * @Date 2016年6月29日
 */
public interface UserService {
	
	public JsonResult checkPass(HttpServletRequest request,String username,String password);
	
	public Customer selectCompany(String companyId);
	
	public File saveImportFile(MultipartHttpServletRequest request) throws IllegalStateException, IOException;
	
	public Map<String, Object> saveOrgAndUser(File file,Customer c,AdminUser user,String dbName);
	
	public void saveTotalUsers(List<TotalUser> users);
	
	/**
	 * 获取一个公司的所有人员信息
	 * @param companyId
	 * @return
	 */
	public Map<String, UserInfoVo> getCompanyUserInfos(String companyId,List<String> userIds);
}
