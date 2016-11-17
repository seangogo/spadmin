package cmcc.mobile.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.AdminRoleMapper;
import cmcc.mobile.admin.entity.AdminRole;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.CustomFormService;
import cmcc.mobile.admin.service.HyFileService;

/**
 *
 * @author renlinggao
 * @Date 2016年5月5日
 * 
 */
@Controller
@RequestMapping("microApp/customForm")
public class CustomFormController extends BaseController {
	@Autowired
	private CustomFormService customFormService;

	@Autowired
	private HyFileService hyFileService;
	@Autowired
	AdminRoleMapper roleMapper ;
	/**
	 * 新建表单插入
	 * 
	 * @param name
	 * @param icon
	 * @param des
	 * @param control
	 * @return
	 */
	@RequestMapping("customform")
	@ResponseBody
	public JsonResult customForm(String mostTypeKey, String name, String icon, String des, String control,
			@RequestParam(value = "scene", defaultValue = "1") Integer scene) {
		List<ApprovalTableConfigDetails> list = JSONArray.parseArray(control, ApprovalTableConfigDetails.class);
		return new JsonResult(
				customFormService.addCustomForm(getCompany().getId(), scene, mostTypeKey, name, icon, des, list,getUser().getId()), "",
				"");
	}

	/**
	 * 修改原来的表单
	 * 
	 * @param id
	 * @param name
	 * @param icon
	 * @param des
	 * @param control
	 * @return
	 */
	@RequestMapping("editFrom")
	@ResponseBody
	public JsonResult editFrom(String id, String mostTypeKey, String name, String icon, String des, String control) {
		List<ApprovalTableConfigDetails> list = JSONArray.parseArray(control, ApprovalTableConfigDetails.class);
		return new JsonResult(customFormService.editCustomForm(mostTypeKey, id, name, icon, des, list), "", "");
	}

	/**
	 * 获取该大类的子类审批流程以及表单数据
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("findApprovel")
	@ResponseBody
	public JsonResult findApprovel(String id, 
			HttpServletRequest request,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,@RequestParam(value = "pageSize", defaultValue = "10") int pageSize,@RequestParam(value = "wyyId", defaultValue = "wyy0001") String wyyId) {
		String companyId = getCompany().getId();
		PageHelper.startPage(pageNum, pageSize);
		List<ApprovalType> model = customFormService.findApprovelByMostType(companyId, id, wyyId);
		PageInfo<ApprovalType> pageinfo = new PageInfo<ApprovalType>(model);
		return new JsonResult(true, "", pageinfo);
	}

	/**
	 * 获取所有的审批大类
	 * 
	 * @return
	 */
	@RequestMapping("findMostType")
	@ResponseBody
	public JsonResult findMostType(HttpServletRequest request, @RequestParam(value = "wyyId", defaultValue = "wyy0001") String wyyId) {
		return new JsonResult(true, "", customFormService.findMostType(wyyId));
	}
/**
 * 获取所有产品
 */
	@RequestMapping("findWyy")
	@ResponseBody
	public JsonResult findWyy(HttpServletRequest request) {
		 Map<String, Object> map = new HashMap<>();
		 Map<String, Object> map1 = new HashMap<>();
		String userId = getUser().getId() ;
		String companyId = getCompany().getId() ;
		MultipleDataSource.setDataSourceKey("");
		map1.put("userId", userId) ;
		map1.put("companyId", companyId) ;
		AdminRole role = roleMapper.selectRoleId(map1) ;
		if(role !=null){
			String[] arr = role.getRoleId().split(",") ;
			map.put("id", arr) ;
		}
		MultipleDataSource.setDataSourceKey("business1");
		return customFormService.findWyy(map);
	}
	/**
	 * 根据产品大类获取产品小类及数据
	 * 
	 */
	@RequestMapping("findApprovelWyy")
	@ResponseBody
	public JsonResult findApprovelWyy(String id, 
			HttpServletRequest request,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,@RequestParam(value = "pageSize", defaultValue = "10") int pageSize,@RequestParam(value = "wyyId", defaultValue = "wyy0001") String wyyId) {
		String companyId = getCompany().getId();
		PageHelper.startPage(pageNum, pageSize);
		List<ApprovalType> model = customFormService.findApprovelByMostType(companyId, id, wyyId);
		PageInfo<ApprovalType> pageinfo = new PageInfo<ApprovalType>(model);
		return new JsonResult(true, "", pageinfo);
	}
	
	/**
	 * 启用停用流程
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("stopApprovel")
	@ResponseBody
	public JsonResult stopApprovel(String id) {
		String mess = "";
		boolean isOk = customFormService.stopWorkFlow(id);
		if (!isOk) {
			mess = "该流程不存在";
		}
		return new JsonResult(isOk, mess, "");
	}

	/**
	 * 获取表单数据
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("getApprovalById")
	@ResponseBody
	public JsonResult getApprovalById(String id) {
		return new JsonResult(true, "", customFormService.getApprovalInfoById(id));
	}

	/**
	 * 获取表单配置数据
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("getDefApprovalUsers")
	@ResponseBody
	public JsonResult getDefApprovalUsers(String id) {
		return new JsonResult(true, "", customFormService.getDefApprovalUsers(id));
	}

	/**
	 * 设置默认审批人
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("setDefApprovalUsers")
	@ResponseBody
	public JsonResult setDefApprovalUsers(String id,
			@RequestParam(value = "defaultApprovalUserIds", defaultValue = "") String defaultApprovalUserIds,
			@RequestParam(value = "lastUserId", defaultValue = "") String lastUserId, String lastDealWay) {
		// 初始化更新数据
		ApprovalTableConfig approvalTableConfig = new ApprovalTableConfig();
		approvalTableConfig.setId(id);
		approvalTableConfig.setDefaultApprovalUserIds(defaultApprovalUserIds);
		approvalTableConfig.setLastUserId(lastUserId);
		approvalTableConfig.setLastDealWay(lastDealWay);
		return new JsonResult(customFormService.setDefApprovalUsers(approvalTableConfig), "", "");
	}

	/**
	 * 导出
	 * 
	 * @param typeId
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("createApprovalImprot")
	public void createApprovalImprot(String typeId, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String mobile = getUser().getMobile();
		String companyId = getCompany().getId();
		Map<String, String> resultMap = new HashMap<String, String>();
		HSSFWorkbook workbook = customFormService.getApprovalExcel(resultMap, typeId, mobile, companyId);

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("content-disposition",
				"attachment;filename=" + new String(resultMap.get("name").getBytes("utf-8"), "iso8859-1") + ".xls");
		OutputStream out = response.getOutputStream();
		workbook.write(out);
		workbook.close();
		out.flush();
		out.close();
	}

	/**
	 * 导入
	 * 
	 * @param mr
	 * @param request
	 * @return
	 */
	@RequestMapping("importData")
	@ResponseBody
	public JsonResult importData(Long taskId,String id, MultipartHttpServletRequest mr, HttpServletRequest request) {
		JsonResult result = new JsonResult();
		return hyFileService.importData(taskId,id, request, result);
	}
}
