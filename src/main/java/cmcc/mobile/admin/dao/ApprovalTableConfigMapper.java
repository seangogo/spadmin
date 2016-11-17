package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalTableConfig;

public interface ApprovalTableConfigMapper {
    int deleteByPrimaryKey(String id);

    int insert(ApprovalTableConfig record);

    int insertSelective(ApprovalTableConfig record);

    ApprovalTableConfig selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ApprovalTableConfig record);

    int updateByPrimaryKey(ApprovalTableConfig record);
    
    List<ApprovalTableConfig> selectByApprovalTypeId(String id);
}