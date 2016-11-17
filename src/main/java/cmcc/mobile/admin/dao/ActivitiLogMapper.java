package cmcc.mobile.admin.dao;

import cmcc.mobile.admin.entity.ActivitiLog;
import cmcc.mobile.admin.entity.ActivitiRole;
import cmcc.mobile.admin.vo.ActivitiRoleVo;

public interface ActivitiLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ActivitiLog record);

    int insertSelective(ActivitiLog log);

    ActivitiLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ActivitiLog record);

    int updateByPrimaryKey(ActivitiLog record);

	int insertEditSelective(ActivitiRole log);
}