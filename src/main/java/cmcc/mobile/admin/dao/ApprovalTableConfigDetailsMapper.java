package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;

public interface ApprovalTableConfigDetailsMapper {
	int deleteByPrimaryKey(String id);

	int insert(ApprovalTableConfigDetails record);

	int insertSelective(ApprovalTableConfigDetails record);

	ApprovalTableConfigDetails selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(ApprovalTableConfigDetails record);

	int updateByPrimaryKeyWithBLOBs(ApprovalTableConfigDetails record);

	int updateByPrimaryKey(ApprovalTableConfigDetails record);

	List<ApprovalTableConfigDetails> getApprovalInfoById(String id);
	
	List<ApprovalTableConfigDetails> getApprovalInfoById1(String id);

	String getControlIdByName(String controlName);
}