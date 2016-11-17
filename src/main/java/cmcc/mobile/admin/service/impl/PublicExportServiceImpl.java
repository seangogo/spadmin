package cmcc.mobile.admin.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.dao.ApprovalDataMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigDetailsMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.AssignPermissionsMapper;
import cmcc.mobile.admin.dao.VerifyThirdCompanyMapper;
import cmcc.mobile.admin.dao.VerifyThirdInfoMapper;
import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.AssignPermissions;
import cmcc.mobile.admin.entity.VerifyThirdCompany;
import cmcc.mobile.admin.entity.VerifyThirdInfo;
import cmcc.mobile.admin.service.PublicExportService;

@Service
public class PublicExportServiceImpl implements PublicExportService{
	
	@Autowired
	private VerifyThirdCompanyMapper verifyThirdCompany;
	
	@Autowired
	private VerifyThirdInfoMapper verifyThirdInfoMapper;
	
	@Autowired
	private AssignPermissionsMapper assignPermissionsMapper;
	
	@Autowired
	private ApprovalTypeMapper approvalTypeMapper;
	
	@Autowired
	private ApprovalDataMapper approvalDateMapper;
	
	@Autowired
	private ApprovalTableConfigMapper approvalTableConfigMapper;
	
	@Autowired
	private ApprovalTableConfigDetailsMapper approvalTableConfigDetailsMpper;

	@Override
	public VerifyThirdCompany selectByThirdCompanyId(String thirdCompanyId) {
		
		return verifyThirdCompany.selectByThirdCompanyId(thirdCompanyId);
	}

	@Override
	public int insert(VerifyThirdInfo record) {
		
		return verifyThirdInfoMapper.insertSelective(record);
	}

	@Override
	public VerifyThirdInfo selectByThirdCompanyIdAndUnionKey(VerifyThirdInfo verifyThirdInfo) {
		
		return verifyThirdInfoMapper.selectByThirdCompanyIdAndUnionKey(verifyThirdInfo);
	}

	@Override
	public List<AssignPermissions> selectAssignPerssiByThirdCompnay(Map<String, String> map) {
		
		return assignPermissionsMapper.selectAssignPerssiByThirdCompnay(map);
	}

	@Override
	public ApprovalType selectByCompanyIdAndTypeId(Map<String, String> map) {
		
		return approvalTypeMapper.selectByCompanyIdAndTypeId(map);
	}

	@Override
	public List<ApprovalData> selectByParams(HashMap<String, Object> map) {
		
		return  approvalDateMapper.selectByParams(map);
	}

	@Override
	public List<ApprovalTableConfig> selectByApprovalTypeId(String typeId) {
		
		return approvalTableConfigMapper.selectByApprovalTypeId(typeId);
	}

	@Override
	public List<ApprovalTableConfigDetails> getApprovalInfoById1(String id) {
		
		return approvalTableConfigDetailsMpper.getApprovalInfoById1(id);
	}
	
}
