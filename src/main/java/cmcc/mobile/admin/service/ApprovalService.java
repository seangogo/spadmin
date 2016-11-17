package cmcc.mobile.admin.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.MsgSend;

public interface ApprovalService {
	@Deprecated
	JsonResult cancelApprovals(HttpServletRequest request, String typeId, Customer customer);

	JsonResult importUserInfo(HttpServletRequest request, Customer customer, Long taskId, String typeId);

	@Deprecated
	JsonResult batchInsertApproval(HttpServletRequest request, String typeId, String userIds, String approvalIds,
			String readId, String startUserID, Customer customer, String wyyId);

	boolean msgsSend(List<MsgSend> list, String companyId);
	/**
	 * 批量插入
	 * @param companyId 
	 * @param typeId 流程id
	 * @param taskId 任务id
	 * @param startUserId 发起人id
	 * @param wyyId
	 * @return
	 */
	public List<MsgSend> batchInsertApproval(String companyId, String typeId, Long taskId, String startUserId,String wyyId);
	
	/**
	 * 批量撤销
	 * @param taskId 任务id
	 */
	public void cancelApprovals(Long taskId);
}
