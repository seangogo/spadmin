package cmcc.mobile.admin.dao;

import java.util.Map;

import cmcc.mobile.admin.entity.UserApprovalType;

public interface UserApprovalTypeMapper {
    int insert(UserApprovalType record);

    int insertSelective(UserApprovalType record);
    
    int deleteByUserIdAndTypeId(String id);
}