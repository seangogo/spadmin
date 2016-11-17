package cmcc.mobile.admin.entity;
/**
 * FlowAction 活动节点
 * 
 * @author hanyf
 *
 */
public class FlowAction {
	private String name;
	private String id;
	private boolean haveNextAction;
	private boolean havePreAcion;
	private FlowUser user;
	private FlowActionForm form;
	private String buttons;
	
	public String getButtons() {
		return buttons;
	}
	public void setButtons(String buttons) {
		this.buttons = buttons;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isHaveNextAction() {
		return haveNextAction;
	}
	public void setHaveNextAction(boolean haveNextAction) {
		this.haveNextAction = haveNextAction;
	}
	public boolean isHavePreAcion() {
		return havePreAcion;
	}
	public void setHavePreAcion(boolean havePreAcion) {
		this.havePreAcion = havePreAcion;
	}
	public FlowUser getUser() {
		return user;
	}
	public void setUser(FlowUser user) {
		this.user = user;
	}
	public FlowActionForm getForm() {
		return form;
	}
	public void setForm(FlowActionForm form) {
		this.form = form;
	}
	

}