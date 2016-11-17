package cmcc.mobile.admin.service;

import java.util.HashMap;
import java.util.List;

import cmcc.mobile.admin.entity.ApprovalData;

public interface IApproveDataService {

	List<ApprovalData> selectByParams(HashMap<String, Object> map);
}
