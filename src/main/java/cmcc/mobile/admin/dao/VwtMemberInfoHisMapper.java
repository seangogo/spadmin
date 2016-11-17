package cmcc.mobile.admin.dao;

import cmcc.mobile.admin.entity.VwtMemberInfoHis;

import java.util.List;
import java.util.Map;

public interface VwtMemberInfoHisMapper {
	
	List<VwtMemberInfoHis> getDeleteByParams(Map<String,Object> map);

	List<VwtMemberInfoHis> selectBycorpId(String ecode);

}
