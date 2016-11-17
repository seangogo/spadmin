package cmcc.mobile.admin.entity;
/**
 * FlowTimeLimitedAction 活动节点
 * 
 * @author hanyf
 *
 */
public class FlowTimeLimitedAction {
	private static String extensionFromString ;
	private static Integer extensionButton;
	private static Integer reviewButton;
	private String name;
	private String id;
	private String beginLine;
	private String endLine;
	private FlowUser mainUser;
	private FlowUser assistUser;
	private boolean needExtension;
	private FlowUser extensionUser;
	private boolean needReview;
	private FlowUser reviewUser;
	private FlowActionForm reviewFrom;
	private FlowActionForm form;
	private Integer button;
	public static String getExtensionFromString() {
		return extensionFromString;
	}
	public static void setExtensionFromString(String extensionFromString) {
		FlowTimeLimitedAction.extensionFromString = extensionFromString;
	}
	public static Integer getExtensionButton() {
		return extensionButton;
	}
	public static void setExtensionButton(Integer extensionButton) {
		FlowTimeLimitedAction.extensionButton = extensionButton;
	}
	public static Integer getReviewButton() {
		return reviewButton;
	}
	public static void setReviewButton(Integer reviewButton) {
		FlowTimeLimitedAction.reviewButton = reviewButton;
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
	
	public String getBeginLine() {
		return beginLine;
	}
	public void setBeginLine(String beginLine) {
		this.beginLine = beginLine;
	}
	public String getEndLine() {
		return endLine;
	}
	public void setEndLine(String endLine) {
		this.endLine = endLine;
	}
	public FlowUser getMainUser() {
		return mainUser;
	}
	public void setMainUser(FlowUser mainUser) {
		this.mainUser = mainUser;
	}
	public FlowUser getAssistUser() {
		return assistUser;
	}
	public void setAssistUser(FlowUser assistUser) {
		this.assistUser = assistUser;
	}
	public boolean isNeedExtension() {
		return needExtension;
	}
	public void setNeedExtension(boolean needExtension) {
		this.needExtension = needExtension;
	}
	public FlowUser getExtensionUser() {
		return extensionUser;
	}
	public void setExtensionUser(FlowUser extensionUser) {
		this.extensionUser = extensionUser;
	}
	public boolean isNeedReview() {
		return needReview;
	}
	public void setNeedReview(boolean needReview) {
		this.needReview = needReview;
	}
	public FlowUser getReviewUser() {
		return reviewUser;
	}
	public void setReviewUser(FlowUser reviewUser) {
		this.reviewUser = reviewUser;
	}
	public FlowActionForm getReviewFrom() {
		return reviewFrom;
	}
	public void setReviewFrom(FlowActionForm reviewFrom) {
		this.reviewFrom = reviewFrom;
	}
	public FlowActionForm getForm() {
		return form;
	}
	public void setForm(FlowActionForm form) {
		this.form = form;
	}
	public Integer getButton() {
		return button;
	}
	public void setButton(Integer button) {
		this.button = button;
	}
	
	

}