package cmcc.mobile.admin.util;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.*;
import org.dom4j.io.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cmcc.mobile.admin.entity.ActivitiTableConfig;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.FlowAction;
import cmcc.mobile.admin.entity.FlowActionForm;
import cmcc.mobile.admin.entity.FlowDiagram;
import cmcc.mobile.admin.entity.FlowForm;
import cmcc.mobile.admin.entity.FlowForms;
import cmcc.mobile.admin.entity.FlowLine;
import cmcc.mobile.admin.entity.FlowMultiUserAction;
import cmcc.mobile.admin.entity.FlowTimeLimitedAction;
import cmcc.mobile.admin.entity.FlowUser;
import cmcc.mobile.admin.entity.FlowXOR;
import cmcc.mobile.admin.vo.ActivitiTypeVo;

/**
 *
 * @author hanyf
 * @Date 2016年7月25日 activiti与xml生成类
 */
public class ActivitiXMLUtil {
	private final Log logger = LogFactory.getLog(getClass());

	// List<Object> firstta
	private ActivitiTypeVo activitiTypeVo;
	private FlowForms flowforms;
	private FlowDiagram flowdiagram;
	private List<String> beginActions;
	private List<String> endActions;
	private Integer flowIndex;
	private Map<String, Map<String, Object>> __tasksConfig;;

	public boolean init(ActivitiTypeVo av) {
		activitiTypeVo = av;
		flowforms = JSON.parseObject(av.getForms(), FlowForms.class);
		flowdiagram = JSON.parseObject(av.getFlow(), FlowDiagram.class);
		// String txt = JSON.toJSONString(flowforms);
		// String txt2 = JSON.toJSONString(flowdiagram);
		// System.out.println(txt);
		// System.err.println(txt2);
		beginActions = new ArrayList<String>();
		endActions = new ArrayList<String>();
		flowIndex = 1;
		__tasksConfig = new HashMap<String, Map<String, Object>>();
		return true;
	}

	private void findBE() {
		for (FlowLine fl : flowdiagram.getLines().values()) {
			String from = fl.getFrom();
			String to = fl.getTo();
			switch (fl.getFrom_type()) {
			case 1:
				FlowAction fa = flowdiagram.getTasks().get(from);
				if (fa != null)
					fa.setHaveNextAction(true);
				else {
					FlowMultiUserAction fmua = flowdiagram.getMultiTasks().get(from);
					if (fmua != null)
						fmua.setHaveNextAction(true);
					else
						logger.debug("有未找到的开始节点，节点编号" + from);
				}
				break;
			}
			switch (fl.getTo_type()) {
			case 1:
				FlowAction fa = flowdiagram.getTasks().get(to);
				if (fa != null)
					fa.setHavePreAcion(true);
				else {
					FlowMultiUserAction fmua = flowdiagram.getMultiTasks().get(to);
					if (fmua != null)
						fmua.setHavePreAcion(true);
					else
						logger.debug("有未找到的结束节点，节点编号" + to);
				}
				break;
			}

		}
		beginActions.clear();
		endActions.clear();
		for (FlowAction fa : flowdiagram.getTasks().values()) {
			if (!fa.isHavePreAcion())
				beginActions.add(fa.getId());
			if (!fa.isHaveNextAction())
				endActions.add(fa.getId());
		}
		if (flowdiagram.getMultiTasks() != null) {
			for (FlowMultiUserAction fmua : flowdiagram.getMultiTasks().values()) {
				if (!fmua.isHavePreAcion())
					beginActions.add(fmua.getId());
				if (!fmua.isHaveNextAction())
					endActions.add(fmua.getId());
			}
		}

	}

