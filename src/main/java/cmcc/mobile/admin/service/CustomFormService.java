package cmcc.mobile.admin.service;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.ApprovalMostType;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.vo.ActivitiTypeVo;
import cmcc.mobile.admin.vo.ApprovalTableConfigVo;
import cmcc.mobile.admin.vo.ApprovalTypeVo;

/**
 *
 * @author renlinggao
 * @Date 2016年5月5日
 */
public interface CustomFormService {
	/**
	 * 添加控件
	 * @param name
	 * @param icon
	 * @param des
	 * @param control
	 * @return
	 */
	public boolean addCustomForm(String companyId,Integer scene,String mostTypeKey,String name, String icon, String des, List<ApprovalTableConfigDetails> control,String userId);
	
	
	/**
	 * 编辑表单
	 * @param id
	 * @param name
	 * @param icon
	 * @param des
	 * @param control
	 * @return
	 */
	public boolean editCustomForm(String mostTypeKey,String id,String name, String icon, String des, List<ApprovalTableConfigDetails> control);
	
	
	/**
	 * 根据类型查询
	 * @param id
	 * @return
	 */
	public List<ApprovalType> findApprovelByMostType(String companyId,String id,String wyyId);
	
	/**
	 * 查询所有大类别
	 * @returnf
	 */
	public List<ApprovalMostType> findMostType(String wyyId);
	
	/**
	 * 获取流程的表单
	 * @param id
	 * @return
	 */
	public ApprovalTypeVo getApprovalInfoById(String id);
	
	
	/**
	 * 根据id获取配置信息
	 * @param id
	 * @return
	 */
	public ApprovalTableConfigVo getDefApprovalUsers(String id);
	
	/**
	 * 设置表单默认审批人
	 * @param id
	 * @return
	 */
	public boolean setDefApprovalUsers(ApprovalTableConfig approvalTableConfig);
	
	/**
	 * 停用流程
	 * @param id
	 * @return
	 */
	public boolean stopWorkFlow(String id);
	
	/**
	 * 获取所有的流程包括删除的（用于导出）
	 * @param companyId
	 * @return
	 */
	public List<ApprovalType> getAllWorkFlow(String companyId);

	/**
	 * 获取所有的流程
	 * @return
	 */
	public List<ApprovalType> getAllWorkFlows(String companyId);
	
	/**
	 * 获取审批的excel导出
	 * @param typeId
	 * @param mobile
	 * @param companyId
	 * @return
	 */
	public HSSFWorkbook getApprovalExcel(Map<String, String> resultMap,String typeId,String mobile,String companyId);
	
	
	/**
	 * 获取activiti流程的表单
	 * @param id
	 * @return
	 */
	public ActivitiTypeVo getActivitiById(String id);
	
	/**
	 * 将前端设计好的activiti流程进行保存
	 * @param name
	 * @param icon
	 * @param des
	 * @param control
	 * @return
	 */
	public boolean addCustomActiviti(String companyId,String mostTypeKey,String name, String icon, String des, String forms, String flow,Integer sence);

	/**
	 * 将前端设计好的activiti流程进行更新
	 * @param name
	 * @param icon
	 * @param des
	 * @param control
	 * @return
	 */
	public boolean editCustomActiviti(String companyId,String id,String mostTypeKey,String name, String icon, String des, String forms, String flow);
	

	/**
	 * 将客户定义的activiti流程最新版进行部署
	 * @param id
	 * @return
	 */
	public boolean deployActivitiById(String id,String companyId);


	public JsonResult findWyy(Map<String, Object> map);

}
