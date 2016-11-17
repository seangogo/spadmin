package cmcc.mobile.admin.entity;

import java.util.List;
import java.util.Map;

/**
 * FlowForm form定义
 * 
 * @author hanyf
 *
 */
public class FlowForm {
	private String formID;
//	private String widgets;
	List<ApprovalTableConfigDetails> widgets;
	Map<String,FlowFormParameter>  paramters;
	public List<ApprovalTableConfigDetails> getWidgets() {
		return widgets;
	}
	public void setWidgets(List<ApprovalTableConfigDetails> widgets) {
		this.widgets = widgets;
	}
	public String getFormID() {
		return formID;
	}
	public void setFormID(String formID) {
		this.formID = formID;
	}
	public Map<String, FlowFormParameter> getParamters() {
		return paramters;
	}
	public void setParamters(Map<String, FlowFormParameter> paramters) {
		this.paramters = paramters;
	}
	
}