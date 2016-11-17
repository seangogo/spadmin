package cmcc.mobile.admin.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.RepositoryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.activiti.util.ActivitiXMLUtil;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.base.VirtualActorConstant;
import cmcc.mobile.admin.dao.ActivitiTableConfigMapper;
import cmcc.mobile.admin.dao.AdminRoleMapper;
import cmcc.mobile.admin.dao.ApprovalAuthorityMapper;
import cmcc.mobile.admin.dao.ApprovalMostTypeMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigDetailsMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.CompanySenceAuthorityMapper;
import cmcc.mobile.admin.dao.UserApprovalTypeMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.ActivitiTableConfig;
import cmcc.mobile.admin.entity.ApprovalAuthority;
import cmcc.mobile.admin.entity.ApprovalMostType;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.CompanySenceAuthority;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.service.CustomFormService;
import cmcc.mobile.admin.util.DateUtil;
import cmcc.mobile.admin.util.PropertiesUtil;
import cmcc.mobile.admin.vo.ActivitiTypeVo;
import cmcc.mobile.admin.vo.ApprovalTableConfigVo;
import cmcc.mobile.admin.vo.ApprovalTypeVo;

/**
 *
 * @author renlinggao
 * @Date 2016年5月5日
 */
@Service("customFormService")
public class CustomFormServiceImpl implements CustomFormService {

	@Autowired
	ApprovalMostTypeMapper approvalMostTypeMapper;

	@Autowired
	ApprovalTableConfigMapper approvalTableConfigMapper;

	@Autowired
	ApprovalTypeMapper approvalTypeMapper;

	@Autowired
	ApprovalTableConfigDetailsMapper approvalTableConfigDetailsMapper;

	@Autowired
	UserMapper userMapper;
	@Autowired
	AdminRoleMapper roleMapper;
	@Autowired
	UserApprovalTypeMapper userApprovalType;

	@Autowired
	private CompanySenceAuthorityMapper companySenceAuthorityMapper;

	@Autowired
	ActivitiTableConfigMapper activitiTableConfigMapper;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ApprovalAuthorityMapper authorityMapper;

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public List<ApprovalType> getAllWorkFlows(String companyId) {
		ApprovalType params = new ApprovalType();
		params.setCompanyId(companyId);
		return approvalTypeMapper.getAllWorkFlow(params);
	}

	@Override
	public boolean addCustomForm(String companyId, Integer scene, String mostTypeKey, String name, String icon,
			String des, List<ApprovalTableConfigDetails> control, String userId) {
		String py = UUID.randomUUID().toString();// CustomFormUtil.convertLower(name);
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(py);
		if (approvalType != null)
			return false;

		// 插入流程表
		String atcId = UUID.randomUUID().toString();
		ApprovalTableConfig atc = new ApprovalTableConfig();
		atc.setId(atcId);
		atc.setApprovalTypeId(py);
		atc.setStatus("0");
		atc.setUserId("1");
		atc.setDate(DateUtil.getDateStr(new Date()));
		approvalTableConfigMapper.insert(atc);
		// 插入流程类型表
		ApprovalType at = new ApprovalType();
		at.setDes(des);
		at.setIcon(icon);
		at.setName(name);
		at.setId(py);
		at.setStatus("1");
		at.setApprovalMostTypeId(mostTypeKey);
		at.setApprovalTableConfigId(atc.getId() + "");
		at.setThirdApprovalStartLink(PropertiesUtil.getAppByKey("TYPE_LINK") + py);
		at.setScene(scene);
		at.setCompanyId(companyId);
		at.setIsDefault("1");
		at.setCreateUserId(userId);
		approvalTypeMapper.insert(at);

		ApprovalAuthority record = new ApprovalAuthority();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		record.setId(py);
		record.setCreateTime(sdf.format(new Date()));
		record.setCompanyId(companyId);
		ApprovalMostType type = approvalMostTypeMapper.selectByPrimaryKey(mostTypeKey);
		record.setWyyId(type.getWyyId());
		authorityMapper.insertSelective(record);

		String mingxifId = null;
		// 插入表单数据
		if (control != null) {
			for (ApprovalTableConfigDetails atcd : control) {
				String atcdId = UUID.randomUUID().toString();

				if ("TableField".equals(atcd.getControlId())) {
					mingxifId = atcdId;
				}

				if (new Integer(atcd.getSequence()) % 100 > 0) {
					atcd.setPreviousId(mingxifId);
				}
				atcd.setId(atcdId);
				atcd.setApprovalTableConfigId(atc.getId() + "");
				approvalTableConfigDetailsMapper.insert(atcd);
			}
		}

		return true;
	}

