package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.ThirdApprovalStart;


public interface ThirdApprovalStartMapper {
    int deleteByPrimaryKey(String id);

    int insert(ThirdApprovalStart record);

    int insertSelective(ThirdApprovalStart record);

    ThirdApprovalStart selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ThirdApprovalStart record);

    int updateByPrimaryKey(ThirdApprovalStart record);
    
    int deleteById(List<String> list);
    
    
    
}