package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;

import cmcc.mobile.admin.entity.ActivitiHistoryTask;

public interface ActivitiHistoryTaskMapper {
	public List<ActivitiHistoryTask> selectByQueryCriteria(Map<String, Object> paramters);
	
	public List<HistoricVariableInstance> selectHistoricVariableInstanceByNativeQuery(Map<String, Object> paramters);
}
