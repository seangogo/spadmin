package cmcc.mobile.admin.util;

import java.util.ArrayList;
import java.util.List;

/**
 * approval_data json解析数据
 * 
 * @author zhangxs
 *
 */
public class DBJSONObject {
	private String data;
	private String controlName; // 控件类型
	private String id; // 控件id
	private String value; // 值
	private List<String> list = new ArrayList<String>();

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getControlName() {
		return controlName;
	}

	public void setControlName(String controlName) {
		this.controlName = controlName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
