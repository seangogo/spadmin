package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.ActivitiTableConfig;

public interface ActivitiTableConfigMapper {
    int deleteByPrimaryKey(String id);

    int insert(ActivitiTableConfig record);

    int insertSelective(ActivitiTableConfig record);

    ActivitiTableConfig selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ActivitiTableConfig record);

    int updateByPrimaryKey(ActivitiTableConfig record);
    
    List<ActivitiTableConfig> selectByApprovalTypeId(String id);
}