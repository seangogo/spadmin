package cmcc.mobile.admin.entity;

import java.sql.Date;

public class VwtCorpHis {
	
	private String corpid;
	private String corpname;
	private String corppersonname;
	private String corpmobilephone;
	private String customerid;
	private Date   deleteTime;
	
	public String getCorpid() {
		return corpid;
	}
	public void setCorpid(String corpid) {
		this.corpid = corpid;
	}
	public String getCorpname() {
		return corpname;
	}
	public void setCorpname(String corpname) {
		this.corpname = corpname;
	}
	public String getCorppersonname() {
		return corppersonname;
	}
	public void setCorppersonname(String corppersonname) {
		this.corppersonname = corppersonname;
	}
	public String getCorpmobilephone() {
		return corpmobilephone;
	}
	public void setCorpmobilephone(String corpmobilephone) {
		this.corpmobilephone = corpmobilephone;
	}
	public String getCustomerid() {
		return customerid;
	}
	public void setCustomerid(String customerid) {
		this.customerid = customerid;
	}
	public Date getDeleteTime() {
		return deleteTime;
	}
	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}
	
	
	
}
