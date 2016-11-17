package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.VerifyThirdInfo;

public interface VerifyThirdInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(VerifyThirdInfo record);

    int insertSelective(VerifyThirdInfo record);

    VerifyThirdInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(VerifyThirdInfo record);

    int updateByPrimaryKey(VerifyThirdInfo record);
    
    VerifyThirdInfo selectByThirdCompanyIdAndUnionKey(VerifyThirdInfo verifyThirdInfo);
}