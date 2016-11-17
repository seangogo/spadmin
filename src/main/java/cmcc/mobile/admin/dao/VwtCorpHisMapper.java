package cmcc.mobile.admin.dao;

import java.util.List;
import java.util.Map;

import cmcc.mobile.admin.entity.VwtCorpHis;

public interface VwtCorpHisMapper {
	List<VwtCorpHis> getDeleteByParams(Map<String,Object> map);
}
