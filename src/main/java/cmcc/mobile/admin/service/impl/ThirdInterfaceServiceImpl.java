package cmcc.mobile.admin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.OrganizationMapper;
import cmcc.mobile.admin.dao.TotalUserMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.service.iThirdInterfaceService;

@Service
public class ThirdInterfaceServiceImpl implements iThirdInterfaceService{

	@Autowired
	private OrganizationMapper organizationMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	
	
	/**
	 * 增加部门
	 */
	public JsonResult addOrganzation(String companyId,Organization org,String dbName) {
		JsonResult json = new JsonResult();
		String pre_id = org.getPreviousId();
		String orgname = org.getOrgName();
		List<Organization>  orgList = new ArrayList<Organization>();
		
		if(!"".equals(pre_id)){ //不是一级部门
			Organization organization = new Organization();
			organization = organizationMapper.selectByPrimaryKey(pre_id);
			if(organization==null){
				json.setSuccess(false);
				json.setMessage("父部门不存在!");
				return json;
			}
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("id",pre_id);
			map.put("companyId",companyId);
			orgList = organizationMapper.getOrgByPreId(map);
			for(int i=0;i<orgList.size();i++){
				if(orgname.equals(orgList.get(i).getOrgName())){
					json.setSuccess(false);
					json.setMessage("部门名称已存在");
					return json;
				}
			}
			Organization preOrg = new Organization();
			preOrg = organizationMapper.selectByPrimaryKey(pre_id);
			String fullname = preOrg.getOrgFullname();
			fullname = fullname + "/" + orgname;
			org.setOrgFullname(fullname);
			org.setStatus("1");
			organizationMapper.insertSelective(org);
			json.setSuccess(true);
		}else{
			orgList = organizationMapper.getOneDept(companyId);
			for(int i=0;i<orgList.size();i++){
				if(orgname.equals(orgList.get(i).getOrgName())){
					json.setSuccess(false);
					json.setMessage("部门名称已存在");
					return json;
				}
			}
			org.setStatus("1");
			org.setOrgFullname(orgname);
			organizationMapper.insertSelective(org);
			json.setSuccess(true);
		}
		return json;
		
	}

	
	/**
	 * 删除部门
	 */
	public JsonResult deleteOrganzation(String companyId, String orgId) {
		
		JsonResult json = new JsonResult();
		List<Organization> orgList = new ArrayList<Organization>();
		List<User> userList = new ArrayList<User>();
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("id",orgId);
		map.put("companyId",companyId);
		orgList = organizationMapper.getOrgByPreId(map);
		if(orgList.size()>0){
			json.setSuccess(false);
			json.setMessage("删除失败，该部门下有子部门!");
			return json;
		}
		
		userList = userMapper.getUserByOrgId(orgId);
		if(userList.size()>0){
			json.setSuccess(false);
			json.setMessage("删除失败，该部门下有人员!");
			return json;
		}
		
		Organization organization = new Organization();
		organization = organizationMapper.selectByPrimaryKey(orgId);
		
		if(organization==null){
			json.setSuccess(false);
			json.setMessage("该部门已被删除");
			return json;
		}
		
		organization.setStatus("9");
		organizationMapper.updateByPrimaryKey(organization);
		json.setSuccess(true);
		
		return json;
	}


	
	/**
	 * 更改部门
	 */
	public JsonResult updateDeptByUserId(String companyId, String orgId, String userId) {
		JsonResult json = new JsonResult();
		User user = new User();
		user = userMapper.getUserById(userId);
		if(user == null){
			json.setSuccess(false);
			json.setMessage("用户不存在");
			return json;
		}
		
		Organization organization = new Organization();
		organization = organizationMapper.selectByPrimaryKey(orgId);
		if(organization==null){
			json.setSuccess(false);
			json.setMessage("部门不存在");
			return json;
		}
		
		user.setOrgId(orgId);
		userMapper.updateByPrimaryKey(user);
		json.setSuccess(true);
		return json;
	}


