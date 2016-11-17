package cmcc.mobile.admin.entity;
/**
 * FlowDiagram 流程图
 * 
 * @author hanyf
 *
 */
public class FlowLine {

	private String name;
	private String id;
	private String condition;
	private String from;
	private Integer from_type; 
	private String to;
	private Integer to_type; 
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
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Integer getFrom_type() {
		return from_type;
	}
	public void setFrom_type(Integer from_type) {
		this.from_type = from_type;
	}
	public Integer getTo_type() {
		return to_type;
	}
	public void setTo_type(Integer to_type) {
		this.to_type = to_type;
	}
}
