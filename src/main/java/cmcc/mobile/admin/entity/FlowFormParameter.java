package cmcc.mobile.admin.entity;
/**
 * FlowUser 流程用户
 * 
 * @author hanyf
 *
 */
public class FlowFormParameter {
	String name;
	String type;
	String controlID;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getControlID() {
		return controlID;
	}
	public void setControlID(String controlID) {
		this.controlID = controlID;
	}
}
