package cmcc.mobile.admin.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.management.RuntimeErrorException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.druid.sql.visitor.functions.Concat;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cmcc.mobile.admin.controller.ExportController;
import cmcc.mobile.admin.dao.ApprovalBatchTaskMapper;
import cmcc.mobile.admin.dao.ApprovalDataMapper;
import cmcc.mobile.admin.dao.ApprovalRunManageMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigDetailsMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.TemporaryBatchStartMapper;
import cmcc.mobile.admin.dao.ThirdApprovalDealMapper;
import cmcc.mobile.admin.dao.UserApprovalDefDataMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.ApprovalBatchTask;
import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.entity.ApprovalRunManage;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.TemporaryBatchStart;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.entity.UserApprovalDefData;
import cmcc.mobile.admin.service.ApprovalBatchTaskService;
import cmcc.mobile.admin.service.WorkFlowService;
import cmcc.mobile.admin.util.PropertiesUtil;
import cmcc.mobile.admin.vo.PictureVo;
import cmcc.mobile.admin.vo.UserInfoVo;

/**
 *
 * @author renlinggao
 * @Date 2016年8月26日
 */
@Service
public class ApprovalBatchTaskServiceImpl implements ApprovalBatchTaskService {

	@Autowired
	private ApprovalBatchTaskMapper approvalBatchTaskMapper;

	@Autowired
	private TemporaryBatchStartMapper temporaryBatchStartMapper;

	@Autowired
	private UserApprovalDefDataMapper userApprovalDefDataMapper;

	@Autowired
	private ApprovalTypeMapper approvalTypeMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private ApprovalTableConfigDetailsMapper approvalTableConfigDetailsMapper;

	@Autowired
	private ApprovalDataMapper approvalDataMapper;

	@Autowired
	private WorkFlowService workFlowService;

	@Autowired
	private ApprovalRunManageMapper approvalRunManageMapper;

	@Autowired
	private ThirdApprovalDealMapper thirdApprovalDealMapper;

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void addTask(ApprovalBatchTask task) {
		approvalBatchTaskMapper.insert(task);
	}

	@Override
	public List<ApprovalBatchTask> findByCondition(ApprovalBatchTask task) {
		return approvalBatchTaskMapper.findByCondition(task);
	}

