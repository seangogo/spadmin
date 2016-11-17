package cmcc.mobile.admin.entity;
import java.util.List;
/**
 * FlowUser 流程用户
 * 
 * @author hanyf
 *
 */
public class FlowUser {
	private List<String> users;
	private List<String> groups;
	private int groupsType;	//0:按组分割；1：不按组分割
	public List<String> getUsers() {
		return users;
	}
	public void setUsers(List<String> users) {
		this.users = users;
	}
	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	public int getGroupsType() {
		return groupsType;
	}
	public void setGroupsType(int groupsType) {
		this.groupsType = groupsType;
	}
	
}
