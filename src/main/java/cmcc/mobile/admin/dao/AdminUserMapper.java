package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.AdminUser;

public interface AdminUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(AdminUser record);

    int insertSelective(AdminUser record);

    AdminUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AdminUser record);

    int updateByPrimaryKey(AdminUser record);
    
    AdminUser checkPass(AdminUser record);
    
    int updatePwdByPrimaryKey(Map<String, String>map);
    
    String selectPwdByMobile(String mobile);

	int selectPwd(String prePwd, String mobile);

	int selectPwd(AdminUser user);

	List<AdminUser> selectByCompanyUser(Map<String, Object> map);
}