package cmcc.mobile.admin.service;
import java.util.List;
import java.util.Map;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.vo.ActivitiRoleVo;

public interface ActivitiRoleService {

	JsonResult addRole(ActivitiRoleVo role);

	JsonResult editRole(ActivitiRoleVo role);

	JsonResult deteleRole(ActivitiRoleVo role);

	JsonResult roleList(ActivitiRoleVo role);

	JsonResult getAllDepet(String companyId);

	JsonResult deleteRolePerson(ActivitiRoleVo role);


	JsonResult rolePersonList(ActivitiRoleVo role);

	JsonResult addRolePerson(String roleId, String[] userId);

	JsonResult getNextNode(String userId,String companyId,List<Map<String, Object>> list);

}
