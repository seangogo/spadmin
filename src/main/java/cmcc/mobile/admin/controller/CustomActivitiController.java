package cmcc.mobile.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.service.CustomFormService;

/**
 *
 * @author hanyf
 * @Date 2016年7月24日
 */
@Controller
@RequestMapping("activiti")
public class CustomActivitiController extends BaseController {
	@Autowired
	private CustomFormService customFormService;

	/**
	 * 从页面新建/更新activiti流程
	 * 
	 * @param name
	 * @param icon
	 * @param des
	 * @param forms
	 * @param flow
	 * @return
	 */
	@RequestMapping("customActiviti")
	@ResponseBody
	public JsonResult customActiviti(String id, String mostTypeKey, String name, String icon, String des, String forms,
			String flow, @RequestParam(value = "sence", defaultValue = "3") Integer sence) {
		if (id.isEmpty())
			return new JsonResult(customFormService.addCustomActiviti(getCompany().getId(), mostTypeKey, name, icon,
					des, forms, flow, sence), "", "");
		else
			return new JsonResult(customFormService.editCustomActiviti(getCompany().getId(), id, mostTypeKey, name,
					icon, des, forms, flow), "", "");
	}

	/**
	 * 获取客户配置的activiti
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("getActivitiById")
	@ResponseBody
	public JsonResult getActivitiById(String id) {
		return new JsonResult(true, "", customFormService.getActivitiById(id));
	}

	/**
	 * 将客户自定义activiti流程部署到系统中
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("deployActivitiById")
	@ResponseBody
	public JsonResult deployActivitiById(String id) {
		return new JsonResult(true, "", customFormService.deployActivitiById(id, getCompany().getId()));
	}

}
