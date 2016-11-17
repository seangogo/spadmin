package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.AssignPermissions;

public interface AssignPermissionsMapper {
    int insert(AssignPermissions record);

    int insertSelective(AssignPermissions record);
    
    List<AssignPermissions> selectAssignPerssiByThirdCompnay(Map<String, String> map);
}