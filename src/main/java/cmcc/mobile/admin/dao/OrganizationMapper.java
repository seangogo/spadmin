package cmcc.mobile.admin.dao;

import java.util.HashMap;
import java.util.List;

import cmcc.mobile.admin.entity.Organization;

public interface OrganizationMapper {
	int deleteByPrimaryKey(String id);

	int insert(Organization record);

	int insertSelective(Organization record);

	Organization selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(Organization record);

	int updateByPrimaryKey(Organization record);

	Organization findDeptByNameAndPreviousId(Organization record);
    
    List<Organization> selectCompanyInfoByCompanyId(String companyId);
    

    List<Organization> selectAllDept(String companyId);
    
    List<Organization> getOrgByPreId(HashMap<String,String> map);
    
    List<Organization> getOneDept(String companyId);
    
    int updateOrgByCompanyId(String companyId);
    
    
}