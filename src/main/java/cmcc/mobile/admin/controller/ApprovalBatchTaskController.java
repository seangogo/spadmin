package cmcc.mobile.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.ApprovalBatchTask;
import cmcc.mobile.admin.service.ApprovalBatchTaskService;

/**
 *
 * @author renlinggao
 * @Date 2016年8月26日
 */
@Controller
@RequestMapping("task")
public class ApprovalBatchTaskController extends BaseController {

	@Autowired
	private ApprovalBatchTaskService approvalBatchTaskService;

	/**
	 * 新建任务
	 * 
	 * @param task
	 * @return
	 */
	@RequestMapping("addTask")
	@ResponseBody
	public JsonResult addTask(String taskName, String approvalTypeId) {
		JsonResult result = new JsonResult(true, null, null);
		// 初始化数据
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setTaskName(taskName);
		task.setApprovalTypeId(approvalTypeId);
		task.setCompanyId(getCompany().getId());
		task.setCreateTime(new Date());
		task.setCreateUserId(getUser().getId());
		task.setStatus(0);
		approvalBatchTaskService.addTask(task);
		return result;
	}

	/**
	 * 查询任务
	 * 
	 * @param task
	 * @return
	 */
	@RequestMapping("findByTasks")
	@ResponseBody
	public JsonResult findByTasks(ApprovalBatchTask task,
			@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		JsonResult result = new JsonResult(true, null, null);

		PageHelper.startPage(pageNum, pageSize);
		List<ApprovalBatchTask> tasks = approvalBatchTaskService.findByCondition(task);
		PageInfo<ApprovalBatchTask> resultList = new PageInfo<ApprovalBatchTask>(tasks);
		result.setModel(resultList);
		return result;
	}

	/**
	 * 删除任务
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("delete")
	@ResponseBody
	public JsonResult delete(@RequestParam(value = "id", required = true) Long id) {
		JsonResult result = new JsonResult(true, null, null);
		approvalBatchTaskService.delete(id);
		return result;
	}

	/**
	 * 编辑任务
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("edit")
	@ResponseBody
	public JsonResult edit(@RequestParam(value = "taskName", required = true) String taskName,
			@RequestParam(value = "id", required = true) Long id) {
		JsonResult result = new JsonResult(true, null, null);
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(id);
		task.setTaskName(taskName);
		approvalBatchTaskService.edit(task);
		return result;
	}

	/**
	 * 名字唯一性校验
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("checkName")
	@ResponseBody
	public JsonResult checkName(ApprovalBatchTask task) {
		JsonResult result = new JsonResult(true, null, null);
		task = approvalBatchTaskService.checkName(task);
		if (task != null) {
			result.setSuccess(false);
			result.setMessage("该流程已有同名的任务");
		}
		return result;
	}

	/**
	 * 导出
	 * 
	 * @param taskId
	 * @param response
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("export")
	public void export(@RequestParam(value = "taskId", required = true) Long taskId, HttpServletResponse response)
			throws UnsupportedEncodingException {
		Map<String, Object> resultMap = approvalBatchTaskService.export(taskId, getCompany().getId());
		Workbook workbook = (Workbook) resultMap.get("workBook");
		response.setContentType("application/vnd.ms-excel");
		String fileName = new String(((String) resultMap.get("name")).getBytes("gb2312"), "iso8859-1");
		response.setHeader("content-disposition", "attachment;filename=" + fileName + ".xls");
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			workbook.write(out);
		} catch (Exception e) {
			// TODO: handle exception
			try {
				workbook.close();
				out.flush();
				out.close();
			} catch (IOException e1) {
				log.error(e1.getMessage(), e1);
			}
		}
	}

	/**
	 * 撤回
	 * 
	 * @param taskId
	 * @param request
	 * @return
	 */
	@RequestMapping("revoke")
	@ResponseBody
	public JsonResult taskRevoke(@RequestParam(value = "taskId", required = true) Long taskId,
			HttpServletRequest request) {
		JsonResult result = new JsonResult(true, null, null);
		String mess = approvalBatchTaskService.taskRevoke(request, taskId);
		result.setMessage(mess);
		return result;
	}

}
