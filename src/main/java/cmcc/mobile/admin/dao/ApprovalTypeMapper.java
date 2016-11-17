package cmcc.mobile.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.ApprovalType;

public interface ApprovalTypeMapper {
	int deleteByPrimaryKey(String id);

	int insert(ApprovalType record);

	int insertSelective(ApprovalType record);

	ApprovalType selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(ApprovalType record);

	int updateByPrimaryKey(ApprovalType record);

	List<ApprovalType> findByMostTypeId(Map<String, String> params);
	
	List<ApprovalType> getAllWorkFlow(ApprovalType record);
    
	ApprovalType getApprovalTypeById(HashMap<String,String> map);

    ApprovalType selectByName(String name);
    
    ApprovalType getApprovalTypeById2(HashMap<String,String> map);
    
    int updateIsDefaultByTypeId1(Map<String, String>map);
    
    int updateIsDefaultByTypeId2(Map<String, String>map);
    
    ApprovalType  selectDefaultByTypeId(Map<String, String>map);
    
    ApprovalType selectByCompanyIdAndTypeId(Map<String, String>map);
}