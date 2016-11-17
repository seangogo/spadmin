package cmcc.mobile.admin.controller;




import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.TotalUserService;
import cmcc.mobile.admin.service.iThirdInterfaceService;


@Controller
@RequestMapping(value = "/thirdInterface")
public class ThirdInterfaceController extends BaseController{
	
	@Autowired
	private iThirdInterfaceService thirdInterfaceService;
	
	@Autowired
	private TotalUserService totalUserService;
	
	/**
	 * 增加部门
	 * @param companyId
	 * @param orgName
	 * @param previousId
	 * @param showindex
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/saveDept")
	@ResponseBody
	public JsonResult saveDept(
			
			@RequestParam(value = "orgName", required = false) String orgName,
			@RequestParam(value = "previousId", required = false) String previousId,
			@RequestParam(value = "showindex", required = false) String showindex,
			HttpServletRequest request){
		
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		Customer company = getCompany();
		String companyId = company.getId();
		SimpleDateFormat sdf  =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 =  new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date date = new Date();
		JsonResult json = new JsonResult();
		Organization org = new Organization();
		org.setOrgName(orgName);
		org.setId(companyId + sdf2.format(date));
		org.setCreatTime(sdf.format(date));
		org.setShowindex(Integer.parseInt(showindex));
		org.setPreviousId(previousId);
		org.setCompanyId(companyId);
		
		json = thirdInterfaceService.addOrganzation(companyId, org, "");
		return json;
	}
	
	/**
	 * 删除部门
	 * @param companyId
	 * @param orgId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteDept")
	@ResponseBody
	public JsonResult deleteDept(
			@RequestParam(value = "orgId", required = false) String orgId,
			HttpServletRequest request){
		
		Customer company = getCompany();
		String companyId = company.getId();
		JsonResult json = new JsonResult();
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		json = thirdInterfaceService.deleteOrganzation(companyId, orgId);
		return json;
	}
	
	/**
	 * 人员部门更新
	 * @param companyId
	 * @param orgId
	 * @param userId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateDeptByUserId")
	@ResponseBody
	public JsonResult updateDeptByUserId(
			@RequestParam(value = "orgId") String orgId,
			@RequestParam(value = "userId") String userId,
			HttpServletRequest request){
		Customer company = getCompany();
		String companyId = company.getId();
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		JsonResult json = new JsonResult();
		json = thirdInterfaceService.updateDeptByUserId(companyId, orgId, userId);
		return json;
	}
	
	/**
	 * 编辑部门
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateDept")
	@ResponseBody
	public JsonResult updateDept(HttpServletRequest request){
		Customer company = getCompany();
		String companyId = company.getId();
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		JsonResult json = new JsonResult();
		String orgId = request.getParameter("orgId").toString();
		String orgName = request.getParameter("orgName").toString();
		String showindex = request.getParameter("showindex").toString();
		
		json = thirdInterfaceService.updateDept(companyId, orgId, orgName, showindex);
		return json;
	}
	

	
	/**
	 * 删除人员
	 * @param selfCompanyId
	 * @param companyId
	 * @param userId
	 * @param userName
	 * @param secretKey
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteUser")
	@ResponseBody
	public JsonResult deleteUser(
			@RequestParam(value = "userId") String userId,
			HttpServletRequest request){
		
		MultipleDataSource.setDataSourceKey("");
		JsonResult json = new JsonResult();
		json =totalUserService.deleteUser(request, userId);
		if(json.getSuccess()==false){
			return json;
		}
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		json = thirdInterfaceService.deleteUser(userId, request);
		return json;
	}
	
	
	/**
	 * 保存人员
	 * @param selfCompanyId
	 * @param companyId
	 * @param userName
	 * @param passWord
	 * @param isManager
	 * @param post
	 * @param mobile
	 * @param workNumber
	 * @param headImg
	 * @param showindex
	 * @param orgId
	 * @param secretKey
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/saveUser")
	@ResponseBody
	public JsonResult addUser(
			
			@RequestParam(value = "userName") String userName,
			@RequestParam(value = "post", required = false) String post,
			@RequestParam(value = "mobile") String mobile,
			@RequestParam(value = "workNumber", required = false) String workNumber,
			@RequestParam(value = "showindex", required = false) String showindex,
			@RequestParam(value = "orgId", required = false) String orgId,
		
			HttpServletRequest request){
		
		JsonResult json = new JsonResult();
		Random rd=new Random();
		SimpleDateFormat sdf  =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 =  new SimpleDateFormat("yyyyMMddHHmmssSSS");
		MultipleDataSource.setDataSourceKey("");
		Customer customer = getCompany();
		String companyId = customer.getId();
		User user = new User();
		String id = companyId+"_"+sdf2.format(new Date())+rd.nextInt(1000);
		user.setStatus("0");
		user.setCreatTime(sdf.format(new Date()));
		user.setOrgId(orgId);
		user.setUserName(userName);
		user.setShowindex(Integer.parseInt(showindex));
		user.setPost(post);
		user.setPassWord("111111");
		user.setMobile(mobile);
		user.setWorkNumber(workNumber);
		user.setId(id);
		user.setCompanyId(companyId);
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		json = thirdInterfaceService.addUser(user,companyId);
		if(json.getSuccess()==false){
			return json;
		}
		MultipleDataSource.setDataSourceKey("");
		TotalUser totalUser = new TotalUser();
		totalUser.setId(id);
		totalUser.setType("2");
	
		totalUser.setName(userName);
		totalUser.setCompanyId(companyId);
		totalUser.setStatus("0");
		totalUser.setMobile(mobile);
		totalUser.setPassword("111111");
		totalUser.setDatabaseName(getCompany().getDbname());
		
		json = totalUserService.addUser(totalUser);
		
		return json;
	}
	
	
	/**
	 * 更新人员
	 * @param selfCompanyId
	 * @param companyId
	 * @param userId
	 * @param userName
	 * @param passWord
	 * @param post
	 * @param workNumber
	 * @param showindex
	 * @param orgId
	 * @param secretKey
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "/updateUser")
	@ResponseBody
	public JsonResult updateUser(
			
			
			@RequestParam(value = "userId") String userId,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "post", required = false) String post,
			@RequestParam(value = "workNumber", required = false) String workNumber,
			@RequestParam(value = "showindex", required = false) String showindex,
			@RequestParam(value = "orgId", required = false) String orgId,
			
			HttpServletRequest request){

		JsonResult json = new JsonResult();
		
		User user = new User();
		TotalUser totalUser = new TotalUser();
		user.setId(userId);
		totalUser.setId(userId);
		user.setUserName(userName);
		totalUser.setName(userName);
		user.setPost(post);
		user.setWorkNumber(workNumber);
		user.setShowindex(Integer.parseInt(showindex));
		user.setOrgId(orgId);
		/**
		if(userId!="" || userId != null){
		
		}else if(userName!="" || userName!=null){
			user.setLoginName(userName);
			totalUser.setName(userName);
		}
		else if(post!="" || post!=null){
			user.setPost(post);
		}else if(workNumber!="" || workNumber!=null){
			user.setWorkNumber(workNumber);
		}else if(showindex!="" || showindex!=null){
			user.setShowindex(Integer.parseInt(showindex));
		}else if(orgId!="" || orgId!=null){
			user.setOrgId(orgId);
		}
		*/
		MultipleDataSource.setDataSourceKey("");
		json = totalUserService.updateUser(request, totalUser);
		
		if(json.getSuccess()==false){
			return json;
		}
		
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		json = thirdInterfaceService.updateUser(user,getCompany().getId());
		
		return json;
	}
	
}