	@Override
	public boolean editCustomForm(String mostTypeKey, String id, String name, String icon, String des,
			List<ApprovalTableConfigDetails> control) {
		ApprovalType at = approvalTypeMapper.selectByPrimaryKey(id);

		if (at != null) {
			// 获取原来的流程配置
			ApprovalTableConfig atc = approvalTableConfigMapper.selectByPrimaryKey(at.getApprovalTableConfigId());

			atc.setStatus("2"); // 设置原来的状态为删除
			approvalTableConfigMapper.updateByPrimaryKeySelective(atc);

			atc.setDate(DateUtil.getDateStr(new Date()));
			String newConfigId = UUID.randomUUID().toString();
			atc.setId(newConfigId);
			atc.setStatus("0");// 设置原来的状态为启用
			// 复制原来的配置插入数据库
			approvalTableConfigMapper.insert(atc);

			String mingxifId = null;
			// 插入所有的表单控件到控件表
			for (ApprovalTableConfigDetails atcd : control) {
				String atcdId = UUID.randomUUID().toString();
				if ("TableField".equals(atcd.getControlId())) {
					mingxifId = atcdId;
				}
				if (new Integer(atcd.getSequence()) % 100 > 0) {
					atcd.setPreviousId(mingxifId);
				}

				atcd.setId(atcdId);
				atcd.setApprovalTableConfigId(newConfigId);
				approvalTableConfigDetailsMapper.insert(atcd);
			}

			at.setApprovalTableConfigId(newConfigId); // 设置新表单配置关联
			at.setName(name);
			at.setDes(des);
			at.setIcon(icon);
			at.setApprovalMostTypeId(mostTypeKey);
			approvalTypeMapper.updateByPrimaryKeySelective(at);
			return true;
		}
		return false;
	}

	@Override
	public List<ApprovalType> findApprovelByMostType(String companyId, String id, String wyyId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", id);
		params.put("companyId", companyId);
		params.put("wyyId", wyyId);
		List<ApprovalType> result = approvalTypeMapper.findByMostTypeId(params);
		return result;
	}

	@Override
	public List<ApprovalMostType> findMostType(String wyyId) {
		return approvalMostTypeMapper.getAll(wyyId);
	}

	@Override
	public ApprovalTypeVo getApprovalInfoById(String id) {
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(id);
		List<ApprovalTableConfigDetails> control = approvalTableConfigDetailsMapper
				.getApprovalInfoById1(approvalType.getApprovalTableConfigId());
		ApprovalTypeVo result = new ApprovalTypeVo(approvalType);
		result.setControl(control);
		return result;
	}

	@Override
	public ApprovalTableConfigVo getDefApprovalUsers(String id) {
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(id);
		ApprovalTableConfig approvalTableConfig = approvalTableConfigMapper
				.selectByPrimaryKey(approvalType.getApprovalTableConfigId());
		List<User> defUserList = getDefUser(approvalTableConfig.getDefaultApprovalUserIds());
		ApprovalTableConfigVo approvalTableConfigVo = new ApprovalTableConfigVo(approvalTableConfig);
		approvalTableConfigVo.setDefUserList(defUserList);
		if (approvalTableConfig.getLastUserId() != null)
			approvalTableConfigVo.setLastUser(userMapper.selectByPrimaryKey(approvalTableConfig.getLastUserId()));
		return approvalTableConfigVo;
	}

	private List<User> getDefUser(String str) {
		List<User> users = new ArrayList<User>();

		if (StringUtils.isNotEmpty(str)) {// 解析字符串加入user集合
			String userIds[] = str.split(",");
			for (String id : userIds) {
				User user = new User();
				if ((VirtualActorConstant.ASSUMED_ROLE.equals(id))) {// 当时站位时
																		// 设置用户名
					user.setUserName(VirtualActorConstant.ASSUMED_ROLE);
					user.setId(VirtualActorConstant.ASSUMED_ROLE);
				} else if (VirtualActorConstant.PROMOTER_ROLE.endsWith(id)) {
					user.setUserName(VirtualActorConstant.PROMOTER_ROLE);
					user.setId(VirtualActorConstant.PROMOTER_ROLE);
				} else if (VirtualActorConstant.DEPT_LEADER_ROLE.endsWith(id)) {
					user.setUserName(VirtualActorConstant.DEPT_LEADER_ROLE);
					user.setId(VirtualActorConstant.DEPT_LEADER_ROLE);
				} else if (VirtualActorConstant.SUPERIOR_DEPT_LEADER_ROLE.endsWith(id)) {
					user.setUserName(VirtualActorConstant.SUPERIOR_DEPT_LEADER_ROLE);
					user.setId(VirtualActorConstant.SUPERIOR_DEPT_LEADER_ROLE);
				} else {
					user = userMapper.selectByPrimaryKey(id);
				}
				users.add(user);
			}
		}
		return users;
	}

