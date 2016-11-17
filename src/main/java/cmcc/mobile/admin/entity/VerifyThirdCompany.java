package cmcc.mobile.admin.entity;

import java.util.Date;

public class VerifyThirdCompany {
    private Long id;

    private String thridCompanyId;

    private String number;

    private Date createtime;

    private Date updatetime;

    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThridCompanyId() {
        return thridCompanyId;
    }

    public void setThridCompanyId(String thridCompanyId) {
        this.thridCompanyId = thridCompanyId == null ? null : thridCompanyId.trim();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
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
}