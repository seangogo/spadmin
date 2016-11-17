package cmcc.mobile.admin.entity;

import java.sql.Date;

public class VwtDeptHis {
	
	private String deptid;
	private String partname;
	private int sort;
	private String parentdeptnum;
	private Date actTime;
	private String corpid;
	private Date deleteTime;
	public String getDeptid() {
		return deptid;
	}
	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}
	public String getPartname() {
		return partname;
	}
	public void setPartname(String partname) {
		this.partname = partname;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public String getParentdeptnum() {
		return parentdeptnum;
	}
	public void setParentdeptnum(String parentdeptnum) {
		this.parentdeptnum = parentdeptnum;
	}
	public Date getActTime() {
		return actTime;
	}
	public void setActTime(Date actTime) {
		this.actTime = actTime;
	}
	public String getCorpid() {
		return corpid;
	}
	public void setCorpid(String corpid) {
		this.corpid = corpid;
	}
	public Date getDeleteTime() {
		return deleteTime;
	}
	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}
	
	
}
