package cmcc.mobile.admin.dao;


import java.util.List;

import cmcc.mobile.admin.entity.MsgSend;

public interface MsgSendMapper {
	
	int insertSelective(MsgSend record);
	
	/**
	 * 批量插入
	 * @param list
	 * @return
	 */
	int insertBatch(List<MsgSend> list);
	
}
