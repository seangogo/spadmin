package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.UserApprovalDefData;

public interface UserApprovalDefDataMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(UserApprovalDefData record);

	int insertSelective(UserApprovalDefData record);

	UserApprovalDefData selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(UserApprovalDefData record);

	int updateByPrimaryKeyWithBLOBs(UserApprovalDefData record);

	int updateByPrimaryKey(UserApprovalDefData record);

	int updateStatusByMobile(Map<String, String> params);

	int insertBatch(List<UserApprovalDefData> list);

	/**
	 * 通过taskid把数据改为删除状态
	 * 
	 * @param record
	 * @return
	 */
	int updateByTaskIdSelective(UserApprovalDefData record);

	/**
	 * 根据任务id和手机号集合删除任务
	 * 
	 * @param params
	 * @return
	 */
	int updateByTaskIdAndMobileSelective(Map<String, Object> params);

	/**
	 * 获取任务的导入数据的值
	 * 
	 * @param record
	 * @return
	 */
	int findTaskDataCount(UserApprovalDefData record);
	
	/**
	 * 根据条件查询初始数据
	 * @param record
	 * @return
	 */
	List<UserApprovalDefData> findByCondition(UserApprovalDefData record);
}