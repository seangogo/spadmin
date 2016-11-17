package cmcc.mobile.admin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.base.ActivitiConstant;
import cmcc.mobile.admin.dao.ActivitiHistoryTaskMapper;
import cmcc.mobile.admin.dao.TotalUserMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.ActivitiHistoryTask;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.WorkFlowService;
import cmcc.mobile.admin.util.DateUtil;
import cmcc.mobile.admin.vo.UserInfoVo;

/**
 *
 * @author renlinggao
 * @Date 2016年8月12日
 */
@Service
public class WorkFlowServiceImpl implements WorkFlowService {
	@Autowired
	private HistoryService historyService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private ActivitiHistoryTaskMapper activitiHistoryTaskMapper;

	@Autowired
	private TotalUserMapper totalUserMapper;

	@Override
	public HSSFWorkbook activitiExport(String typeId, String processStatus, String dbName) {
		// 产生工作簿对象
		HSSFWorkbook workbook = new HSSFWorkbook();

		return workbook;
	}

	@Override
	public UserInfoVo getUserInfo(String userId, String dbName) {
		return userMapper.findById(userId);
	}

	@Override
	public List<ActivitiHistoryTask> getFlowRecord(String processInstanceId) {
		Map<String, Object> paramters = new HashMap<>();
		paramters.put("processInstanceId", processInstanceId);
		List<ActivitiHistoryTask> ahtList = activitiHistoryTaskMapper.selectByQueryCriteria(paramters);
		return ahtList;
	}

	@Override
	public Map<String, List<ActivitiHistoryTask>> getActHisTaskByKey(String key) {
		Map<String, List<ActivitiHistoryTask>> resultMap = new HashMap<>();// 返回的map
		Map<String, Object> paramters = new HashMap<>();
		paramters.put("processDefinitionKey", key);
		//获取这个流程key的所有的流程历史任务信息
		List<ActivitiHistoryTask> ahtList = activitiHistoryTaskMapper.selectByQueryCriteria(paramters);
		for (ActivitiHistoryTask aht : ahtList) {
			String processInstanceId = aht.getProcessInstanceId();
			List<ActivitiHistoryTask> list = resultMap.get(processInstanceId);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(aht);
			resultMap.put(processInstanceId, list);
		}
		return resultMap;
	}

}
