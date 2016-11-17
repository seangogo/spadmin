package cmcc.mobile.admin.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.service.CustomFormService;

@Controller
public class RouteController extends BaseController {
	@Autowired
	private CustomFormService customFormService;

	// login
	@RequestMapping("/login")
	public String login(HttpServletRequest request) {
		return "login";
	}

	// microapp
	@RequestMapping("/microApp/list")
	public String microAppList() {
		return "general/microApp/list";
	}

	@RequestMapping("/microApp/customForm/manager")
	public String customFormManager(HttpServletRequest request,
			@RequestParam(value = "wyyId", defaultValue = "wyy0001") String wyyId) {
		List<Map<String, Object>> status = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		Map<String, Object> map4 = new HashMap<String, Object>();
		Map<String, Object> map5 = new HashMap<String, Object>();

		map1.put("status", 0);
		map1.put("name", "起草状态");
		map2.put("status", 1);
		map2.put("name", "流转状态");
		map3.put("status", 6);
		map3.put("name", "审批完成");
		map4.put("status", 8);
		map4.put("name", "审批驳回");
		map5.put("status", 9);
		map5.put("name", "起草人撤回");

		status.add(map1);
		status.add(map2);
		status.add(map3);
		status.add(map4);
		status.add(map5);

		request.setAttribute("status", status);
		request.setAttribute("data", customFormService.findMostType(wyyId));
		return "general/microApp/customForm/manager";
	}

	@RequestMapping("/export/home")
	public String home(HttpServletRequest request) {

		List<ApprovalType> list = customFormService.getAllWorkFlows(getCompany().getId());
		request.setAttribute("data", list);
		// 初始化状态
		List<Map<String, Object>> status = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		Map<String, Object> map4 = new HashMap<String, Object>();
		Map<String, Object> map5 = new HashMap<String, Object>();

		map1.put("status", 0);
		map1.put("name", "起草状态");
		map2.put("status", 1);
		map2.put("name", "流转状态");
		map3.put("status", 6);
		map3.put("name", "审批完成");
		map4.put("status", 8);
		map4.put("name", "审批驳回");
		map5.put("status", 9);
		map5.put("name", "起草人撤回");

		status.add(map1);
		status.add(map2);
		status.add(map3);
		status.add(map4);
		status.add(map5);

		request.setAttribute("status", status);
		return "general/microApp/customForm/export";
	}

	// edition 1 & 2
	@RequestMapping("/microApp/customForm/input-e1")
	public String customFormInputE1(HttpServletRequest request) {
		UUID uuid = UUID.randomUUID();
		request.setAttribute("UUID", uuid.toString());
		return "editionFirst/microApp/customForm/input";
	}

	@RequestMapping("/microApp/customForm/input-e2")
	public String customFormInputE2(HttpServletRequest request) {
		UUID uuid = UUID.randomUUID();
		request.setAttribute("UUID", uuid.toString());
		return "editionSecond/microApp/customForm/input";
	}

	// employee manager
	@RequestMapping("/user/manager")
	public String employeeManager() {
		return "general/employee/manager";
	}

	// password
	@RequestMapping("/users/passwordManager")
	public String settingPassword() {
		return "general/setting/password";
	}

	// flow chart
	@RequestMapping("/flow/flowchart")
	public String flowchart() {
		return "editionThird/flow/flowchart";
	}

	@RequestMapping("/flow/flowManager")
	public String flowManager(HttpServletRequest request,
			@RequestParam(value = "wyyId", defaultValue = "wyy0001") String wyyId) {
		request.setAttribute("mostType", customFormService.findMostType(wyyId));
		return "editionThird/flow/flowManager";
	}

	@RequestMapping("/flow/editform")
	public String editform(HttpServletRequest request) {
		UUID uuid = UUID.randomUUID();
		request.setAttribute("UUID", uuid.toString());
		return "editionThird/flow/editform";
	}
	
	@RequestMapping("/flow-e4/flowchart")
	public String flowchartEdition4() {
		return "editionFourth/flow/flowchart";
	}
	@RequestMapping("/flow-e4/flowManager")
	public String flowManagerEdition4(HttpServletRequest request,
			@RequestParam(value = "wyyId", defaultValue = "wyy0001") String wyyId) {
		request.setAttribute("mostType", customFormService.findMostType(wyyId));
		return "editionFourth/flow/flowManager";
	}
	@RequestMapping("/flow-e4/editform")
	public String editformEdition4(HttpServletRequest request) {
		UUID uuid = UUID.randomUUID();
		request.setAttribute("UUID", uuid.toString());
		return "editionFourth/flow/editform";
	}
	@RequestMapping("/flow-e4/editform-pc")
	public String editformEdition4PC(HttpServletRequest request) {
		UUID uuid = UUID.randomUUID();
		request.setAttribute("UUID", uuid.toString());
		return "editionFourth/flow/editform-PC";
	}
	
	@RequestMapping("/microApp/task/manager")
	public String taskmanager(HttpServletRequest request, String approvalTypeId) {
		request.setAttribute("approvalTypeId", approvalTypeId);
		return "general/microApp/task/manager";
	}

	/**
	 * 退出
	 */
	@RequestMapping("/exit")
	@ResponseBody
	public JsonResult exit(HttpServletResponse response, HttpServletRequest request) throws IOException {
		request.getSession().invalidate();// 清空session
		// return new ModelAndView("login");
		// response.sendRedirect(request.getContextPath() + "/login.do");
		return new JsonResult(true, null, null);
	}
}
