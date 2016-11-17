package cmcc.mobile.admin.vo;

import cmcc.mobile.admin.entity.User;

/**
 *
 * @author renlinggao
 * @Date 2016年8月18日
 */
public class UserInfoVo extends User{
	private String orgName;

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	
}
