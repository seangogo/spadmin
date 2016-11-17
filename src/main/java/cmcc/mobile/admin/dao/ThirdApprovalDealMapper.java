package cmcc.mobile.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.ThirdApprovalDeal;


public interface ThirdApprovalDealMapper {
    int deleteByPrimaryKey(String id);

    int insert(ThirdApprovalDeal record);

    int insertSelective(ThirdApprovalDeal record);

    ThirdApprovalDeal selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ThirdApprovalDeal record);

    int updateByPrimaryKey(ThirdApprovalDeal record);
    
    int deleteById(List<String> list);
    
    List<ThirdApprovalDeal> getDealByConfig(HashMap<String,String> map);
    
    int batchinsert(List<ThirdApprovalDeal> list);
    
    /**
     * 根据任务di撤销发起的没有办的流程
     * @param taskId
     * @return
     */
    int deleteByTaskId(Long taskId);
    
    /**
	 * 通过手机号集合回收代办数据
	 * @param params
	 * @return
	 */
	int revokeByMobiles(Map<String, Object> params);
}