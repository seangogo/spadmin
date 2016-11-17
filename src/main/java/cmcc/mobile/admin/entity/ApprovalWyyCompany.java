package cmcc.mobile.admin.entity;

public class ApprovalWyyCompany {
    private Long id;

    private String wyyId;

    private String companyId;

    private String status;

    private String createTime;

    private String createUid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWyyId() {
        return wyyId;
    }

    public void setWyyId(String wyyId) {
        this.wyyId = wyyId == null ? null : wyyId.trim();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId == null ? null : companyId.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }

    public String getCreateUid() {
        return createUid;
    }

    public void setCreateUid(String createUid) {
        this.createUid = createUid == null ? null : createUid.trim();
    }
}