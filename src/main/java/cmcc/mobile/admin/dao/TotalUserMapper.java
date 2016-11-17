package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.TotalUser;

public interface TotalUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(TotalUser record);

    int insertSelective(TotalUser record);

    TotalUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TotalUser record);

    int updateByPrimaryKey(TotalUser record);
    
    TotalUser getTotalUserById(String id);
    
    int updateByCompanyId(String companyId);
    
    int insertBatch(List<TotalUser> list);

	int updateByPrimaryKeySelectives(TotalUser totalUser);

	List<TotalUser> selectByTotalUser(String companyId);
}