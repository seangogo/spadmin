package cmcc.mobile.admin.entity;

import java.util.List;
import java.util.Map;

/**
 * FlowForm form定义
 * 
 * @author hanyf
 *
 */
public class FlowForms {
	private String flowName;
	Map<String,FlowForm> forms;
	public String getFlowName() {
		return flowName;
	}
	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}
	public Map<String, FlowForm> getForms() {
		return forms;
	}
	public void setForms(Map<String, FlowForm> forms) {
		this.forms = forms;
	}



}