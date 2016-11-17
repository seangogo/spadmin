package cmcc.mobile.admin.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cmcc.mobile.admin.activiti.cmd.getByteArrayVariableCmd;
import cmcc.mobile.admin.base.ActivitiConstant;
import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.dao.ActivitiHistoryTaskMapper;
import cmcc.mobile.admin.entity.ActivitiHistoryTask;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.UserService;
import cmcc.mobile.admin.service.WorkFlowService;
import cmcc.mobile.admin.util.DateUtil;
import cmcc.mobile.admin.util.PropertiesUtil;
import cmcc.mobile.admin.vo.PictureVo;
import cmcc.mobile.admin.vo.UserInfoVo;

/**
 *
 * @author renlinggao
 * @Date 2016年8月12日
 */
@Controller
@RequestMapping("workflow")
public class WorkFlowController extends BaseController {
	@Autowired
	private WorkFlowService workFlowService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private FormService formService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private ManagementService managementService;
	
	@Autowired
	private ActivitiHistoryTaskMapper activitiHistoryTaskMapper;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("exportOld")
	@ResponseBody
	public void export(String typeId, String processStatus, @DateTimeFormat(pattern = "yyyy-MM-dd") Date applyStartTime,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date applyEndTime, HttpServletResponse response) {
		String currUrl = PropertiesUtil.getAppByKey("SERVER_URL") + "spadmin/file/download/";

		String dbName = getCompany().getDbname();
		// 产生工作簿对象
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色
		style.setBorderLeft((short) 1); // 边框的大小
		style.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色
		style.setBorderRight((short) 1);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		style.setBorderBottom((short) 1);
		style.setWrapText(true);// 自动换行
		List<ProcessDefinition> definition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey(typeId).list();

		for (int k = definition.size() - 1; k >= 0; k--) {
			// 合并单元格的信息
			List<Map> regionInfos = new ArrayList<Map>();
			ProcessDefinition d = definition.get(k);
			// 画标题
			Map<String, Object> forms = (Map<String, Object>) getProcessConfig(d).get("forms");

			HistoricProcessInstanceQuery processDefinitionKey = historyService.createHistoricProcessInstanceQuery()
					.processDefinitionId(d.getId());

			if (StringUtils.isNotEmpty(processStatus)) {
				processDefinitionKey.variableValueEquals(ActivitiConstant.PROCESS_STATUS, processStatus);
			}
			if (applyStartTime != null) {
				processDefinitionKey.startedAfter(applyStartTime);
			}
			if (applyEndTime != null) {
				processDefinitionKey.startedBefore(applyEndTime);
			}
			List<HistoricProcessInstance> list = processDefinitionKey.list();
			if (list == null || list.isEmpty())
				continue;
			// 新建一个excel页
			HSSFSheet createSheet = workbook.createSheet("版本" + d.getVersion());
			// 创建标题行
			HSSFRow titleRow = createSheet.createRow(0);
			// 表单信息
			List<String> formIds = new ArrayList<String>();
			List<Integer> formStartIndex = new ArrayList<Integer>();
			int lastCount = 9;
			int currRow = 1;
			for (int i = 0; i < list.size(); i++) {
				// 当前的流程
				HistoricProcessInstance hp = list.get(i);
				// 当前流程的所有的历史任务
				List<HistoricTaskInstance> hts = historyService.createHistoricTaskInstanceQuery()
						.processInstanceId(hp.getId()).orderByTaskCreateTime().asc().list();
				// 发起者的任务
				HistoricTaskInstance firstHt = hts.get(0);
				// 发起人id
				String ownerId = firstHt.getOwner();
				// 发起人用户信息
				MultipleDataSource.setDataSourceKey(dbName);
				UserInfoVo startUserInfo = workFlowService.getUserInfo(ownerId, dbName);
				// 当前流程的状态
				int ps = (int) historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId())
						.variableName(ActivitiConstant.PROCESS_STATUS).singleResult().getValue();
				String statusName = "";

				// 表单的数据
				String formDataTaskId = "";

				switch (ps) {
				case ActivitiConstant.PROCESS_STATUS_DRAFT:// 起草状态
					statusName = "草拟";
					formDataTaskId = hts.get(0).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_REFUSE:// 拒绝状态
					statusName = "拒绝";
					formDataTaskId = hts.get(hts.size() - 1).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_CIRCULATION:// 流转状态
					statusName = "正在审批";
					formDataTaskId = hts.get(0).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_COMPLETE:// 完成状态
					statusName = "完成";
					formDataTaskId = hts.get(hts.size() - 1).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_REVOKE:// 起草人撤销
					statusName = "起草人撤回";
					formDataTaskId = hts.get(0).getId();
					break;
				default:
					break;
				}

				// 审批标题
				String titleName = startUserInfo.getUserName() + "的" + d.getName();
				// 审批发起时间
				String startTime = DateUtil.getDateStr(firstHt.getStartTime());
				// 组装历史审批人
				MultipleDataSource.setDataSourceKey(null);
				List<ActivitiHistoryTask> ahtList = workFlowService.getFlowRecord(hp.getId());

				// 创建一行
				HSSFRow createRow = createSheet.createRow(currRow);
				currRow++;

				HSSFCell flowIdCell = createRow.createCell(0);
				flowIdCell.setCellStyle(style);
				createSheet.setColumnWidth(0, (short) d.getId().getBytes().length * 256);
				flowIdCell.setCellValue(d.getId());

				HSSFCell titleNameCell = createRow.createCell(1);
				titleNameCell.setCellStyle(style);
				createSheet.setColumnWidth(1, (short) titleName.getBytes().length * 256);
				titleNameCell.setCellValue(titleName);

				HSSFCell statusNameCell = createRow.createCell(2);
				statusNameCell.setCellStyle(style);
				createSheet.setColumnWidth(2, (short) statusName.getBytes().length * 256);
				statusNameCell.setCellValue(statusName);

				HSSFCell startTimeCell = createRow.createCell(3);
				startTimeCell.setCellStyle(style);
				createSheet.setColumnWidth(3, (short) startTime.getBytes().length * 256);
				startTimeCell.setCellValue(startTime);

				HSSFCell startUserMobileCell = createRow.createCell(4);
				startUserMobileCell.setCellStyle(style);
				createSheet.setColumnWidth(4, 4000);
				startUserMobileCell.setCellValue(startUserInfo.getMobile());

				HSSFCell startUserNameCell = createRow.createCell(5);
				startUserNameCell.setCellStyle(style);
				createSheet.setColumnWidth(5, 4000);
				startUserNameCell.setCellValue(startUserInfo.getUserName());

				HSSFCell startUserOrgNameCell = createRow.createCell(6);
				startUserOrgNameCell.setCellStyle(style);
				createSheet.setColumnWidth(6, (short) startUserInfo.getOrgName().getBytes().length * 256);
				startUserOrgNameCell.setCellValue(startUserInfo.getOrgName());

				HSSFCell hisUserCell = createRow.createCell(7);
				hisUserCell.setCellStyle(style);
				HSSFCell hisRecordCell = createRow.createCell(8);
				hisRecordCell.setCellStyle(style);
				String hisUserStr = "";
				String hisRecordStr = "";
				for (ActivitiHistoryTask at : ahtList) {
					hisUserStr += at.getAssignee() + "\n";
					String username = at.getAssignee();
					String time = at.getEndTime() != null ? DateUtil.getDateStr(at.getEndTime()) : "";
					String st = at.getStatus();
					hisRecordStr += (username + "|" + time + "|" + st + ";\n");
				}
				createSheet.setColumnWidth(7, 4000);
				hisUserCell.setCellValue(hisUserStr.substring(0, hisUserStr.length() - 1));
				createSheet.setColumnWidth(8, 11000);
				hisRecordCell.setCellValue(hisRecordStr.substring(0, hisRecordStr.length() - 1));

				HistoricVariableInstance hvData = historyService.createHistoricVariableInstanceQuery()
						.taskId(formDataTaskId).variableName("formData").singleResult();
				if (hvData != null) {
					List<Map> formData = (List<Map>) hvData.getValue();

					for (Map fd : formData) {
						String fid = (String) fd.get("id");
						// 画标题
						if (!formIds.contains(fid)) {
							formIds.add(fid);
							formStartIndex.add(lastCount);
							Map f = (Map) forms.get(fid);
							List<Map> widgets = (List<Map>) f.get("widgets");
							String formName = (String) f.get("formName");
							HSSFCell cCell = titleRow.createCell(lastCount);
							cCell.setCellStyle(style);
							cCell.setCellValue(formName);
							// createSheet.setColumnWidth(lastCount,
							// formName.getBytes().length * 256);
							lastCount++;
							for (Map c : widgets) {
								String cId = (String) c.get("controlId");
								if ("TextNote".equals(cId) || "PictureNote".equals(cId) || "ValidField".equals(cId)) {// 如果是说明控件就跳过
									continue;
								}

								if ("LinkageSelectField".equals(cId)) {
									String[] strs = ((String) c.get("describeName")).split(",");
									for (String str : strs) {
										HSSFCell createCell = titleRow.createCell(lastCount);
										createCell.setCellValue(str);
										createCell.setCellStyle(style);
										createSheet.setColumnWidth(lastCount, str.getBytes().length * 256);
										lastCount++;
									}
								} else {
									String value = (String) c.get("describeName");
									HSSFCell createCell = titleRow.createCell(lastCount);
									createCell.setCellValue(value);
									createCell.setCellStyle(style);
									createSheet.setColumnWidth(lastCount, value.getBytes().length * 256);
									lastCount++;
								}
							}

						}
						int index = formStartIndex.get(formIds.indexOf(fid));
						String formName = (String) fd.get("title");
						List<Map> data = (List<Map>) fd.get("data");

						HSSFCell createCell = createRow.createCell(index);
						createCell.setCellStyle(style);
						createCell.setCellValue(formName);
						createSheet.setColumnWidth(index, formName.getBytes().length * 256);
						index++;
						for (Map d1 : data) {
							String value = "";
							String cId = (String) d1.get("controlName");
							HSSFCell cCell = createRow.createCell(index);
							cCell.setCellStyle(style);
							if ("DDPhotoField".equals(cId) || "DDAttachment".equals(cId)) {
								value = (String) d1.get("value");
								if (StringUtils.isNotEmpty(value)) {
									List<PictureVo> pictureVo_list = JSONObject.parseArray(value, PictureVo.class);
									value = "";
									for (PictureVo pv : pictureVo_list) {
										value += currUrl + pv.getId() + "/" + pv.getName() + "\n";
									}
									value.substring(0, value.length() - 1);
									cCell.setCellStyle(style);
									cCell.setCellValue(value);
									createSheet.setColumnWidth(index, value.getBytes().length * 256);
								}

							} else if ("LinkageSelectField".equals(cId)) {
								List v = (List) d1.get("value");
								cCell.setCellValue(v.get(0) + "");
								index++;
								HSSFCell cCell1 = createRow.createCell(index);
								cCell1.setCellStyle(style);
								cCell1.setCellValue(v.get(1) + "");

							} else if ("TableField".equals(cId)) {
								index++;
								int tIndex = index;
								List<Map> v = (List<Map>) d1.get("value");
								// 合并单元格的参数
								Map<String, Object> regInfo = new HashMap<String, Object>();
								regInfo.put("height", v.size());
								regInfo.put("currRow", currRow - 1);
								Set<Integer> unRegIndex = new HashSet<Integer>();

								for (int ii = 0; ii < v.size(); ii++) {
									HSSFRow r = createRow;
									if (ii != 0) {
										r = createSheet.createRow(currRow);
										// CellRangeAddress region1 = new
										// CellRangeAddress(currRow - 1,
										// currRow, 0,
										// 0);
										// createSheet.addMergedRegion(region1);
										currRow++;
									}
									List<Map> cv = (List<Map>) v.get(ii).get("list");

									tIndex = index;
									for (Map td : cv) {
										String tdcId = (String) td.get("TextField");
										HSSFCell tdcCell = r.createCell(tIndex);
										unRegIndex.add(tIndex);
										tdcCell.setCellStyle(style);
										if ("DDPhotoField".equals(cId) || "DDAttachment".equals(cId)) {
											String tdValue = (String) td.get("value");
											if (StringUtils.isNotEmpty(tdValue)) {
												List<PictureVo> pictureVo_list = JSONObject.parseArray(value,
														PictureVo.class);
												tdValue = "";
												for (PictureVo pv : pictureVo_list) {
													tdValue += currUrl + pv.getId() + "/" + pv.getName() + ".do\n";
												}
												tdValue.substring(0, value.length() - 1);
												tdcCell.setCellStyle(style);
												tdcCell.setCellValue(tdValue);
												createSheet.setColumnWidth(index, tdValue.getBytes().length * 256);
											}
										} else if ("LinkageSelectField".equals(cId)) {
											List tdv = (List) td.get("value");
											tdcCell.setCellValue(tdv.get(0) + "");
											tIndex++;
											HSSFCell cCell1 = createRow.createCell(index);
											unRegIndex.add(tIndex);
											cCell1.setCellValue(tdv.get(1) + "");
											cCell1.setCellStyle(style);
										} else {
											String tdValue = td.get("value") + "";
											tdcCell.setCellValue(tdValue);
											// createSheet.setColumnWidth(index,
											// tdValue.getBytes().length * 256);
										}
										// tIndex++;
									}
								}
								regInfo.put("unIndexs", unRegIndex);
								regionInfos.add(regInfo);
								index = tIndex;
							} else if ("UserSelect".equals(cId)) {
								try {
									String jsonStr = (String) d1.get("value");
									List<Map> users = JSONArray.parseArray(jsonStr, Map.class);
									List<String> userNames = new ArrayList<String>();
									for (Map u : users) {
										userNames.add((String) u.get("userName"));
									}
									value = StringUtils.join(userNames, ",");
									cCell.setCellValue(value);
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}
							} else {
								value = d1.get("value") + "";
								cCell.setCellValue(value);
								// createSheet.setColumnWidth(index,
								// value.getBytes().length * 256);
							}
							index++;
						}

					}
				}
			}

			String[] titleNames = new String[] { "流程编号", "标题", "审批状态", "审批发起时间", "发起人手机号", "发起人姓名", "发起人部门", "历史审批人",
					"审批记录" };
			for (int i = 0; i < titleNames.length; i++) {
				HSSFCell createCell = titleRow.createCell(i);
				createCell.setCellStyle(style);
				createCell.setCellValue(titleNames[i]);
			}

			// 合并单元格
			for (Map rinfo : regionInfos) {
				int height = (Integer) rinfo.get("height");
				int rCurrRow = (Integer) rinfo.get("currRow");
				int width = titleRow.getPhysicalNumberOfCells();
				Set<Integer> unIndex = (Set<Integer>) rinfo.get("unIndexs");
				for (int w = 0; w < width; w++) {
					if (!unIndex.contains(w) && height > 0) {
						try {
							CellRangeAddress region = new CellRangeAddress(rCurrRow, rCurrRow + height - 1, w, w);
							createSheet.addMergedRegion(region);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}

			}
		}
		// 传输给用户
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("content-disposition", "attachment;filename="
					+ new String(definition.get(0).getName().getBytes("utf-8"), "iso8859-1") + ".xls");
			OutputStream out = response.getOutputStream();
			workbook.write(out);
			workbook.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("export")
	@ResponseBody
	public void exportNew(String typeId, String processStatus,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date applyStartTime,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date applyEndTime, HttpServletResponse response) {
		String currUrl = PropertiesUtil.getAppByKey("SERVER_URL") + "spadmin/file/download/";
		// 获取所有改流程的历史任务
		Set<String> userIdsSet = new HashSet<>();
		Map<String, List<HistoricTaskInstance>> allHisTask = getAllHisTaskByDefKey(typeId, userIdsSet);
		List<String> userIdsList = new ArrayList<>(userIdsSet);// 有关系的用户集合
		MultipleDataSource.setDataSourceKey(null);// 切换到主库
		Map<String, List<ActivitiHistoryTask>> allHisTakVo = workFlowService.getActHisTaskByKey(typeId);
		// 获取这个流程的所有实例的数据和实例状态
		Map<String, Integer> processStatusMap = new HashMap<>();// 流程状态集合
		Map<String, List<Map>> processDatasMap = new HashMap<>();// 根据历史任务id存放
		getAllProcessStatusAndFormDatas(typeId, processStatusMap, processDatasMap);
		// 获取用户集合
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		Map<String, UserInfoVo> userInfosMap = userService.getCompanyUserInfos(getCompany().getId(), userIdsList);
		// 产生工作簿对象
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色
		style.setBorderLeft((short) 1); // 边框的大小
		style.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色
		style.setBorderRight((short) 1);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		style.setBorderBottom((short) 1);
		style.setWrapText(true);// 自动换行

		List<ProcessDefinition> definition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey(typeId).list();

		for (int k = definition.size() - 1; k >= 0; k--) {
			// 合并单元格的信息
			List<Map> regionInfos = new ArrayList<Map>();
			ProcessDefinition d = definition.get(k);
			// 画标题
			Map<String, Object> forms = (Map<String, Object>) getProcessConfig(d).get("forms");

			HistoricProcessInstanceQuery processDefinitionKey = historyService.createHistoricProcessInstanceQuery()
					.processDefinitionId(d.getId());

			if (StringUtils.isNotEmpty(processStatus)) {
				processDefinitionKey.variableValueEquals(ActivitiConstant.PROCESS_STATUS, processStatus);
			}
			if (applyStartTime != null) {
				processDefinitionKey.startedAfter(applyStartTime);
			}
			if (applyEndTime != null) {
				processDefinitionKey.startedBefore(applyEndTime);
			}
			List<HistoricProcessInstance> list = processDefinitionKey.list();
			if (list == null || list.isEmpty())
				continue;
			// 新建一个excel页
			HSSFSheet createSheet = workbook.createSheet("版本" + d.getVersion());
			// 创建标题行
			HSSFRow titleRow = createSheet.createRow(0);
			// 表单信息
			List<String> formIds = new ArrayList<String>();
			List<Integer> formStartIndex = new ArrayList<Integer>();
			int lastCount = 9;
			int currRow = 1;
			for (int i = 0; i < list.size(); i++) {
				// 当前的流程
				HistoricProcessInstance hp = list.get(i);
				// 当前流程的所有的历史任务
				List<HistoricTaskInstance> hts = allHisTask.get(hp.getId());
				// 发起者的任务
				HistoricTaskInstance firstHt = hts.get(0);
				// 发起人id
				String ownerId = firstHt.getOwner();
				// 发起人用户信息
				UserInfoVo startUserInfo = userInfosMap.get(ownerId);

				// 当前流程的状态
				int ps = processStatusMap.get(hp.getId());
				String statusName = "";

				// 表单的数据
				String formDataTaskId = "";

				switch (ps) {
				case ActivitiConstant.PROCESS_STATUS_DRAFT:// 起草状态
					statusName = "草拟";
					formDataTaskId = hts.get(0).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_REFUSE:// 拒绝状态
					statusName = "拒绝";
					formDataTaskId = hts.get(hts.size() - 1).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_CIRCULATION:// 流转状态
					statusName = "正在审批";
					formDataTaskId = hts.get(0).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_COMPLETE:// 完成状态
					statusName = "完成";
					formDataTaskId = hts.get(hts.size() - 1).getId();
					break;
				case ActivitiConstant.PROCESS_STATUS_REVOKE:// 起草人撤销
					statusName = "起草人撤回";
					formDataTaskId = hts.get(0).getId();
					break;
				default:
					break;
				}

				// 审批标题
				String titleName = startUserInfo.getUserName() + "的" + d.getName();
				// 审批发起时间
				String startTime = DateUtil.getDateStr(firstHt.getStartTime());
				// 组装历史审批人
				List<ActivitiHistoryTask> ahtList = allHisTakVo.get(hp.getId());

				// 创建一行
				HSSFRow createRow = createSheet.createRow(currRow);
				currRow++;

				HSSFCell flowIdCell = createRow.createCell(0);
				flowIdCell.setCellStyle(style);
				createSheet.setColumnWidth(0, (short) d.getId().getBytes().length * 256);
				flowIdCell.setCellValue(d.getId());

				HSSFCell titleNameCell = createRow.createCell(1);
				titleNameCell.setCellStyle(style);
				createSheet.setColumnWidth(1, (short) titleName.getBytes().length * 256);
				titleNameCell.setCellValue(titleName);

				HSSFCell statusNameCell = createRow.createCell(2);
				statusNameCell.setCellStyle(style);
				createSheet.setColumnWidth(2, (short) statusName.getBytes().length * 256);
				statusNameCell.setCellValue(statusName);

				HSSFCell startTimeCell = createRow.createCell(3);
				startTimeCell.setCellStyle(style);
				createSheet.setColumnWidth(3, (short) startTime.getBytes().length * 256);
				startTimeCell.setCellValue(startTime);

				HSSFCell startUserMobileCell = createRow.createCell(4);
				startUserMobileCell.setCellStyle(style);
				createSheet.setColumnWidth(4, 4000);
				startUserMobileCell.setCellValue(startUserInfo.getMobile());

				HSSFCell startUserNameCell = createRow.createCell(5);
				startUserNameCell.setCellStyle(style);
				createSheet.setColumnWidth(5, 4000);
				startUserNameCell.setCellValue(startUserInfo.getUserName());

				HSSFCell startUserOrgNameCell = createRow.createCell(6);
				startUserOrgNameCell.setCellStyle(style);
				createSheet.setColumnWidth(6, (short) startUserInfo.getOrgName().getBytes().length * 256);
				startUserOrgNameCell.setCellValue(startUserInfo.getOrgName());

				HSSFCell hisUserCell = createRow.createCell(7);
				hisUserCell.setCellStyle(style);
				HSSFCell hisRecordCell = createRow.createCell(8);
				hisRecordCell.setCellStyle(style);
				String hisUserStr = "";
				String hisRecordStr = "";
				for (ActivitiHistoryTask at : ahtList) {
					String username = userInfosMap.get(at.getAssignee()).getUserName();
					hisUserStr += username + "\n";

					String time = at.getEndTime() != null ? DateUtil.getDateStr(at.getEndTime()) : "";
					String st = at.getStatus();
					hisRecordStr += (username + "|" + time + "|" + st + ";\n");
				}
				createSheet.setColumnWidth(7, 4000);
				hisUserCell.setCellValue(hisUserStr.substring(0, hisUserStr.length() - 1));
				createSheet.setColumnWidth(8, 11000);
				hisRecordCell.setCellValue(hisRecordStr.substring(0, hisRecordStr.length() - 1));

				List<Map> formData = processDatasMap.get(formDataTaskId);
				if (formData != null) {
					for (Map fd : formData) {
						String fid = (String) fd.get("id");
						// 画标题
						if (!formIds.contains(fid)) {
							formIds.add(fid);
							formStartIndex.add(lastCount);
							Map f = (Map) forms.get(fid);
							List<Map> widgets = (List<Map>) f.get("widgets");
							String formName = (String) f.get("formName");
							HSSFCell cCell = titleRow.createCell(lastCount);
							cCell.setCellStyle(style);
							cCell.setCellValue(formName);
							// createSheet.setColumnWidth(lastCount,
							// formName.getBytes().length * 256);
							lastCount++;
							for (Map c : widgets) {
								String cId = (String) c.get("controlId");
								if ("TextNote".equals(cId) || "PictureNote".equals(cId) || "ValidField".equals(cId)) {// 如果是说明控件就跳过
									continue;
								}

								if ("LinkageSelectField".equals(cId)) {
									String[] strs = ((String) c.get("describeName")).split(",");
									for (String str : strs) {
										HSSFCell createCell = titleRow.createCell(lastCount);
										createCell.setCellValue(str);
										createCell.setCellStyle(style);
										createSheet.setColumnWidth(lastCount, str.getBytes().length * 256);
										lastCount++;
									}
								} else {
									String value = (String) c.get("describeName");
									HSSFCell createCell = titleRow.createCell(lastCount);
									createCell.setCellValue(value);
									createCell.setCellStyle(style);
									createSheet.setColumnWidth(lastCount, value.getBytes().length * 256);
									lastCount++;
								}
							}

						}
						int index = formStartIndex.get(formIds.indexOf(fid));
						String formName = (String) fd.get("title");
						List<Map> data = (List<Map>) fd.get("data");

						HSSFCell createCell = createRow.createCell(index);
						createCell.setCellStyle(style);
						createCell.setCellValue(formName);
						createSheet.setColumnWidth(index, formName.getBytes().length * 256);
						index++;
						for (Map d1 : data) {
							String value = "";
							String cId = (String) d1.get("controlName");
							HSSFCell cCell = createRow.createCell(index);
							cCell.setCellStyle(style);
							if ("DDPhotoField".equals(cId) || "DDAttachment".equals(cId)) {
								value = (String) d1.get("value");
								if (StringUtils.isNotEmpty(value)) {
									List<PictureVo> pictureVo_list = JSONObject.parseArray(value, PictureVo.class);
									value = "";
									for (PictureVo pv : pictureVo_list) {
										value += currUrl + pv.getId() + "/" + pv.getName() + "\n";
									}
									value.substring(0, value.length() - 1);
									cCell.setCellStyle(style);
									cCell.setCellValue(value);
									createSheet.setColumnWidth(index, value.getBytes().length * 256);
								}

							} else if ("LinkageSelectField".equals(cId)) {
								List v = (List) d1.get("value");
								cCell.setCellValue(v.get(0) + "");
								index++;
								HSSFCell cCell1 = createRow.createCell(index);
								cCell1.setCellStyle(style);
								cCell1.setCellValue(v.get(1) + "");

							} else if ("TableField".equals(cId)) {
								index++;
								int tIndex = index;
								List<Map> v = (List<Map>) d1.get("value");
								// 合并单元格的参数
								Map<String, Object> regInfo = new HashMap<String, Object>();
								regInfo.put("height", v.size());
								regInfo.put("currRow", currRow - 1);
								Set<Integer> unRegIndex = new HashSet<Integer>();

								for (int ii = 0; ii < v.size(); ii++) {
									HSSFRow r = createRow;
									if (ii != 0) {
										r = createSheet.createRow(currRow);
										// CellRangeAddress region1 = new
										// CellRangeAddress(currRow - 1,
										// currRow, 0,
										// 0);
										// createSheet.addMergedRegion(region1);
										currRow++;
									}
									List<Map> cv = (List<Map>) v.get(ii).get("list");

									tIndex = index;
									for (Map td : cv) {
										String tdcId = (String) td.get("controlName");
										HSSFCell tdcCell = r.createCell(tIndex);
										unRegIndex.add(tIndex);
										tdcCell.setCellStyle(style);
										if ("DDPhotoField".equals(tdcId) || "DDAttachment".equals(tdcId)) {
											String tdValue = (String) td.get("value");
											if (StringUtils.isNotEmpty(tdValue)) {
												List<PictureVo> pictureVo_list = JSONObject.parseArray(value,
														PictureVo.class);
												tdValue = "";
												for (PictureVo pv : pictureVo_list) {
													tdValue += currUrl + pv.getId() + "/" + pv.getName() + ".do\n";
												}
												tdValue.substring(0, value.length() - 1);
												tdcCell.setCellStyle(style);
												tdcCell.setCellValue(tdValue);
												createSheet.setColumnWidth(index, tdValue.getBytes().length * 256);
											}
										} else if ("LinkageSelectField".equals(cId)) {
											List tdv = (List) td.get("value");
											tdcCell.setCellValue(tdv.get(0) + "");
											tIndex++;
											HSSFCell cCell1 = createRow.createCell(index);
											unRegIndex.add(tIndex);
											cCell1.setCellValue(tdv.get(1) + "");
											cCell1.setCellStyle(style);
										} else {
											String tdValue = td.get("value") + "";
											tdcCell.setCellValue(tdValue);
											// createSheet.setColumnWidth(index,
											// tdValue.getBytes().length * 256);
										}
										// tIndex++;
									}
								}
								regInfo.put("unIndexs", unRegIndex);
								regionInfos.add(regInfo);
								index = tIndex;
							} else if ("UserSelect".equals(cId)) {
								try {
									String jsonStr = (String) d1.get("value");
									List<Map> users = JSONArray.parseArray(jsonStr, Map.class);
									List<String> userNames = new ArrayList<String>();
									for (Map u : users) {
										userNames.add((String) u.get("userName"));
									}
									value = StringUtils.join(userNames, ",");
									cCell.setCellValue(value);
								} catch (Exception e) {
									log.error(e.getMessage(), e);
								}
							} else {
								value = d1.get("value") + "";
								cCell.setCellValue(value);
								// createSheet.setColumnWidth(index,
								// value.getBytes().length * 256);
							}
							index++;
						}

					}
				}
			}

			String[] titleNames = new String[] { "流程编号", "标题", "审批状态", "审批发起时间", "发起人手机号", "发起人姓名", "发起人部门", "历史审批人",
					"审批记录" };
			for (int i = 0; i < titleNames.length; i++) {
				HSSFCell createCell = titleRow.createCell(i);
				createCell.setCellStyle(style);
				createCell.setCellValue(titleNames[i]);
			}

			// 合并单元格
			for (Map rinfo : regionInfos) {
				int height = (Integer) rinfo.get("height");
				int rCurrRow = (Integer) rinfo.get("currRow");
				int width = titleRow.getPhysicalNumberOfCells();
				Set<Integer> unIndex = (Set<Integer>) rinfo.get("unIndexs");
				for (int w = 0; w < width; w++) {
					if (!unIndex.contains(w) && height > 1) {
						try {
							CellRangeAddress region = new CellRangeAddress(rCurrRow, rCurrRow + height - 1, w, w);
							createSheet.addMergedRegion(region);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}

			}
		}
		// 传输给用户
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("content-disposition", "attachment;filename="
					+ new String(definition.get(0).getName().getBytes("utf-8"), "iso8859-1") + ".xls");
			OutputStream out = response.getOutputStream();
			workbook.write(out);
			workbook.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * 获取流程配置信息
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getProcessConfig(ProcessDefinition processDefinition) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> tasksConfig = new HashMap<String, Object>();

		StartFormData data = formService.getStartFormData(processDefinition.getId());

		for (FormProperty fp : data.getFormProperties()) {
			if (ActivitiConstant.ALL_FORM_DETAIL.equals(fp.getId())) {
				resultMap = JSONObject.parseObject(fp.getValue(), Map.class);
			} else if (ActivitiConstant.TASKS_CONFIG.equals(fp.getId())) {
				tasksConfig = JSONObject.parseObject(fp.getValue(), Map.class);
			}
		}
		resultMap.put("tasksConfig", tasksConfig);
		return resultMap;
	}

	/**
	 * 根据流程实例id封装历史任务
	 * 
	 * @param key
	 * @return
	 */
	private Map<String, List<HistoricTaskInstance>> getAllHisTaskByDefKey(String key, Set<String> userIdsSet) {
		// 查询这个流程定义key的所有的任务集合
		List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery().processDefinitionKey(key)
				.orderByTaskCreateTime().asc().list();
		Map<String, List<HistoricTaskInstance>> resultMap = new HashMap<>();// 返回的数据（根据不同流程定义分类）
		for (HistoricTaskInstance ht : taskList) {
			String processInstanceId = ht.getProcessInstanceId();// 获取流程的实例id
			List<HistoricTaskInstance> htList = resultMap.get(processInstanceId);// 获取这个实例的所有的任务集合
			if (htList == null) {
				htList = new ArrayList<>();
			}
			userIdsSet.add(ht.getAssignee());// 把办理人放入人员集合中
			userIdsSet.add(ht.getOwner());// 把发我人放到用户集合中
			htList.add(ht);// 往一个流程实例的所有任务集合中添加遍历的历史任务
			resultMap.put(processInstanceId, htList);
		}
		return resultMap;
	}

	/**
	 * 根据key查询并封装流程全局数据
	 * 
	 * @param key
	 *            流程的key
	 * @param processStatusMap
	 *            流程状态的map集合
	 * @param processDatasMap
	 *            流程表单数据的map集合
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getAllProcessStatusAndFormDatas(String key, Map<String, Integer> processStatusMap,
			Map<String, List<Map>> processDatasMap) {
		// 原生sql查询流程的全局数据
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT RES.* FROM ").append(managementService.getTableName(HistoricVariableInstance.class))
				.append(" RES INNER JOIN ").append(managementService.getTableName(HistoricProcessInstance.class))
				.append(" hp ON hp.PROC_INST_ID_ = RES.PROC_INST_ID_ ").append(" INNER JOIN ")
				.append(managementService.getTableName(ProcessDefinition.class))
				.append(" rp ON rp.ID_ = hp.PROC_DEF_ID_  ").append("WHERE rp.KEY_= #{defKey} and (RES.NAME_= '"
						+ ActivitiConstant.PROCESS_STATUS + "' or RES.NAME_=" + "'formData'" + ")");
		List<HistoricVariableInstance> list = historyService.createNativeHistoricVariableInstanceQuery()
				.sql(sb.toString()).parameter("defKey", key).list();
		
		// 循环根据流程实例id存在数据
		for (HistoricVariableInstance hv : list) {
			if (ActivitiConstant.PROCESS_STATUS.equals(hv.getVariableName())) {
				int status = (int) getValue(hv);
				processStatusMap.put(hv.getProcessInstanceId(), status);
			} else if ("formData".equals(hv.getVariableName())) {
				List<Map> formData = (List<Map>) getValue(hv);
				processDatasMap.put(hv.getTaskId(), formData);
			}
		}
	}
	
	private Object getValue(HistoricVariableInstance hv) {
		if (hv.getVariableTypeName().equals("serializable")) {
			// 查询ACT_GE_BYTEARRAY表并反序列化
			HistoricVariableInstanceEntity entity = (HistoricVariableInstanceEntity) hv;
			ByteArrayEntity byteArrayEntity = managementService.executeCommand(new getByteArrayVariableCmd(entity.getByteArrayValueId()));
			SerializableType serializableType = ((SerializableType) entity.getVariableType());
			Object deserialize = serializableType.deserialize(byteArrayEntity.getBytes(), entity);
			return deserialize;
		} else {
			return hv.getValue();
		}
	}
}