	private Element createBeginEvent(Element root) {
		Element startEvent = root.addElement("startEvent");
		startEvent.addAttribute("id", "startevent1");
		startEvent.addAttribute("name", "Start");
		Element extensionElements = startEvent.addElement("extensionElements");
		Element formProperty = extensionElements.addElement("formProperty", "activiti");
		formProperty.addAttribute("id", "__allForms");
		formProperty.addAttribute("name", "全部流程表单变量");
		formProperty.addAttribute("type", "string");
		// formProperty.addAttribute("default",
		// "<![CDATA["+activitiTypeVo.getForms()+"]]>");
		formProperty.addAttribute("default", activitiTypeVo.getForms());

		if (beginActions.size() == 1) {
			String fa = beginActions.get(0);
			Element sequenceFlow = root.addElement("sequenceFlow");
			sequenceFlow.addAttribute("id", "flow" + flowIndex++);
			sequenceFlow.addAttribute("sourceRef", "startevent1");
			sequenceFlow.addAttribute("targetRef", fa);
		} else {

			Element exclusiveGateway = root.addElement("exclusiveGateway");
			exclusiveGateway.addAttribute("id", "exclusivegateway1");
			exclusiveGateway.addAttribute("name", "Start Exclusive Gateway");

			Element sequenceFlow = root.addElement("sequenceFlow");
			sequenceFlow.addAttribute("id", "flow" + flowIndex++);
			sequenceFlow.addAttribute("sourceRef", "startevent1");
			sequenceFlow.addAttribute("targetRef", "exclusivegateway1");

			for (String fa : beginActions) {
				sequenceFlow = root.addElement("sequenceFlow");
				sequenceFlow.addAttribute("id", "flow" + flowIndex++);
				sequenceFlow.addAttribute("sourceRef", "exclusivegateway1");
				sequenceFlow.addAttribute("targetRef", fa);
				Element conditionExpression = sequenceFlow.addElement("conditionExpression");
				conditionExpression.addAttribute("xsi:type", "tFormalExpression");
				conditionExpression.addCDATA("${__nextTaskName==\"" + fa + "\"}");
			}
		}
		return extensionElements;
	}

	private void createEndEvent(Element root) {
		root.addElement("endEvent").addAttribute("id", "endevent1").addAttribute("name", "End");
		for (String fa : endActions) {
			root.addElement("sequenceFlow").addAttribute("id", "flow" + flowIndex++).addAttribute("sourceRef", fa)
					.addAttribute("targetRef", "endevent1");
		}
	}

	private void clear() {
		beginActions.clear();
		endActions.clear();
		__tasksConfig.clear();
		flowIndex = 1;
	}

	private void createTimeLimitedTasks(Element root) {
		for (FlowTimeLimitedAction ftla : flowdiagram.getTimeLimitedTasks().values()) {
			Element subProcess = root.addElement("subProcess").addAttribute("id", "subFlowID_" + ftla.getId())
					.addAttribute("name", "Sub Process" + ftla.getId());

		}

	}

