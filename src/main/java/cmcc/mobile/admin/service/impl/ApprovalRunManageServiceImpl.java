package cmcc.mobile.admin.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.dao.ApprovalRunManageMapper;
import cmcc.mobile.admin.entity.ApprovalRunManage;
import cmcc.mobile.admin.service.ApprovalRunManageService;


@Service
public class ApprovalRunManageServiceImpl implements ApprovalRunManageService{
	
	@Autowired
	private ApprovalRunManageMapper managerMapper;
	
	@Override
	public String dateDiff(String id) {
		String min = managerMapper.selectMinDate(id);
		String max = managerMapper.selectMaxDate(id);
		if (StringUtils.isEmpty(min) || StringUtils.isEmpty(max))
			return "";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("min", min);
		map.put("max", max);

		String diff = managerMapper.dateDiff(map);

		String str = "";

		if (StringUtils.isNotEmpty(diff)) {
			String[] times = diff.split(":");
			if (!times[0].equals("00")) {
				int hours = new Integer(times[0]);
				if (hours >= 24) {
					int day = hours / 24;
					str += day + "天";
					int hour = hours % 24;
					if (hour > 0) {
						str += hour + "小时";
					}
				} else {
					str += hours + "小时";
				}
			}
			if (!times[1].equals("00")) {
				int minuters = new Integer(times[1]);
				str += minuters + "分";
			}
			if (!times[2].equals("00")) {
				int second = new Integer(times[2]);
				str += second + "秒";
			}
		}
		return str;
	}

	@Override
	public List<ApprovalRunManage> selectByApprovalId(String flowId) {
		
		return managerMapper.selectByApprovalId(flowId);
	}

}
