package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.ActivitiRoleGroup;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.vo.ActivitiRoleVo;
import cmcc.mobile.admin.vo.GroupVo;
import cmcc.mobile.admin.vo.UserVo;

public interface ActivitiRoleGroupMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ActivitiRoleGroup record);

    int insertSelective(ActivitiRoleGroup group);

    int selectByPrimaryKey(ActivitiRoleGroup group);

    int updateByPrimaryKeySelective(ActivitiRoleGroup record);

    int updateByPrimaryKey(ActivitiRoleGroup record);

	int deleteRolePerson(ActivitiRoleGroup roles);

	JsonResult insertSelective(String roleId, List<Map> list);

	ActivitiRoleGroup selectByRoleId(ActivitiRoleVo role);

	List<UserVo> selectByrolePersonList(ActivitiRoleVo role);

	int addGroup(Map<String, Object> map);

	int selectByPersons(Map<String, Object> map);

	List<ActivitiRoleGroup> selectById(ActivitiRoleVo role);

	List<User> selectByUser(Map<String, Object> map2);

	List<ActivitiRoleGroup> selectByUserId(Map<String, Object> user);

	List<ActivitiRoleGroup> selectByUserOrg(Map<String, Object> user);

	List<ActivitiRoleGroup> selectByUID(String uId);

	List<ActivitiRoleGroup> selectByUserIds(Map<String, Object> user);

	List<ActivitiRoleGroup> selectByUserOrgs(Map<String, Object> user);

}