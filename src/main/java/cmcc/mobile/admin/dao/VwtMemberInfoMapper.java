package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.VwtMemberInfo;

public interface VwtMemberInfoMapper {
	
	List<VwtMemberInfo> getUserInfoByCorpId(String corpId);
	
	List<VwtMemberInfo> getUserInfoByParams(Map<String,Object> map);

	List<VwtMemberInfo> selectBycorpId(String ecode);

	List<VwtMemberInfo> selectBycorp(String ecode);
}
