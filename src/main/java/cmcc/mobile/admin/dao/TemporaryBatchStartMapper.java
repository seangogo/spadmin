package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.TemporaryBatchStart;

public interface TemporaryBatchStartMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(TemporaryBatchStart record);

	int insertSelective(TemporaryBatchStart record);

	TemporaryBatchStart selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(TemporaryBatchStart record);

	int updateByPrimaryKey(TemporaryBatchStart record);

	int deleteByThirdId(List<String> list);

	int batchinsert(List<TemporaryBatchStart> list);

	/**
	 * 根据taskId把插入人员数据做成删除状态
	 * 
	 * @param record
	 * @return
	 */
	int updateByTaskIdSelective(TemporaryBatchStart record);

	/**
	 * 通过任务id或者用户id查询临时消息
	 * 
	 * @param record
	 * @return
	 */
	List<TemporaryBatchStart> findByTaskIdAndUserId(TemporaryBatchStart record);
	
	/**
	 * 获取一个任务下的发起的流程的数量
	 * @param taskId
	 * @return
	 */
	int findCountByTaskId(Long taskId);
	
	/**
	 * 撤销临时表发起的没有办的流程
	 * @param taskId
	 * @return
	 */
	int cancelByTaskId(Long taskId);
	
	/**
	 * 通过手机号集合回收零时数据
	 * @param params
	 * @return
	 */
	int revokeByMobiles(Map<String, Object> params);
	
}