package cmcc.mobile.admin.entity;

import java.util.List;
import java.util.Map;

/**
 * FlowDiagram 流程图
 * 
 * @author hanyf
 *
 */
public class FlowDiagram {
	String flowName;
	Map<String,FlowAction> tasks;
	Map<String,FlowLine> lines;
	Map<String,FlowXOR> xors;
	Map<String,FlowMultiUserAction> multiTasks;
	Map<String,FlowTimeLimitedAction>timeLimitedTasks;
	
	
	public Map<String, FlowTimeLimitedAction> getTimeLimitedTasks() {
		return timeLimitedTasks;
	}
	public void setTimeLimitedTasks(Map<String, FlowTimeLimitedAction> timeLimitedTasks) {
		this.timeLimitedTasks = timeLimitedTasks;
	}
	public Map<String, FlowMultiUserAction> getMultiTasks() {
		return multiTasks;
	}
	public void setMultiTasks(Map<String, FlowMultiUserAction> multiTasks) {
		this.multiTasks = multiTasks;
	}
	public Map<String, FlowXOR> getXors() {
		return xors;
	}
	public void setXors(Map<String, FlowXOR> xors) {
		this.xors = xors;
	}
	public String getFlowName() {
		return flowName;
	}
	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}
	public Map<String, FlowAction> getTasks() {
		return tasks;
	}
	public void setTasks(Map<String, FlowAction> tasks) {
		this.tasks = tasks;
	}
	public Map<String, FlowLine> getLines() {
		return lines;
	}
	public void setLines(Map<String, FlowLine> lines) {
		this.lines = lines;
	}
}