	@Override
	public boolean setDefApprovalUsers(ApprovalTableConfig approvalTableConfig) {
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(approvalTableConfig.getId());
		if (approvalType == null)
			return false;
		ApprovalTableConfig a = approvalTableConfigMapper.selectByPrimaryKey(approvalType.getApprovalTableConfigId());
		approvalTableConfig.setId(a.getId());
		if (a != null) {
			approvalTableConfigMapper.updateByPrimaryKeySelective(approvalTableConfig);
			return true;
		}
		return false;
	}

	@Override
	public boolean stopWorkFlow(String id) {
		ApprovalType at = approvalTypeMapper.selectByPrimaryKey(id);
		if (at != null) {
			if ("1".equals(at.getStatus())) {
				int x = userApprovalType.deleteByUserIdAndTypeId(id);
				at.setStatus("2");
			} else {
				at.setStatus("1");
			}
			approvalTypeMapper.updateByPrimaryKeySelective(at);
			return true;
		}
		return false;
	}

	@Override
	public List<ApprovalType> getAllWorkFlow(String companyId) {
		ApprovalType type = new ApprovalType();
		type.setCompanyId(companyId);
		return approvalTypeMapper.getAllWorkFlow(type);
	}

	@Override
	public HSSFWorkbook getApprovalExcel(Map<String, String> resultMap, String typeId, String mobile,
			String companyId) {
		ApprovalType type = approvalTypeMapper.selectByPrimaryKey(typeId);
		resultMap.put("name", type.getName() + "数据导入模板");
		String configId = type.getApprovalTableConfigId();
		List<ApprovalTableConfigDetails> controls = approvalTableConfigDetailsMapper.getApprovalInfoById(configId);

		// 产生工作簿对象
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色
		style.setBorderLeft((short) 1); // 边框的大小
		style.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色
		style.setBorderRight((short) 1);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		style.setBorderBottom((short) 1);
		style.setWrapText(true);// 自动换行

		HSSFSheet sheet = workbook.createSheet("sheet");
		sheet.setDefaultColumnWidth(20);

		// 设置表头
		HSSFRow titleRow = sheet.createRow(0);
		titleRow.setHeight((short) 300);

		HSSFCell cell0 = titleRow.createCell(0);
		cell0.setCellValue("姓名");
		HSSFCell cell1 = titleRow.createCell(1);
		cell1.setCellValue("手机号");

		String[] unExportController = new String[] { "DDPhotoField", "DDAttachment", "TableField", "DDDateField",
				"DDDateRangeField", "ValidField", "PictureNote" };
		List<String> tempList = Arrays.asList(unExportController);
		int column = 2;
		for (ApprovalTableConfigDetails details : controls) {
			if (tempList.contains(details.getControlId()) || StringUtils.isNotEmpty(details.getPreviousId())) {
				continue;
			}
			HSSFCell cell = titleRow.createCell(column);

			String value = details.getDescribeName();
			if (details.getControlId().equals("TextNote")) {
				value = (details.getExp() != null ? details.getExp() : "说明控件");
				value = value.replace("<br>", "\r\n");
				sheet.setColumnWidth(column, (short) "说明控件    ".getBytes().length * 256);
			}
			cell.setCellValue(value);
			column++;
		}

		int column1 = 2;
		HSSFRow row = sheet.createRow(1);
		HSSFCell rcell0 = titleRow.createCell(0);
		rcell0.setCellValue("姓名");
		HSSFCell rcell1 = titleRow.createCell(1);
		rcell1.setCellValue("手机号");

		for (ApprovalTableConfigDetails details : controls) {
			if (tempList.contains(details.getControlId()) || StringUtils.isNotEmpty(details.getPreviousId())) {
				continue;
			}
			HSSFCell cell = row.createCell(column1);
			String value = "";
			if ("DDMultiSelectField".equals(details.getControlId()) || "DDSelectField".equals(details.getControlId())) {
				value = "选项1\\选项2\\选项3";
			} else if ("MoneyField".equals(details.getControlId()) || "NumberField".equals(details.getControlId())) {
				value = "数字";
			} else if ("TextNote".equals(details.getControlId())) {
				value = "说明文字";
			} else if ("TextField".equals(details.getControlId()) || "TextareaField".equals(details.getControlId())) {
				value = "文字";
			} else if ("LinkageSelectField".equals(details.getControlId())) {
				value = "选项1(子项1,子项2,子项3)\\选项2(子项1,子项2,子项3)\\选项3(子项1,子项2,子项3)";
			}
			cell.setCellValue(value);
			if (titleRow.getCell(column1).getStringCellValue().length() < value.length()) {
				sheet.setColumnWidth(column1, (short) value.getBytes().length * 256);
			}
			column1++;
		}

		return workbook;
	}

