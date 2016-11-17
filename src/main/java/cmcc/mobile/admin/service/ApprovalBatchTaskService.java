package cmcc.mobile.admin.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Workbook;

import cmcc.mobile.admin.entity.ApprovalBatchTask;

/**
 *
 * @author renlinggao
 * @Date 2016年8月26日
 */
public interface ApprovalBatchTaskService {
	/**
	 * 插入
	 * @param task
	 */
	public void addTask(ApprovalBatchTask task);
	
	/**
	 * 条件查询
	 * @param task
	 * @return
	 */
	public List<ApprovalBatchTask> findByCondition(ApprovalBatchTask task);
	
	/**
	 * 删除
	 * @param id
	 */
	public void delete(Long id);
	
	/**
	 * 编辑
	 * @param task
	 */
	public void edit(ApprovalBatchTask task);
	
	/**
	 * 名字唯一性校验
	 * @param task
	 * @return
	 */
	public ApprovalBatchTask checkName(ApprovalBatchTask task);
	
	/**
	 * 导出
	 * @param taskId
	 * @return
	 */
	public Map<String, Object> export(Long taskId,String companyId);
	
	/**
	 * 撤回
	 * @param request
	 * @param taskId
	 * @return
	 */
	public String taskRevoke(HttpServletRequest request,Long taskId);

}
