package cmcc.mobile.admin.entity;

import java.sql.Date;

public class VwtMemberInfoHis {
	
	private String corpid;
	private String memid;
	private String telnum;
	private String membername;
	private String spell;
	private String deptid;
	private int sort;
	private String jobnum;
	private char memstatus;
	private Date creattime;
	private int roleauth;
	public String getCorpid() {
		return corpid;
	}
	public void setCorpid(String corpid) {
		this.corpid = corpid;
	}
	public String getMemid() {
		return memid;
	}
	public void setMemid(String memid) {
		this.memid = memid;
	}
	public String getTelnum() {
		return telnum;
	}
	public void setTelnum(String telnum) {
		this.telnum = telnum;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public String getSpell() {
		return spell;
	}
	public void setSpell(String spell) {
		this.spell = spell;
	}
	public String getDeptid() {
		return deptid;
	}
	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public String getJobnum() {
		return jobnum;
	}
	public void setJobnum(String jobnum) {
		this.jobnum = jobnum;
	}
	public char getMemstatus() {
		return memstatus;
	}
	public void setMemstatus(char memstatus) {
		this.memstatus = memstatus;
	}
	public Date getCreattime() {
		return creattime;
	}
	public void setCreattime(Date creattime) {
		this.creattime = creattime;
	}
	public int getRoleauth() {
		return roleauth;
	}
	public void setRoleauth(int roleauth) {
		this.roleauth = roleauth;
	}
	public int getVisitauth() {
		return visitauth;
	}
	public void setVisitauth(int visitauth) {
		this.visitauth = visitauth;
	}
	public Date getDeleteTime() {
		return deleteTime;
	}
	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}
	private int visitauth;
	private Date deleteTime;
	
}