	@Override
	public boolean editCustomActiviti(String companyId, String id, String mostTypeKey, String name, String icon,
			String des, String forms, String flow) {
		ApprovalType at = approvalTypeMapper.selectByPrimaryKey(id);

		if (at != null) {
			// 获取原来的流程配置
			ActivitiTableConfig atc = activitiTableConfigMapper.selectByPrimaryKey(at.getApprovalTableConfigId());

			atc.setStatus("2"); // 设置原来的状态为删除
			activitiTableConfigMapper.updateByPrimaryKeySelective(atc);

			atc.setDate(DateUtil.getDateStr(new Date()));
			String newConfigId = UUID.randomUUID().toString();
			atc.setId(newConfigId);
			atc.setStatus("0");// 设置原来的状态为启用
			// 复制原来的配置插入数据库
			atc.setUserId("1");
			atc.setForms(forms);
			atc.setFlow(flow);
			activitiTableConfigMapper.insert(atc);

			at.setApprovalTableConfigId(newConfigId); // 设置新表单配置关联
			at.setName(name);
			at.setDes(des);
			at.setIcon(icon);
			at.setApprovalMostTypeId(mostTypeKey);
			approvalTypeMapper.updateByPrimaryKeySelective(at);
			return true;
		}
		return false;

	}

	@Override
	public boolean addCustomActiviti(String companyId, String mostTypeKey, String name, String icon, String des,
			String forms, String flow, Integer sence) {
		String py = "ACT_" + UUID.randomUUID().toString();// CustomFormUtil.convertLower(name);
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(py);
		if (approvalType != null)
			return false;

		// 插入流程表
		String atcId = UUID.randomUUID().toString();
		ActivitiTableConfig atc = new ActivitiTableConfig();
		atc.setId(atcId);
		atc.setApprovalTypeId(py);
		atc.setStatus("0");
		atc.setUserId("1");
		atc.setDate(DateUtil.getDateStr(new Date()));
		atc.setForms(forms);
		atc.setFlow(flow);
		activitiTableConfigMapper.insert(atc);
		// 插入流程类型表
		ApprovalType at = new ApprovalType();
		at.setDes(des);
		at.setIcon(icon);
		at.setName(name);
		at.setId(py);
		at.setStatus("1");
		at.setApprovalMostTypeId(mostTypeKey);
		at.setApprovalTableConfigId(atc.getId() + "");
		// at.setThirdApprovalStartLink(PropertiesUtil.getAppByKey("TYPE_LINK")
		// + py);
		at.setScene(sence);
		at.setCompanyId(companyId);
		approvalTypeMapper.insert(at);
		return true;

	}

	@Override
	public ActivitiTypeVo getActivitiById(String id) {
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(id);
		ActivitiTableConfig atc = activitiTableConfigMapper.selectByPrimaryKey(approvalType.getApprovalTableConfigId());
		ActivitiTypeVo result = new ActivitiTypeVo(approvalType);
		result.setFlowConfigID(atc.getId());
		result.setFlow(atc.getFlow());
		result.setForms(atc.getForms());
		return result;
	}

	@Override
	public boolean deployActivitiById(String id, String companyId) {
		CompanySenceAuthority param = new CompanySenceAuthority();
		param.setCompanyId(companyId);
		// param = companySenceAuthorityMapper.findByCompanyId(param);
		// if(param == null ||
		// !Arrays.asList(param.getSence().split(",")).contains("3")){
		// throw new RuntimeException("没有权限部署流程");
		// }

		ActivitiTypeVo av = getActivitiById(id);
		ActivitiXMLUtil aXMLUtil = new ActivitiXMLUtil();
		aXMLUtil.init(av.getId(), av.getName(), av.getFlow(), av.getForms());
		String xml = aXMLUtil.createXML();
		return deploy(xml, id);
	}

	/**
	 * 字符串部署方式
	 * 
	 * @param str
	 * @param id
	 * @return
	 */
	private boolean deploy(String str, String id) {
		try {
			repositoryService.createDeployment().name(id).addString(id + ".bpmn20.xml", str).deploy();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	@Override
	public JsonResult findWyy(Map<String, Object> map) {
		List<ApprovalMostType> list = approvalMostTypeMapper.selectByWyy(map);
		return new JsonResult(true, "", list);
	}

}