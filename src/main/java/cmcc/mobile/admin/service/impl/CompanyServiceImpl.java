package cmcc.mobile.admin.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.dao.AdminUserMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.OrganizationMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.AdminUser;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.service.CompanyService;

@Service("companyServiceImp")
public class CompanyServiceImpl implements CompanyService{
	
	@Autowired
	private OrganizationMapper organizationMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private AdminUserMapper adminUserMapper;
	
	@Autowired
	private ApprovalTypeMapper approvalTypeMapper;

	@Override
	public List<Organization> selectCompanyInfoByCompanyId(String companyId) {
		
		return organizationMapper.selectCompanyInfoByCompanyId(companyId);
		
	}

	@Override
	public List<User> selectUserInfoByOrg(Map<String, Object> map) {
		
		return userMapper.selectUserInfoByOrg(map);
	}
	@Override
	public List<User> selectUserInfoByOrgId(String orgId) {
		
		return userMapper.selectUserInfoByOrgId(orgId);
	}

	@Override
	public int updatePwdByPrimaryKey(Map<String, String> map) {
		
		return adminUserMapper.updatePwdByPrimaryKey(map);
	}

	@Override
	public List<Organization> selectAllDept(String companyId) {
		
		return organizationMapper.selectAllDept(companyId);
	}

	@Override
	public List<User> selectAllByOrgId(String id) {
		
		return userMapper.selectAllByOrgId(id);
	}

	@Override
	public String selectPwdByMobile(String mobile) {
		
		return adminUserMapper.selectPwdByMobile(mobile);
	}

	@Override
	public int updateIsDefaultByTypeId1(Map<String, String> map) {
		
		return approvalTypeMapper.updateIsDefaultByTypeId1(map);
	}

	@Override
	public int updateIsDefaultByTypeId2(Map<String, String> map) {
		
		return approvalTypeMapper.updateIsDefaultByTypeId2(map);
	}

	@Override
	public ApprovalType selectDefaultByTypeId(Map<String, String> map) {
		
		return approvalTypeMapper.selectDefaultByTypeId(map);
	}

	@Override
	public List<User> selectAllByOrg(Map<String, Object> map) {
		return userMapper.selectAllByOrg(map);
	}





}
