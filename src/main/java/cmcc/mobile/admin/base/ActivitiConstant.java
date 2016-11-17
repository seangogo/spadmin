package cmcc.mobile.admin.base;

/**
 *
 * @author renlinggao
 * @Date 2016年7月26日
 */
public class ActivitiConstant {
	public static final String FORM_ID = "__formId"; // 流程表单的id
	public static final String FORM_RELATION = "__formRelation"; // 表单是否继承上一个表单
	public static final String ALL_FORM_DETAIL = "__allForms"; //所有表单
	public static final String TASKS_CONFIG = "__tasksConfig";//所有节点的配置
	public static final String NEXT_NODE = "__nextTaskName";//下一个节点
	public static final String NEXT_BUTTON = "__button";//节点的按钮组
	
	public static final String CONFIG_SUCCESS="__success";//是否继承上一个表单

	public static final String TASK_DELETE_REASON_COMPLETED = "200";
	public static final String TASK_DELETE_REASON_CANCELED = "300";
	public static final String TASK_DELETE_REASON_REASSIGNED = "400";
	public static final String TASK_DELETE_REASON_REJECTED = "500";
	public static final String TASK_DELETE_REASON_RECOVERED = "600";
	
	public static final String PREVIOUS_TASK = "__previousTask";
	
	public static final String PROCESS_STATUS = "__process_status";//流程状态
	
	public static final int PROCESS_STATUS_DRAFT = 0;//起草状态
	public static final int PROCESS_STATUS_CIRCULATION=1;//流转状态
	public static final int PROCESS_STATUS_COMPLETE=2;//完成状态
	public static final int PROCESS_STATUS_REFUSE=3;//拒绝状态
	public static final int PROCESS_STATUS_REVOKE = 9;//起草人撤销
}
