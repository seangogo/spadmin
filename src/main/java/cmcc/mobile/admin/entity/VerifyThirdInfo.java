package cmcc.mobile.admin.entity;

import java.util.Date;

public class VerifyThirdInfo {
    private Long id;

    private String thirdCompanyId;

    private String uKey;

    private String unionKey;

    private Date createtime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThirdCompanyId() {
        return thirdCompanyId;
    }

    public void setThirdCompanyId(String thirdCompanyId) {
        this.thirdCompanyId = thirdCompanyId == null ? null : thirdCompanyId.trim();
    }

    public String getuKey() {
        return uKey;
    }

    public void setuKey(String uKey) {
        this.uKey = uKey == null ? null : uKey.trim();
    }



	public String getUnionKey() {
		return unionKey;
	}

	public void setUnionKey(String unionKey) {
		this.unionKey = unionKey;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

    
}