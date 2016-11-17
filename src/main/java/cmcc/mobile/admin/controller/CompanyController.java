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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.AdminUserMapper;
import cmcc.mobile.admin.entity.AdminUser;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.CompanyService;
import cmcc.mobile.admin.util.MD5Util;

@Controller
@RequestMapping("users")
public class CompanyController extends BaseController {

	@Autowired
	private CompanyService companyService;
	@Autowired
	private AdminUserMapper adminUserMapper;
	/**
	 * 根据缓存中的Id获取部门
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("getDeptInfo")
	@ResponseBody
	public JsonResult getDeptInfo(HttpServletRequest request) {
		JsonResult result = new JsonResult();

		String companyId = getCompany().getId();
		if (StringUtils.isEmpty(companyId)) {
			result.setMessage("用户公司Id不能为空");
			return result;
		}
		// 切换数据库
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		List<Organization> organizations = companyService.selectCompanyInfoByCompanyId(companyId);

		result.setSuccess(true);
		result.setModel(organizations);

		return result;
	}

	/**
	 * 根据orgId获取人员信息
	 * 
	 * @param orgId
	 * @return
	 */
	@RequestMapping("getUserInfo")
	@ResponseBody
	public JsonResult getUserInfo(HttpServletRequest request ,String orgId) {
		Map<String, Object> map = new HashMap<>() ;
		String companyId = getCompany().getId() ;
		JsonResult result = new JsonResult();
		if (StringUtils.isEmpty(orgId)) {
			result.setMessage("Id不能为空");
			return result;
		}
		if (!orgId.equals(getCompany().getDeptId())) {
			result.setMessage("没有权限访问");
			return result;
		}
		map.put("orgId", orgId) ;
		map.put("companyId", companyId) ;
		List<User> users = companyService.selectUserInfoByOrg(map);
		result.setModel(users);
		result.setSuccess(true);

		return result;

	}

	/**
	 * 修改密码
	 * 
	 * @param prePwd
	 * @param newPwd
	 * @param surePwd
	 * @param request
	 * @return
	 */
	@RequestMapping("modifyPwd")
	@ResponseBody
	public JsonResult changePwd(String prePwd, String newPwd, String surePwd, HttpServletRequest request) {
		JsonResult result = new JsonResult();

		if (null == getUser() || StringUtils.isEmpty(getUser().getMobile())) {
			result.setMessage("缓存中不存在");
			return result;
		}

		MultipleDataSource.setDataSourceKey(null);
		//String password = companyService.selectPwdByMobile(getUser().getMobile());
		String mobile = getUser().getMobile() ;
		if (StringUtils.isEmpty(prePwd) || StringUtils.isEmpty(newPwd) || StringUtils.isEmpty(surePwd)) {
			result.setMessage("密码不能为空");
		} else {
			AdminUser user = new AdminUser() ;
			user.setMobile(mobile);
			user.setPassword(prePwd);
			int count = adminUserMapper.selectPwd(user) ;
			if (count==0) {
				result.setMessage("密码不正确，请重新输入");
			} else {
				if (!newPwd.equals(surePwd)) {
					result.setMessage("重置密码不一致，请确认！");
				} else {
					Map<String, String> map = new HashMap<String, String>();
					map.put("password", MD5Util.encrypt(newPwd));
					if (StringUtils.isEmpty(getUser().getId())) {
						result.setMessage("用户Id为空");
						return result;
					}
					map.put("id", getUser().getId());
					int m = companyService.updatePwdByPrimaryKey(map);
					if (m == 1) {
						result.setSuccess(true);
						result.setMessage("修改成功");
					}
				}
			}
		}

		return result;
	}

	/**
	 * 获取所有部门
	 * 
	 * @param selfCompanyId
	 *            调用该接口的第三方企业Id
	 * @param secretKey
	 *            验证密钥
	 * @return
	 */
	@RequestMapping("getAllDept")
	@ResponseBody
	public JsonResult getAllDept(String deptId, String selfCompanyId, String secretKey, String companyId) {
		JsonResult result = new JsonResult();
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		String conpanyId = getCompany().getId();
		List<Organization> organizations = companyService.selectAllDept(conpanyId);
		if (organizations != null && organizations.size() != 0) {
			result.setSuccess(true);
			result.setModel(organizations);
		} else {
			result.setSuccess(false);
			result.setMessage("没有部门");
		}
		return result;

	}

	
	/**
	 * 根据部门Id获取部门人员
	 * @param deptId
	 * @param selfCompanyId
	 * @param secretKey
	 * @param companyId
	 * @param pageNum 第几页
	 * @param pageSize 分页数量
	 * @param isPage 是否分页
	 * @return
	 */
	@RequestMapping("getUserByDeptId")
	@ResponseBody
	public JsonResult getUserByDeptId(String deptId, String selfCompanyId, String secretKey, String companyId,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
			@RequestParam(value = "isPage", defaultValue = "0") int isPage) {
		
		JsonResult result = new JsonResult();
		Map<String, Object> map = new HashMap<>() ;
		if (deptId == null || deptId.trim().length() == 0) {
			result.setSuccess(false);
			result.setMessage("Id不存在");
			return result;
		}
		List<User> users = new ArrayList<User>();
		switch (isPage) {
		case 0:
			map.put("companyId", companyId) ;
			map.put("deptId", deptId) ;
			users = companyService.selectAllByOrg(map);
			result.setSuccess(true);
			result.setModel(users);
			break;
		case 1:
			PageHelper.startPage(pageNum, pageSize);
			map.put("companyId", companyId) ;
			map.put("deptId", deptId) ;
			users = companyService.selectAllByOrg(map);
			PageInfo<User> pageInfo = new PageInfo<User>(users);
			result.setSuccess(true);
			result.setModel(pageInfo);
			break;
			
		default:
			break;
		}
		return result;
	}

	/**
	 * 设置默认收藏
	 * 
	 * @param typeId
	 * @return
	 */

	@RequestMapping("isDefaultCollection")
	@ResponseBody
	public JsonResult isDefaultCollection(String typeId) {

		JsonResult result = new JsonResult();
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		// String companyId = "13705233767";
		String companyId = getCompany().getId();
		if (StringUtils.isEmpty(typeId)) {
			result.setSuccess(false);
			result.setMessage("公司Id缓存中不存在");
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("id", typeId);
		map.put("companyId", companyId);
		ApprovalType approvalType = companyService.selectDefaultByTypeId(map);
		if (null == approvalType) {
			result.setSuccess(false);
			result.setMessage("流程已被停用");
			result.setSuccess(false);
			return result;
		}

		if (StringUtils.isEmpty(approvalType.getIsDefault()) || Long.parseLong(approvalType.getIsDefault()) == 0
				|| Long.parseLong(approvalType.getIsDefault()) > 1000) {

			companyService.updateIsDefaultByTypeId1(map);
			result.setModel("1");
			result.setMessage("添加成功！");
			result.setSuccess(true);
		} else {

			companyService.updateIsDefaultByTypeId2(map);
			result.setModel("0");
			result.setMessage("取消成功");
			result.setSuccess(true);

		}

		return result;

	}

}