	@Override
	public void delete(Long id) {
		// 逻辑删除初始数据
		UserApprovalDefData data = new UserApprovalDefData();
		data.setTaskId(id);
		data.setStatus("0");
		userApprovalDefDataMapper.updateByTaskIdSelective(data);

		// 删除暂存的导入人员的数据
		TemporaryBatchStart start = new TemporaryBatchStart();
		start.setTaskId(id);
		start.setStatus(0);
		temporaryBatchStartMapper.updateByTaskIdSelective(start);

		// 删除任务
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(id);
		task.setStatus(9);
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);
	}

	@Override
	public void edit(ApprovalBatchTask task) {
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);
	}

	@Override
	public ApprovalBatchTask checkName(ApprovalBatchTask task) {
		return approvalBatchTaskMapper.checkName(task);
	}

	@Override
	public Map<String, Object> export(Long taskId, String companyId) {
		// 返回前端的excel表
		Map<String, Object> result = new HashMap<String, Object>();
		HSSFWorkbook book = new HSSFWorkbook();
		ApprovalBatchTask task = approvalBatchTaskMapper.selectByPrimaryKey(taskId);

		if (task == null)
			throw new RuntimeException("任务不存在");
		if (!companyId.equals(task.getCompanyId()))
			throw new RuntimeException("没有权限");
		// 单元格样式
		HSSFCellStyle style = book.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色
		style.setBorderLeft((short) 1); // 边框的大小
		style.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色
		style.setBorderRight((short) 1);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		style.setBorderBottom((short) 1);
		style.setWrapText(true);// 自动换行
		// 获取流程的相关数据
		ApprovalType type = approvalTypeMapper.selectByPrimaryKey(task.getApprovalTypeId());
		// 获取批量发起的数据
		TemporaryBatchStart start = new TemporaryBatchStart();
		start.setTaskId(taskId);
		List<TemporaryBatchStart> temDeal = temporaryBatchStartMapper.findByTaskIdAndUserId(start);
		// 创建初始数据页
		createInitDataSheet(book, taskId, style);
		if (temDeal != null && !temDeal.isEmpty()) {
			// 创建初始导入人数页
			createInitUser(book, temDeal, style);
			// 数据导出
			createExportData(book, temDeal, style, type);
		}
		result.put("name", task.getTaskName());
		result.put("workBook", book);
		return result;
	}

	/**
	 * 创建需要导出的数据页
	 * 
	 * @param book
	 * @param temDeal
	 * @param style
	 */
	private void createExportData(HSSFWorkbook book, List<TemporaryBatchStart> temDeal, HSSFCellStyle style,
			ApprovalType type) {
		String CONTROL_TYPE = "controlName";// 控件的类型
		String CONTROL_ID = "id";// 控件id
		String CONTROL_VALUE = "value";// 控件的值
		// 合并单元格的参数
		List<Map> regInfos = new ArrayList<Map>();
		// 下载图片文件的路径
		String currUrl = PropertiesUtil.getAppByKey("SERVER_URL") + "spadmin/file/download/";
		HSSFSheet sheet = book.createSheet("流程数据");
		HSSFRow titleRow = sheet.createRow(0);
		String titles[] = new String[] { "审批编号", "标题", "审批状态", "审批结果", "审批发起时间", "审批完成时间", "发起人手机号", "发起人姓名", "发起人部门",
				"历史审批人", "审批记录", "当前处理人", "审批耗时" };
		for (int i = 0; i < titles.length; i++) {
			HSSFCell titleCell = titleRow.createCell(i);
			titleCell.setCellStyle(style);
			sheet.setColumnWidth(i, titles[i].getBytes().length * 256);
			titleCell.setCellValue(titles[i]);
		}
		String configId = temDeal.get(0).getApprovalTableConfigId();
		List<ApprovalTableConfigDetails> details = approvalTableConfigDetailsMapper.getApprovalInfoById1(configId);
		// 记录当前的标题位置
		int titleColumn = titles.length;
		// excel插入标题
		for (ApprovalTableConfigDetails c : details) {

			HSSFCell titleCell = titleRow.createCell(titleColumn);
			titleCell.setCellStyle(style);

			if ("TextNote".equals(c.getControlId()) || "PictureNote".equals(c.getControlId())
					|| "ValidField".equals(c.getControlId())) {// 如果是说明控件就跳过
				continue;
			}

			if ("LinkageSelectField".equals(c.getControlId())) {
				String[] strs = c.getDescribeName().split(",");
				if (strs.length == 2) {
					titleCell.setCellValue(strs[0]);
					titleColumn++;
					HSSFCell tCell = titleRow.createCell(titleColumn);
					tCell.setCellStyle(style);
					tCell.setCellValue(strs[1]);
				}
			} else {
				titleCell.setCellValue(c.getDescribeName());
			}
			titleColumn++;
		}
		// 填写表单数据
		int currRowNum = 1;// 当前行
		for (TemporaryBatchStart s : temDeal) {
			HSSFRow currRow = sheet.createRow(currRowNum);
			// 获取当前发起的数据
			String flowId = s.getFlowId();
			ApprovalData data = approvalDataMapper.selectByPrimaryKey(flowId);
			// 设置流程数据
			// 设置审批编号
			HSSFCell flowIdCell = currRow.createCell(0);
			flowIdCell.setCellStyle(style);

			// 设置流程标题
			HSSFCell typeNameCell = currRow.createCell(1);
			typeNameCell.setCellStyle(style);
			typeNameCell.setCellValue(type.getName());

			// 审批状态
			HSSFCell statusCell = currRow.createCell(2);
			statusCell.setCellStyle(style);

			// 审批结果
			HSSFCell endCell = currRow.createCell(3);
			endCell.setCellStyle(style);
			// 审批发起时间
			HSSFCell startTimeCell = currRow.createCell(4);
			startTimeCell.setCellStyle(style);

			// 审批结束时间
			HSSFCell finshTimeCell = currRow.createCell(5);
			String value5 = "";
			finshTimeCell.setCellStyle(style);
			// 发起人手机号
			HSSFCell startUserMobileCell = currRow.createCell(6);
			startUserMobileCell.setCellStyle(style);

			// 发起人姓名
			HSSFCell startUserNameCell = currRow.createCell(7);
			startUserNameCell.setCellStyle(style);

			// 发起人部门
			HSSFCell startUserOrgNameCell = currRow.createCell(8);
			startUserOrgNameCell.setCellStyle(style);

			if (StringUtils.isNotEmpty(flowId) && data != null) {
				UserInfoVo startUserInfo = userMapper.findById(data.getUserId());
				String endStr = ExportController.status2String(data.getStatus(), true);
				String statusStr = ExportController.status2String(data.getStatus(), false);
				flowIdCell.setCellValue(flowId);
				statusCell.setCellValue(statusStr);
				startTimeCell.setCellValue(data.getDraftDate());
				finshTimeCell.setCellValue(value5);
				startUserMobileCell.setCellValue(startUserInfo.getMobile());
				startUserNameCell.setCellValue(startUserInfo.getUserName());
				startUserOrgNameCell.setCellValue(startUserInfo.getOrgName());
				endCell.setCellValue(endStr);
				// 历史审批人 (查找流程扭转记录)
				List<ApprovalRunManage> manages = approvalRunManageMapper.selectByApprovalId(data.getFlowId());
				// 审批人
				String appPersons = "";
				// 审批记录
				String appRecords = "";
				// 当前审批人
				String curPerson = "";
				for (ApprovalRunManage manage : manages) {
					if (StringUtils.isNotEmpty(manage.getUserId()) && StringUtils.isNotEmpty(manage.getExamineDate())) {
						User u = userMapper.selectByPrimaryKey(manage.getUserId());
						appPersons += u.getUserName() + "，";
						appRecords += u.getUserName() + " | " + manage.getExamineDate() + " | " + manage.getOpinion()
								+ "; \n";
					}
					if (StringUtils.isNotEmpty(manage.getRunStatus()) && manage.getRunStatus().equals("1")) {
						User u = userMapper.selectByPrimaryKey(data.getUserId());
						curPerson = u.getUserName();
					}

				}
				appPersons = "".equals(appPersons) ? "" : appPersons.substring(0, appPersons.lastIndexOf("，"));
				appRecords = "".equals(appRecords) ? "" : appRecords.substring(0, appRecords.lastIndexOf("\n"));

				HSSFCell hisPerCell = currRow.createCell(9);
				String value9 = appPersons;
				hisPerCell.setCellValue(value9);
				hisPerCell.setCellStyle(style);
				sheet.setColumnWidth(9, 6000);

				HSSFCell hisStepCell = currRow.createCell(10);
				String value10 = appRecords;
				hisStepCell.setCellValue(value10);
				hisStepCell.setCellStyle(style);
				sheet.setColumnWidth(10, 13000);
				// 当前处理人 11
				HSSFCell currPerson = currRow.createCell(11);
				String value11 = curPerson;
				currPerson.setCellValue(value11);
				currPerson.setCellStyle(style);
				sheet.setColumnWidth(11, 3000);

				// 解析数据
				Map<String, Object> jsd = JSONObject.parseObject(data.getJsonData(), Map.class);
				List<Map> jsonData = (List<Map>) jsd.get("data");
				int currCol = titles.length;// 当前单元格列数
				// 循环所有的控件数据
				for (Map d : jsonData) {
					String cId = (String) d.get(CONTROL_ID);
					String cType = (String) d.get(CONTROL_TYPE);
					String cValue = "";
					// 创建单元格
					HSSFCell cell = currRow.createCell(currCol);
					cell.setCellStyle(style);
					if ("TableField".equals(cType)) {
						// 填写谁的明细TODO
						currCol++;
						int tfCol = currCol;
						// 获取明细的条数
						List<Map> tfData = (List<Map>) d.get(CONTROL_VALUE);
						// 设置合并单元格的值
						Map<String, Object> reinfo = new HashMap<String, Object>();
						reinfo.put("currRow", currRowNum);
						reinfo.put("height", tfData.size());
						Set<Integer> unRegIndex = new HashSet<Integer>();
						reinfo.put("unIndexs", unRegIndex);
						regInfos.add(reinfo);

						// 循环明细的数据
						for (int ii = 0; ii < tfData.size(); ii++) {
							tfCol = currCol;
							// 循环一个明细中的控件集合
							List<Map> oneData = (List<Map>) tfData.get(ii).get("list");
							// 初始化明细的单元格行
							HSSFRow tfr = currRow;
							if (ii != 0) {
								currRowNum++;
								tfr = sheet.createRow(currRowNum);
							}
							// 循环一个明细的控件
							for (Map tfcd : oneData) {
								String tfcId = (String) tfcd.get(CONTROL_ID);
								String tfcType = (String) tfcd.get(CONTROL_TYPE);

								HSSFCell tfCell = tfr.createCell(tfCol);
								unRegIndex.add(tfCol);
								tfCell.setCellStyle(style);
								if ("DDAttachment".equals(cType) || "DDPhotoField".equals(cType)) {
									String tfValue = tfcd.get(CONTROL_VALUE) + "";
									if (StringUtils.isNotEmpty(tfValue)) {
										List<Map> pv = JSONArray.parseArray(tfValue, Map.class);
										List<String> pUrlList = new ArrayList<String>();
										for (Map picMap : pv) {
											String purl = currUrl + picMap.get("id") + "/" + picMap.get("name") + ".do";
											sheet.setColumnWidth(currCol, purl.getBytes().length * 256);
											pUrlList.add(purl);
										}
										tfCell.setCellValue(StringUtils.join(pUrlList, "\n"));
									}
								} else if ("LinkageSelectField".equals(cType)) {
									List value = (List) tfcd.get(CONTROL_VALUE);
									if (value.size() == 2) {
										tfCell.setCellValue(value.get(0) + "");
										tfCol++;
										HSSFCell lc = currRow.createCell(currCol);
										lc.setCellStyle(style);
										lc.setCellValue(value.get(1) + "");
										unRegIndex.add(tfCol);
									}
								} else {
									tfCell.setCellValue(tfcd.get(CONTROL_VALUE) + "");
								}
								tfCol++;
							}

						}
						currCol = tfCol - 1;
					} else if ("DDAttachment".equals(cType) || "DDPhotoField".equals(cType)) {
						cValue = d.get(CONTROL_VALUE) + "";
						if (StringUtils.isNotEmpty(cValue)) {
							List<Map> pv = JSONArray.parseArray(cValue, Map.class);
							List<String> pUrlList = new ArrayList<String>();
							for (Map picMap : pv) {
								String purl = currUrl + picMap.get("id") + "/" + picMap.get("name") + ".do";
								sheet.setColumnWidth(currCol, purl.getBytes().length * 256);
								pUrlList.add(purl);
							}
							cell.setCellValue(StringUtils.join(pUrlList, "\n"));
						}
					} else if ("LinkageSelectField".equals(cType)) {
						List value = (List) d.get(CONTROL_VALUE);
						if (value.size() == 2) {
							cell.setCellValue(value.get(0) + "");
							currCol++;
							HSSFCell lc = currRow.createCell(currCol);
							lc.setCellStyle(style);
							lc.setCellValue(value.get(1) + "");
						}
					} else {
						cValue = d.get(CONTROL_VALUE) + "";
						cell.setCellValue(cValue);
					}
					currCol++;
				}
			} else {
				UserInfoVo userInfo = userMapper.findById(s.getUserId());
				statusCell.setCellValue("未提交");
				startUserMobileCell.setCellValue(userInfo.getMobile());
				startUserNameCell.setCellValue(userInfo.getUserName());
				startUserOrgNameCell.setCellValue(userInfo.getOrgName());
			}
			currRowNum++;
		}

		// 合并单元格
		for (Map rinfo : regInfos) {
			int height = (Integer) rinfo.get("height");
			int rCurrRow = (Integer) rinfo.get("currRow");
			int width = titleRow.getPhysicalNumberOfCells();
			Set<Integer> unIndex = (Set<Integer>) rinfo.get("unIndexs");
			for (int w = 0; w < width; w++) {
				if (unIndex.contains(w))
					continue;
				CellRangeAddress region = new CellRangeAddress(rCurrRow, rCurrRow + height - 1, w, w);
				sheet.addMergedRegion(region);
			}
		}
	}

	/**
	 * 新建初始导入人数的sheet
	 * 
	 * @param book
	 * @param temDeal
	 */
	private void createInitUser(HSSFWorkbook book, List<TemporaryBatchStart> temDeal, HSSFCellStyle style) {
		HSSFSheet sheet = book.createSheet("导入人数");

		HSSFRow titleRow = sheet.createRow(0);

		HSSFCell titleMobileCell = titleRow.createCell(0);
		titleMobileCell.setCellStyle(style);
		titleMobileCell.setCellValue("手机号");
		sheet.setColumnWidth(0, "手机号   ".getBytes().length * 256);

		HSSFCell titleNameCell = titleRow.createCell(1);
		titleNameCell.setCellStyle(style);
		titleNameCell.setCellValue("姓名");
		sheet.setColumnWidth(1, "姓名姓名".getBytes().length * 256);

		for (int i = 0; i < temDeal.size(); i++) {
			TemporaryBatchStart start = temDeal.get(i);

			HSSFRow createRow = sheet.createRow(i + 1);
			HSSFCell mobileCell = createRow.createCell(0);
			mobileCell.setCellStyle(style);
			mobileCell.setCellValue(start.getMobile());

			HSSFCell nameCell = createRow.createCell(1);
			nameCell.setCellStyle(style);
			nameCell.setCellValue(start.getUserName());
		}
	}

	/**
	 * 创建初始表单数据
	 * 
	 * @param book
	 * @param taskId
	 * @param type
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createInitDataSheet(HSSFWorkbook book, Long taskId, HSSFCellStyle style) {
		HSSFSheet sheet = book.createSheet("导入的初始数据");
		// 查询初始数据
		UserApprovalDefData data = new UserApprovalDefData();
		data.setTaskId(taskId);
		List<UserApprovalDefData> initDatas = userApprovalDefDataMapper.findByCondition(data);

		if (initDatas != null && !initDatas.isEmpty()) {
			// 控件对应的位置
			Map<String, Integer> cIndex = new HashMap<String, Integer>();
			// 手机号对应的行数
			Map<String, Integer> mIndex = new HashMap<String, Integer>();
			// excel行
			int row = 1;
			// 获取所有的控件
			String tableConfigId = initDatas.get(0).getApprovalTableConfigId();
			List<ApprovalTableConfigDetails> details = approvalTableConfigDetailsMapper
					.getApprovalInfoById(tableConfigId);
			// 标题行
			HSSFRow titleRow = sheet.createRow(0);
			// 手机号标题
			HSSFCell mobileCell = titleRow.createCell(0);
			mobileCell.setCellValue("手机号");
			mobileCell.setCellStyle(style);
			sheet.setColumnWidth(0, "手机号    ".getBytes().length * 256);
			// 名称标题
			HSSFCell nameCell = titleRow.createCell(1);
			nameCell.setCellValue("名字");
			nameCell.setCellStyle(style);
			sheet.setColumnWidth(1, "名字名字".getBytes().length * 256);

			for (int i = 0; i < details.size(); i++) {
				cIndex.put(details.get(i).getReName(), i + 2);
				HSSFCell cCell = titleRow.createCell(i + 2);
				String value = details.get(i).getDescribeName();
				// 如果是说明控件
				if ("TextNote".equals(details.get(0).getControlId()))
					value = details.get(0).getExp();
				cCell.setCellValue(value);
				cCell.setCellStyle(style);
				if (StringUtils.isNotEmpty(value)) {
					sheet.setColumnWidth(i + 2, value.getBytes().length * 256);
				}
			}

			for (UserApprovalDefData d : initDatas) {
				String mobile = d.getMobile();
				String cid = d.getControlId();
				String companyId = d.getCompanyId();// 公司id
				String json = d.getJsonData();// 初始数据
				// 判断该手机号是否有位置
				if (mIndex.get(mobile) == null) {
					int r = row;
					mIndex.put(mobile, r);
					row++;
				}
				// 或者这一行excel的位置
				int currRow = mIndex.get(mobile);
				int currColumn = cIndex.get(cid);

				HSSFRow hrow = sheet.getRow(currRow);
				if (hrow == null)
					hrow = sheet.createRow(currRow);

				// 填入手机号和名字
				HSSFCell mCell = hrow.getCell(0);
				if (mCell == null) {
					mCell = hrow.createCell(0);
					mCell.setCellValue(mobile);
					mCell.setCellStyle(style);
				}
				// 设置用户名字
				HSSFCell nCell = hrow.getCell(1);
				if (nCell == null) {
					nCell = hrow.createCell(1);

					HashMap<String, String> param = new HashMap<>();
					param.put("mobile", mobile);
					param.put("companyId", companyId);
					User user = userMapper.selectByMobile(param);
					nCell.setCellValue(user.getUserName());
					nCell.setCellStyle(style);
				}
				// 填控件数据
				HSSFCell cell = hrow.getCell(currColumn);
				if (cell == null)
					cell = hrow.createCell(currColumn);
				cell.setCellStyle(style);
				String cValue = "";
				if (StringUtils.isNotEmpty(json)
						&& (cid.startsWith("DDSelectField") || cid.startsWith("DDMultiSelectField"))) {// 如果是单行或者多行选择框
					try {
						Map<String, Object> jsonMap = JSONObject.parseObject(json);
						List<String> ops = (List<String>) jsonMap.get("options");
						cValue = StringUtils.join(ops, "/");
					} catch (Exception e) {
						logger.error("手机号：" + mobile);
						logger.error(e.getMessage(), e);
					}
				} else if (StringUtils.isNotEmpty(json) && cid.startsWith("LinkageSelectField")) {// 如果是联动选择框
					try {
						Map jsonMap = JSONObject.parseObject(json);
						List<String> temList = new ArrayList<String>();
						List<Map> ops = (List<Map>) ((Map) jsonMap.get("linkageOption")).get("selects");
						for (Map p : ops) {
							// 获取父元素名称
							String pStr = p.get("value") + "";
							List<Map> child = (List<Map>) p.get("children");
							List<String> cList = new ArrayList<String>();
							for (Map c : child) {
								String temC = c.get("value") + "";
								cList.add(temC);
							}
							// 导出初始数据
							String oneAll = pStr + "(" + StringUtils.join(cList, ",") + ")";
							temList.add(oneAll);
						}
						cValue = StringUtils.join(temList, "/");
					} catch (Exception e) {
						logger.error("手机号：" + mobile);
						logger.error(e.getMessage(), e);
					}
				} else {
					cValue = json;
				}
				cell.setCellValue(cValue);
			}
		}

	}

	@Override
	public String taskRevoke(HttpServletRequest request, Long taskId) {
		String filePath = PropertiesUtil.getAppByKey("PERSON_INFO_FILE_PATH");// 文件上传保存的地址
		MultipartHttpServletRequest mr = (MultipartHttpServletRequest) request;
		File file = null;
		String mess = "";
		for (Iterator<MultipartFile> ite = mr.getFileMap().values().iterator(); ite.hasNext();) {
			MultipartFile multipartFile = ite.next();
			if (multipartFile.getSize() > 0) {
				// 获取文件名
				String fileName = multipartFile.getOriginalFilename();
				// 获取文件后缀名
				String extension = FilenameUtils.getExtension(fileName);

				file = new File(filePath, UUID.randomUUID().toString() + "." + extension);

				if (file.exists()) {
					file.delete();
				}
				HSSFWorkbook hbook = null;
				XSSFWorkbook xbook = null;
				FileInputStream fileIn = null;
				try {
					if (extension.equals("xls")) {
						multipartFile.transferTo(file);
						hbook = new HSSFWorkbook(fileIn);
						mess = readBook(hbook, taskId);
					} else if (extension.equals("xlsx")) {
						multipartFile.transferTo(file);
						xbook = new XSSFWorkbook(file);
						mess = readBook(xbook, taskId);
					} else {
						mess = "文件格式不对";
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage());
				} finally {
					try {
						if (fileIn != null)
							fileIn.close();
						if (hbook != null)
							hbook.close();
						if (xbook != null)
							xbook.close();
						if (file != null && file.exists())
							file.delete();
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}

			}

		}

		return mess;
	}

	/**
	 * 
	 * @param book
	 * @param taskId
	 * @return
	 */
	private String readBook(Workbook book, Long taskId) {
		ApprovalBatchTask task = approvalBatchTaskMapper.selectByPrimaryKey(taskId);
		if (task == null)
			throw new RuntimeException("任务不存在");
		String resultMess = "";
		// 导入的手机号集合
		List<String> mobiles = new ArrayList<String>();
		// 解析excel
		Sheet sheet = book.getSheetAt(0);
		int rowNum = sheet.getPhysicalNumberOfRows();// 行数
		for (int i = 0; i < rowNum; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(0);
			if (cell != null) {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String mob = cell.getStringCellValue();
				mobiles.add(mob);
			}
		}
		int tr = 0;
		int dr = 0;
		if (mobiles != null) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("list", mobiles);
			params.put("taskId", taskId);
			tr = temporaryBatchStartMapper.revokeByMobiles(params);
			dr = thirdApprovalDealMapper.revokeByMobiles(params);
		}
		// 更新撤销数量
		int cancelNum = task.getUndotaskUsers() != null ? task.getUndotaskUsers() : 0;
		cancelNum = cancelNum + dr;
		task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setUndotaskUsers(cancelNum);
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);
		resultMess = "撤销代办" + dr + "条";
		return resultMess;
	}

}
