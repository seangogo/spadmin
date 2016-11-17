package cmcc.mobile.admin.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.dao.ApprovalDataMapper;
import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.service.IApproveDataService;

@Service
public class IApproveDataServiceImpl implements IApproveDataService{
	
	@Autowired
	private ApprovalDataMapper approvalDataMapper;
	
	@Override
	public List<ApprovalData> selectByParams(HashMap<String, Object> map) {
		
		return approvalDataMapper.selectByParams(map);
	}

}
