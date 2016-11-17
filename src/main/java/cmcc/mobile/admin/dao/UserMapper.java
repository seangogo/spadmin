package cmcc.mobile.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.vo.UserInfoVo;

public interface UserMapper {
	int deleteByPrimaryKey(String id);

	int insert(User record);

	int insertSelective(User record);

	User selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(User record);

	int updateByPrimaryKey(User record);

	User getByMobile(User record);

	List<User> selectUserInfoByOrgId(String orgId);

	List<User> getUserByOrgId(String id);

	User getUserById(String id);

	User selectByMobile(HashMap<String, String> map);

	User getInfoByWorkNumber(HashMap<String, String> map);

	List<User> selectAllByOrgId(String id);
	
	int updateUserByCompanyId(String companyId);

	List<String> getExistMobiles(Map<String, Object> params);
	
	int batchUpdate(Map<String, Object> params);
	
	int insertBatch(List<User> list);

	int updateByPrimaryKeySelectives(User user) ;

	List<User> selectUserInfoByOrg(Map<String, Object> map);

	List<User> selectAllByOrg(Map<String, Object> map);
	
	UserInfoVo findById(String id);
	List<User> selectUser(Map<String, Object> map);
	
	/**
	 * 获取一个公司的所有人员数据
	 * @param companyId
	 * @return
	 */
	List<UserInfoVo> findByCompanyId(Map<String,Object> params);

}