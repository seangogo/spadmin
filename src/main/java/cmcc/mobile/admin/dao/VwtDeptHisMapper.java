package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.VwtDeptHis;

public interface VwtDeptHisMapper {
	
	List<VwtDeptHis> getDeleteByParams(Map<String,Object> map);

	List<VwtDeptHis> selectBycorpId(String ecode);
}
