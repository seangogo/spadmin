package cmcc.mobile.admin.entity;

import java.util.Date;

public class AssignPermissions {
    private String thirdCompanyId;

    private String companyId;

    private Date updatetime;

    private String status;

    private String type;
    
    private String processdureId;

    public String getThirdCompanyId() {
        return thirdCompanyId;
    }

    public void setThirdCompanyId(String thirdCompanyId) {
        this.thirdCompanyId = thirdCompanyId == null ? null : thirdCompanyId.trim();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId == null ? null : companyId.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

	public String getProcessdureId() {
		return processdureId;
	}

	public void setProcessdureId(String processdureId) {
		this.processdureId = processdureId;
	}
    
    
}