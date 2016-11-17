package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalWyyCompany;

public interface ApprovalWyyCompanyMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ApprovalWyyCompany record);

    int insertSelective(ApprovalWyyCompany record);

    ApprovalWyyCompany selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ApprovalWyyCompany record);

    int updateByPrimaryKey(ApprovalWyyCompany record);

	List<ApprovalWyyCompany> selectByCompanyId(String companyId);
}