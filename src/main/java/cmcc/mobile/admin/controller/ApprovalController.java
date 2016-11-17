package cmcc.mobile.admin.controller;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.soap.Addressing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.MsgSend;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.ApprovalService;
import cmcc.mobile.admin.util.PropertiesUtil;

/**
 * 批量发起流程
 * 
 * @author Administrator
 *
 */

@Controller
@RequestMapping(value = "/approval")
public class ApprovalController extends BaseController {

	@Autowired
	private ApprovalService approvalService;

	/**
	 * 批量发起流程
	 * 
	 * @return
	 */
	@RequestMapping("batchInsertApproval")
	@ResponseBody
	public JsonResult batchInsertApproval(HttpServletRequest request, String typeId, String userIds, String approvalIds,
			String readId, String startUserID, String wyyId, Long taskId) {
		JsonResult json = new JsonResult(true,null,null);
		if (wyyId == null || wyyId.equals("")) {
			wyyId = "wyy0001";
		}

		//
		// json = approvalService.batchInsertApproval(request,
		// typeId,userIds,approvalIds,readId,startUserID,getCompany(),wyyId);
		//
		//
		// List<MsgSend> msgList = (List<MsgSend>) json.getModel();
		List<MsgSend> msgList = approvalService.batchInsertApproval(getCompany().getId(), typeId, taskId, startUserID,
				wyyId);
		MultipleDataSource.setDataSourceKey(null);
		approvalService.msgsSend(msgList, getCompany().getId());
		return json;
	}

	/**
	 * execl导入人员信息
	 */
	@RequestMapping("importUserInfo")
	@ResponseBody
	public JsonResult importUserInfo(HttpServletRequest request, Long taskId, String typeId) {
		JsonResult json = new JsonResult();
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		json = approvalService.importUserInfo(request, getCompany(), taskId, typeId);
		return json;

	}

	/**
	 * 撤销批量导入流程
	 */
	@RequestMapping("cancelApprovals")
	@ResponseBody
	public JsonResult cancelApprovals(Long taskId, HttpServletRequest request) {
		JsonResult json = new JsonResult(true,null,null);
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		approvalService.cancelApprovals(taskId);
//		json = approvalService.cancelApprovals(request, typeId, getCompany());
		return json;
	}
}
