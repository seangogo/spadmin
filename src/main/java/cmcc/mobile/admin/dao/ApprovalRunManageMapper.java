package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.ApprovalRunManage;

public interface ApprovalRunManageMapper {
    int deleteByPrimaryKey(String runId);

    int insert(ApprovalRunManage record);

    int insertSelective(ApprovalRunManage record);

    ApprovalRunManage selectByPrimaryKey(String runId);

    int updateByPrimaryKeySelective(ApprovalRunManage record);

    int updateByPrimaryKey(ApprovalRunManage record);
    
    int deleteById(List<String> list);
    
    List<ApprovalRunManage> selectByApprovalId(String flowId);
    
    String dateDiff(Map<String, Object> map);
    
    String selectMinDate(String approval_data_id);
	
	String selectMaxDate(String approval_data_id);
	
	List<ApprovalRunManage> getNoexecuteApproval(List<String> list);
	

}