	/**
	 * 编辑部门
	 */
	public JsonResult updateDept(String companyId, String orgId, String orgName, String showindex) {
		JsonResult json = new JsonResult();
		Organization organization = new Organization();
		organization = organizationMapper.selectByPrimaryKey(orgId);
		if(organization==null){
			json.setMessage("部门不存在");
			json.setSuccess(false);
			return json;
		}
		
		String pre_id = organization.getPreviousId();
		List<Organization> orgList = new ArrayList<Organization>();
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("id",pre_id);
		map.put("companyId",companyId);
		orgList = organizationMapper.getOrgByPreId(map);
		
		if(orgList!=null && orgList.size()>0){
			for(int i=0;i<orgList.size();i++){
				if(orgList.get(i).getOrgName().equals(orgName) && !orgList.get(i).getId().equals(orgId)){
					json.setSuccess(false);
					json.setMessage("部门名称已存在");
					return json;
				}
			}
		}
		String[] fullorgnames = organization.getOrgFullname().split("/");
		fullorgnames[fullorgnames.length-1] = orgName;
		String fullorgname = "";
		for(int i=0;i<fullorgnames.length;i++){
			if(i==fullorgnames.length-1){
				fullorgname =fullorgname + fullorgnames[i];
			}else{
				fullorgname =fullorgname + fullorgnames[i]+"/";
			}
		}
		
		organization.setOrgName(orgName);
		organization.setOrgFullname(fullorgname);
		organization.setShowindex(Integer.parseInt(showindex));
		
		if(organizationMapper.updateByPrimaryKey(organization)>0)
			json.setSuccess(true);
		else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		return json;
	}
	
	
	/**
	 * 增加用户
	 */
	public JsonResult addUser(User user,String companyId) {
		
		JsonResult json = new JsonResult();
		
		String mobile = user.getMobile();
		User uu = new User();
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("mobile", mobile);
		map.put("companyId",companyId);
	
		uu = userMapper.selectByMobile(map);
		if(uu!=null){
			json.setMessage("手机号已经存在");
			json.setSuccess(false);
			return json;
		}
		
		
		if(!"".equals(user.getWorkNumber()) && user.getWorkNumber()!=null){
			User ur = new User();
			HashMap<String,String> m = new HashMap<String,String>();
			m.put("workNumber", user.getWorkNumber());
			m.put("companyId",companyId);
			ur = userMapper.getInfoByWorkNumber(m);
			if(ur!=null){
				json.setMessage("工号已经存在");
				json.setSuccess(false);
				return json;
			}
		}
		
		
		
		if(userMapper.insertSelective(user)>0){
			json.setSuccess(true);
		}else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		
		return json;
	}


	/**
	 * 删除人员
	 */
	public JsonResult deleteUser(String userId, HttpServletRequest request) {
		JsonResult json = new JsonResult();
		
		User user = new User();
		user = userMapper.getUserById(userId);
		if(user==null){
			json.setSuccess(false);
			json.setMessage("人员不存在");
			return json;
		}
		
		user.setStatus("9");
		if(userMapper.updateByPrimaryKeySelective(user)>0){
			json.setSuccess(true);
			
		}else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		
		return json;
	}



	public JsonResult updateUser(User user,String companyId) {
		JsonResult json = new JsonResult();
		
		if(!"".equals(user.getWorkNumber()) && user.getWorkNumber()!=null){
			User ur = new User();
			HashMap<String,String> m = new HashMap<String,String>();
			m.put("workNumber", user.getWorkNumber());
			m.put("companyId",companyId);
			ur = userMapper.getInfoByWorkNumber(m);
			if(ur!=null){
				if(!ur.getId().equals(user.getId())){
					json.setMessage("工号已经存在");
					json.setSuccess(false);
					return json;
				}
			}
		}
		
		if(userMapper.updateByPrimaryKeySelective(user)>0){
			json.setSuccess(true);
		}else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		return json;
	}


	

	
	
}
