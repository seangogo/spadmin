package cmcc.mobile.admin.dao;

import cmcc.mobile.admin.entity.CompanySenceAuthority;

public interface CompanySenceAuthorityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CompanySenceAuthority record);

    int insertSelective(CompanySenceAuthority record);

    CompanySenceAuthority selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CompanySenceAuthority record);

    int updateByPrimaryKey(CompanySenceAuthority record);
    
    CompanySenceAuthority findByCompanyId(CompanySenceAuthority record);
}