	public String createXML() {
		flowIndex = 1;
		String strXML = null;
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");

		Element definitions = document.addElement("definitions", "http://www.omg.org/spec/BPMN/20100524/MODEL");
		definitions.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		definitions.addAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		definitions.addAttribute("xmlns:activiti", "http://activiti.org/bpmn");
		definitions.addAttribute("xmlns:bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
		definitions.addAttribute("xmlns:omgdc", "http://www.omg.org/spec/DD/20100524/DC");
		definitions.addAttribute("xmlns:omgdi", "http://www.omg.org/spec/DD/20100524/DI");
		definitions.addAttribute("typeLanguage", "http://www.w3.org/2001/XMLSchema");
		definitions.addAttribute("expressionLanguage", "http://www.w3.org/1999/XPath");
		definitions.addAttribute("targetNamespace", "http://www.jsydydsp.me/activiti/" + activitiTypeVo.getName());

		Element process = definitions.addElement("process");
		process.addAttribute("id", activitiTypeVo.getId());
		process.addAttribute("name", activitiTypeVo.getName());
		process.addAttribute("isExecutable", "true");
		findBE();
		Element extensionElements = createBeginEvent(process);

		for (FlowAction fa : flowdiagram.getTasks().values()) {
			Element userTask = process.addElement("userTask").addAttribute("id", fa.getId()).addAttribute("name",
					fa.getName());
			FlowUser user = fa.getUser();
			Map<String, Object> para = new HashMap<String, Object>();
			if (!user.getUsers().isEmpty()) {
				para.put("candidateUsers",
						"[" + org.apache.commons.lang.StringUtils.join(user.getUsers().toArray(), ",") + "]");
			}
			if (!user.getGroups().isEmpty()) {
				para.put("candidateGroups",
						"[" + org.apache.commons.lang.StringUtils.join(user.getGroups().toArray(), ",") + "]");
			}
			FlowActionForm forminfo = fa.getForm();
			para.put("__formId", forminfo.getFormID());
			para.put("__success", forminfo.isSuccess());
			para.put("__writable", forminfo.isWritable());
			para.put("__groupsType", user.getGroupsType());
			para.put("__button", fa.getButtons());
			__tasksConfig.put(fa.getId(), para);

		}
		if (flowdiagram.getMultiTasks() != null) {
			for (FlowMultiUserAction fmua : flowdiagram.getMultiTasks().values()) {
				Element userTask = process.addElement("userTask").addAttribute("id", fmua.getId())
						.addAttribute("name", fmua.getName()).addAttribute("async", "true");
				userTask.addElement("multiInstanceLoopCharacteristics").addAttribute("isSequential",
						fmua.getSequential());
				FlowUser user = fmua.getUser();
				Map<String, Object> para = new HashMap<String, Object>();
				if (!user.getUsers().isEmpty()) {
					para.put("candidateUsers",
							org.apache.commons.lang.StringUtils.join(user.getUsers().toArray(), ","));
				}
				if (!user.getGroups().isEmpty()) {
					para.put("candidateGroups",
							org.apache.commons.lang.StringUtils.join(user.getGroups().toArray(), ","));
				}
				FlowActionForm forminfo = fmua.getForm();
				para.put("__formId", forminfo.getFormID());
				para.put("__success", forminfo.isSuccess());
				para.put("__writable", forminfo.isWritable());
				para.put("__groupsType", user.getGroupsType());
				para.put("__button", fmua.getButtons());
				__tasksConfig.put(fmua.getId(), para);

			}
		}
		for (FlowLine fl : flowdiagram.getLines().values()) {
			Element sequenceFlow = process.addElement("sequenceFlow").addAttribute("id", fl.getId())
					.addAttribute("sourceRef", fl.getFrom()).addAttribute("targetRef", fl.getTo());
			if (!fl.getCondition().isEmpty()) {
				Element conditionExpression = sequenceFlow.addElement("conditionExpression");
				conditionExpression.addAttribute("xsi:type", "tFormalExpression");
				conditionExpression.addCDATA(fl.getCondition());
			}
		}

		if (flowdiagram.getXors() != null) {
			for (FlowXOR fXor : flowdiagram.getXors().values()) {
				Element exclusiveGateway = process.addElement("exclusiveGateway").addAttribute("id", fXor.getId())
						.addAttribute("name", fXor.getName());
				if (!fXor.getDefaultLine().isEmpty()) {
					exclusiveGateway.addAttribute("default", fXor.getDefaultLine());
				}
			}
		}
		createEndEvent(process);

		Element formProperty = extensionElements.addElement("formProperty", "activiti");
		formProperty.addAttribute("id", "__tasksConfig");
		formProperty.addAttribute("name", "全部流程节点属性数据");
		formProperty.addAttribute("type", "string");
		// formProperty.addAttribute("default",
		// "<![CDATA["+JSONObject.toJSONString(__tasksConfig)+"]]>");
		formProperty.addAttribute("default", JSONObject.toJSONString(__tasksConfig));

		// --------
		StringWriter strWtr = new StringWriter();
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		XMLWriter xmlWriter = new XMLWriter(strWtr, format);
		try {
			xmlWriter.write(document);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		strXML = strWtr.toString();
		// --------

		// -------
		// strXML=document.asXML();
		// ------

		// -------------
//		File file = new File("C:\\Users\\Administrator\\Desktop\\test\\testflow.bpmn20.xml");
//		if (file.exists()) {
//			file.delete();
//		}
//		try {
//			file.createNewFile();
//			XMLWriter out = new XMLWriter(new FileWriter(file));
//			out.write(document);
//			out.flush();
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// --------------
		clear();
		return strXML;
	}
}
