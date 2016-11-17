package cmcc.mobile.admin.dao;

import cmcc.mobile.admin.entity.VerifyThirdCompany;

public interface VerifyThirdCompanyMapper {
    int deleteByPrimaryKey(Long id);

    int insert(VerifyThirdCompany record);

    int insertSelective(VerifyThirdCompany record);

    VerifyThirdCompany selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(VerifyThirdCompany record);

    int updateByPrimaryKey(VerifyThirdCompany record);
    
   VerifyThirdCompany selectByThirdCompanyId(String thirdCompanyId);
}