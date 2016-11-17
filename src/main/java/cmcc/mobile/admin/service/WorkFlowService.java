package cmcc.mobile.admin.service;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.entity.ActivitiHistoryTask;
import cmcc.mobile.admin.vo.UserInfoVo;

/**
 *
 * @author renlinggao
 * @Date 2016年8月11日
 */
@Service
public interface WorkFlowService {
	public HSSFWorkbook activitiExport(String typeId, String processStatus,String dbName);

	public UserInfoVo getUserInfo(String userId, String dbName);

	public List<ActivitiHistoryTask> getFlowRecord(String processInstanceId);
	
	public Map<String, List<ActivitiHistoryTask>> getActHisTaskByKey(String key);
}
