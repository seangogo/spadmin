package cmcc.mobile.admin.entity;

/**
 * FlowUser 流程用户
 * 
 * @author hanyf
 *
 */
public class FlowActionForm {
	private boolean success;
	private boolean writable;
	private String formID;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public boolean isWritable() {
		return writable;
	}
	public void setWritable(boolean writable) {
		this.writable = writable;
	}
	public String getFormID() {
		return formID;
	}
	public void setFormID(String formID) {
		this.formID = formID;
	}
}
