package cmcc.mobile.admin.controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.ActivitiRoleService;
import cmcc.mobile.admin.vo.ActivitiRoleVo;
import cmcc.mobile.admin.vo.GroupVo;



/**
 * 
 * @author wubj
 *
 */
@Controller
@RequestMapping("/role")
public class ActivitiRoleController extends BaseController{
	@Autowired
	private ActivitiRoleService roleService ;
	
	//新增角色
	@RequestMapping("/addRole")
	@ResponseBody
	public JsonResult addRole(HttpServletRequest request,ActivitiRoleVo role){
		String createId = getUser().getId() ;
		if(StringUtils.isEmpty(createId)){
			return new JsonResult(false,"用户不存在",null) ;
		}
		role.setCreateId(createId);
		if(role.getType()!=null&&StringUtils.isNotEmpty(role.getRoleName())&&StringUtils.isNotEmpty(role.getCompanyId())){
			return roleService.addRole(role) ;
		}
		return new JsonResult(false,"参数错误",null) ;
			
	}
	//修改角色
	@RequestMapping("/editRole")
	@ResponseBody
	public JsonResult editRole(HttpServletRequest request,ActivitiRoleVo role){
		String createId = getUser().getId() ;
		if(StringUtils.isEmpty(createId)){
			return new JsonResult(false,"用户不存在",null) ;
		}
		role.setCreateId(createId);
		if(StringUtils.isNoneEmpty(role.getId().toString())){
			return roleService.editRole(role) ;
		}
		return null ;
	}
	//删除
	@RequestMapping("/deleteRole")
	@ResponseBody
	public JsonResult deteleRole(HttpServletRequest request,ActivitiRoleVo role){
		String createId = getUser().getId() ;
		if(StringUtils.isEmpty(createId)){
			return new JsonResult(false,"用户不存在",null) ;
		}
		role.setCreateId(createId);
		if(StringUtils.isNoneEmpty(role.getId().toString())){
			return roleService.deteleRole(role) ;
		}
		return null ;
	}
	//启用，禁用
	@RequestMapping("/updateRoleStatus")
	@ResponseBody
	public JsonResult updateRoleStatus(HttpServletRequest request,ActivitiRoleVo role){
		String createId = getUser().getId() ;
		if(StringUtils.isEmpty(createId)){
			return new JsonResult(false,"用户不存在",null) ;
		}
		role.setCreateId(createId);
		if(StringUtils.isNoneEmpty(role.getId().toString())){
			return roleService.deteleRole(role) ;
		}
		return null ;
	}
	
	//角色list
	@RequestMapping("/roleList")
	@ResponseBody
	public JsonResult roleList(HttpServletRequest request,ActivitiRoleVo role){
		String companyId = getCompany().getId() ;
		if(StringUtils.isEmpty(companyId)){
			return new JsonResult(false,"集团不存在",null) ;
		}
		role.setCompanyId(companyId);
		return roleService.roleList(role); 
	}
	//所有部门和用户
	@RequestMapping("/getAllDepet")
	@ResponseBody
	public JsonResult getAllDepet(HttpServletRequest request){
		JsonResult result = new JsonResult();

		String companyId = getCompany().getId();
		if (StringUtils.isEmpty(companyId)) {
			result.setMessage("用户公司Id不能为空");
			return result;
		}
		// 切换数据库
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		return roleService.getAllDepet(companyId) ;
	}
	//批量新建角色/群组成员
		@RequestMapping("/addRolePerson")
		@ResponseBody
		public JsonResult addRolePerson(String roleId,String[] userId){	
		if(StringUtils.isNotEmpty(roleId)&&userId!=null){
			return roleService.addRolePerson(roleId,userId) ;
		}
			return new JsonResult(false,"参数错误",null);
		}
	//删除角色/群组成员
	@RequestMapping("/deleteRolePerson")
	@ResponseBody
	public JsonResult deleteRolePerson(HttpServletRequest request,ActivitiRoleVo role){
		String createId = getUser().getId() ;

		if(StringUtils.isEmpty(createId)){
			return new JsonResult(false,"用户不存在",null) ;
		}
		role.setCreateId(createId);
		if(StringUtils.isNotEmpty(role.getUserId())&&StringUtils.isNotEmpty(role.getId().toString())){
			return roleService.deleteRolePerson(role) ;
		}
		return new JsonResult(false,"参数错误",null) ;
	}
	//获取角色下的所有成员list
	@RequestMapping("/rolePersonList")
	@ResponseBody
	public JsonResult rolePersonList(HttpServletRequest request,ActivitiRoleVo role){
		String companyId = getCompany().getId() ;
		if(StringUtils.isEmpty(companyId)){
			return new JsonResult(false,"集团不存在",null) ;
		}
		role.setCompanyId(companyId);
		return roleService.rolePersonList(role); 
	}
	//根据首环节获取下一节点审核人
	@RequestMapping("/getNextNode")
	@ResponseBody
	public JsonResult getNextNode(String userId,String companyId){
		userId = "testenterprise_20160803100832100732" ;
		companyId = "testenterprise" ;
		Map<String, Object> map2 = new HashMap<String, Object>() ;
		Map<String, Object> map3 = new HashMap<String, Object>() ;
		List<Map<String,Object>> list = new ArrayList<>() ;
		List<GroupVo> candidateGroups = new ArrayList<GroupVo>() ;
		GroupVo vo = new GroupVo() ;
		vo.setId("testenterprise_20160803100832100732");
		vo.setName("111");
		candidateGroups.add(0, vo);
		map2.put("__formId", "flowform1469675280373") ;
		map2.put("__writable", false) ;
		map2.put("__success", true) ;
		map2.put("__groupsType", 1) ;
		map2.put("candidateGroups",null) ;
		map2.put("candidateUsers", candidateGroups) ;
		map2.put("isBelong",false) ;
		map2.put("userId", userId) ;
		map2.put("id","task1") ;
		map3.put("id", "task2") ;
		list.add(map2) ;
		list.add(map3);
//		map.put("task2",map2);
		if(StringUtils.isNotEmpty(userId)){
			return roleService.getNextNode(userId,companyId,list) ;
		}
		return new JsonResult(false,"参数错误",null) ;
	}
	// 角色页面
	@RequestMapping("/role")
	public String ActivitiRole() {
		return "activitiRole/role";
	}

		
	@RequestMapping("/group")
	public String ActivitiGroup() {
		return "activitiRole/group";
	}
	
	@RequestMapping("/permission")
	public String ActivitiPermission() {
		return "activitiRole/permission";
	}
}
