package cmcc.mobile.admin.dao;

import cmcc.mobile.admin.entity.ApprovalAuthority;

public interface ApprovalAuthorityMapper {
    int deleteByPrimaryKey(String id);

    int insert(ApprovalAuthority record);

    int insertSelective(ApprovalAuthority record);

    ApprovalAuthority selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ApprovalAuthority record);

    int updateByPrimaryKey(ApprovalAuthority record);
}