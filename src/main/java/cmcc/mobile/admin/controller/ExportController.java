package cmcc.mobile.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.dao.ApprovalTableConfigDetailsMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.OrganizationMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.entity.ApprovalRunManage;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.ApprovalRunManageService;
import cmcc.mobile.admin.service.CustomFormService;
import cmcc.mobile.admin.service.IApproveDataService;
import cmcc.mobile.admin.util.DBJSONObject;
import cmcc.mobile.admin.vo.ExportVO;
import cmcc.mobile.admin.vo.PictureVo;

/**
 * 数据导出处理
 * 
 * 
 *
 */
@Controller
@RequestMapping("/export")
public class ExportController extends BaseController {
	@Autowired
	private IApproveDataService approveDataService;

	@Autowired
	private ApprovalTypeMapper approvalTypeMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private OrganizationMapper organizationMapper;

	@Autowired
	private ApprovalTableConfigMapper approvalTableConfigMapper;

	@Autowired
	private ApprovalRunManageService approvalRunManageMapper;

	@Autowired
	private ApprovalTableConfigDetailsMapper configDetailsMapper;

	@Autowired
	private CustomFormService customFormService;

	/**
	 * @param export
	 */
	@RequestMapping("/exportExcel")
	public void export(ExportVO export, HttpServletResponse response, HttpServletRequest request) {

		// export.setTypeId("zhoubao");//周报的Id
		/**
		 * 获取到所有模板
		 */
		MultipleDataSource.setDataSourceKey("business1");
		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(export.getId());

		if (null != approvalType) {
			response.setContentType("application/vnd.ms-excel");
			String fileName = null;
			try {
				fileName = new String(approvalType.getName().getBytes("gb2312"), "iso8859-1");
			} catch (UnsupportedEncodingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			response.setHeader("content-disposition", "attachment;filename=" + fileName + ".xls");

			String titls[] = new String[] { "审批编号", "标题", "审批状态", "审批结果", "审批发起时间", "审批完成时间", "发起人手机号", "发起人姓名",
					"发起人部门", "历史审批人", "审批记录", "当前处理人", "审批耗时" };

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

			List<ApprovalTableConfig> configs = approvalTableConfigMapper.selectByApprovalTypeId(approvalType.getId());

			// 循环创建sheet
			HashMap<String, Object> params = this.setParams(export);
			int count = 1;
			for (ApprovalTableConfig type : configs) {

				/* 查询对应模板 */
				List<String> heads = new ArrayList<String>(Arrays.asList(titls));
				// details:表头名称列表
				List<ApprovalTableConfigDetails> details = configDetailsMapper
						.getApprovalInfoById1(String.valueOf(type.getId()));
				for (ApprovalTableConfigDetails detail : details) {
					if ("TextNote".equals(detail.getControlId()) || "PictureNote".equals(detail.getControlId())
							|| "ValidField".equals(detail.getControlId())) {// 如果是说明控件就跳过
						continue;
					}

					if ("LinkageSelectField".equals(detail.getControlId())) {
						String[] strs = detail.getDescribeName().split(",");
						for (String str : strs) {
							heads.add(str);
						}
					} else {
						heads.add(detail.getDescribeName());
					}

				}

				// 每个模板创建一个sheet
				// HSSFSheet sheet = workbook.createSheet("Sheet " + count++);
				HSSFSheet sheet = workbook
						.createSheet(type.getDate() != null ? type.getDate().replace(":", "") : "Sheet" + count++);
				// 设置列头
				HSSFRow head = sheet.createRow(0);
				head.setHeight((short) 300);
				sheet.setDefaultColumnWidth(10);

				for (int i = 0; i < heads.size(); i++) {
					HSSFCell cell = head.createCell(i);
					cell.setCellValue(heads.get(i));
					cell.setCellStyle(style);
				}
				// 参数封装
				params.put("configId", null);
				params.put("configId", type.getId());
				// 查询数据
				List<ApprovalData> datas = approveDataService.selectByParams(params);
				int rowCount = 1;
				for (int i = 0; i < datas.size(); i++) {
					// 审批实例
					ApprovalData data = datas.get(i);

					if (StringUtils.isNotEmpty(data.getJsonData())) {
						DBJSONObject object = JSONObject.parseObject(data.getJsonData(), DBJSONObject.class);
						// 封装一条数据
						List<DBJSONObject> list = JSONObject.parseArray(object.getData(), DBJSONObject.class);
						/**
						 * 开始解析数据
						 */
						Boolean isDetail = false;
						// 明细数据列表
						List<DBJSONObject> list_details = new ArrayList<DBJSONObject>();
						List<DBJSONObject> list_detailm = new ArrayList<DBJSONObject>();
						// 非明细列表
						List<DBJSONObject> list_detailz = new ArrayList<DBJSONObject>();
						for (DBJSONObject dbjsonObject : list) {
							//
							if (StringUtils.isNotEmpty(dbjsonObject.getControlName())
									&& dbjsonObject.getControlName().equals("TableField")) {
								if (list_details == null || list_details.size() == 0) {
									list_details = JSONObject.parseArray(dbjsonObject.getValue(), DBJSONObject.class);
								} else {
									list_detailm = JSONObject.parseArray(dbjsonObject.getValue(), DBJSONObject.class);
									for (int m = 0; m < list_detailm.get(0).getList().size(); m++) {

										list_details.get(0).getList().add(list_detailm.get(0).getList().get(m));
									}
								}

								if (CollectionUtils.isNotEmpty(list_details)) {
									isDetail = true;
								}
							} else {
								list_detailz.add(dbjsonObject);
							}
						}
						int forNum = 1;
						// 如果有多条明细记录
						HSSFCellStyle cellStyle = workbook.createCellStyle();

						if (isDetail && list_details.size() > 1) {
							forNum = list_details.size();
							// 设置背景色
							cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
							cellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
						} else {
							cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
							cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
						}
						cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色
						cellStyle.setBorderLeft((short) 1); // 边框的大小
						cellStyle.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色
						cellStyle.setBorderRight((short) 1);
						cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
						cellStyle.setBorderBottom((short) 1);
						cellStyle.setWrapText(true);// 自动换行
						cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
						// 循环创建多行数据 设置背景色区分
						for (int j = 0; j < forNum; j++) {
							// createExcel(sheet,rowCount);
							HSSFRow row = sheet.createRow((int) rowCount);// 创建一行
							rowCount++;
							// 审批编号
							HSSFCell cell = row.createCell(0);
							String value = data.getFlowId();
							cell.setCellValue(value);
							cell.setCellStyle(cellStyle);
							sheet.setColumnWidth(0, (short) (value + "  ").getBytes().length * 256);
							// 标题
							User user = userMapper.selectByPrimaryKey(data.getUserId());
							HSSFCell cell1 = row.createCell(1);
							String value1 = user.getUserName() + "的" + approvalType.getName();
							cell1.setCellValue(value1);
							cell1.setCellStyle(cellStyle);
							sheet.setColumnWidth(1, (short) value1.getBytes().length * 256);
							// 审批状态
							HSSFCell cell2 = row.createCell(2);
							String value2 = status2String(data.getStatus(), false);
							cell2.setCellValue(value2);
							cell2.setCellStyle(cellStyle);
							sheet.setColumnWidth(2, (short) value2.getBytes().length * 256);
							// 审批结果
							HSSFCell cell3 = row.createCell(3);
							String value3 = status2String(data.getStatus(), true);
							cell3.setCellValue(value3);
							cell3.setCellStyle(cellStyle);
							sheet.setColumnWidth(3, (short) value3.getBytes().length * 256);
							// 审批发起时间
							HSSFCell cell4 = row.createCell(4);
							String value4 = data.getDraftDate();
							cell4.setCellValue(value4);
							cell4.setCellStyle(cellStyle);
							sheet.setColumnWidth(4, (short) value4.getBytes().length * 256);
							// 审批完成时间
							HSSFCell cell5 = row.createCell(5);
							String value5 = "";
							cell5.setCellValue(value5);
							cell5.setCellStyle(cellStyle);
							sheet.setColumnWidth(5, (short) value5.getBytes().length * 256);
							// 发起人手机号
							HSSFCell cell6 = row.createCell(6);
							String value6 = user.getMobile() + " ";
							cell6.setCellValue(value6);
							cell6.setCellStyle(cellStyle);
							sheet.setColumnWidth(6, (short) value6.getBytes().length * 256);
							// 发起人姓名
							HSSFCell cell7 = row.createCell(7);
							String value7 = "  "
									+ ((user != null && user.getUserName() != null) ? user.getUserName() : "") + "  ";
							cell7.setCellValue(value7);
							cell7.setCellStyle(cellStyle);
							sheet.setColumnWidth(7, (short) value7.getBytes().length * 256);
							// 发起人部门
							Organization org = organizationMapper.selectByPrimaryKey(user.getOrgId());
							HSSFCell cell8 = row.createCell(8);
							String value8 = "  " + ((org != null && org.getOrgName() != null) ? org.getOrgName() : "")
									+ "  ";
							cell8.setCellValue(value8);
							cell8.setCellStyle(cellStyle);
							sheet.setColumnWidth(8, (short) value8.getBytes().length * 256);
							// 历史审批人 (查找流程扭转记录)
							List<ApprovalRunManage> manages = approvalRunManageMapper
									.selectByApprovalId(data.getFlowId());
							// 审批人
							String appPersons = "";
							// 审批记录
							String appRecords = "";
							// 当前审批人
							String curPerson = "";
							for (ApprovalRunManage manage : manages) {
								if (StringUtils.isNotEmpty(manage.getUserId())
										&& StringUtils.isNotEmpty(manage.getExamineDate())) {
									User u = userMapper.selectByPrimaryKey(manage.getUserId());
									appPersons += u.getUserName() + "，";
									appRecords += u.getUserName() + " | " + manage.getExamineDate() + " | "
											+ manage.getOpinion() + "; \n";
								}
								if (StringUtils.isNotEmpty(manage.getRunStatus())
										&& manage.getRunStatus().equals("1")) {
									User u = userMapper.selectByPrimaryKey(data.getUserId());
									curPerson = u.getUserName();
								}

							}
							appPersons = "".equals(appPersons) ? ""
									: appPersons.substring(0, appPersons.lastIndexOf("，"));
							appRecords = "".equals(appRecords) ? ""
									: appRecords.substring(0, appRecords.lastIndexOf("\n"));

							HSSFCell cell9 = row.createCell(9);
							String value9 = appPersons;
							cell9.setCellValue(value9);
							cell9.setCellStyle(cellStyle);
							sheet.setColumnWidth(9, 6000);

							HSSFCell cell10 = row.createCell(10);
							String value10 = appRecords;
							cell10.setCellValue(value10);
							cell10.setCellStyle(cellStyle);
							sheet.setColumnWidth(10, 13000);
							// 当前处理人 11
							HSSFCell cell11 = row.createCell(11);
							String value11 = curPerson;
							cell11.setCellValue(value11);
							cell11.setCellStyle(cellStyle);
							sheet.setColumnWidth(11, 3000);

							// 审批耗时 12
							// if (data.getStatus().equals("6") ||
							// data.getStatus().equals("8")) {
							String time = approvalRunManageMapper.dateDiff(data.getFlowId());
							HSSFCell cell12 = row.createCell(12);
							String value12 = time;
							cell12.setCellValue(value12);
							cell12.setCellStyle(cellStyle);
							sheet.setColumnWidth(12, (short) time.getBytes().length * 256);
							// }

							// // 明细
							// HSSFCell cell13 = row.createCell(13);
							// String value13 = user.getUserName() + "的" +
							// approvalType.getName() + "明细" + (j + 1);
							// cell13.setCellValue(value13);
							// cell13.setCellStyle(cellStyle);
							// sheet.setColumnWidth(13, (short)
							// value13.getBytes().length * 256);

							int dataCell = 13;
							// 如果有明细数据
							if (isDetail) {
								for (int k = 0; k < details.size(); k++) {
									ApprovalTableConfigDetails configDetails = details.get(k);
									if ("TextNote".equals(configDetails.getControlId())
											|| "PictureNote".equals(configDetails.getControlId())
											|| "ValidField".equals(configDetails.getControlId())) {// 如果是说明控件就跳过
										continue;
									}
									if (configDetails.getControlId().equals("TableField")) {
										String value13 = user.getUserName() + "的" + approvalType.getName() + "明细"
												+ (j + 1);
										HSSFCell dataCellValue = row.createCell(dataCell);
										dataCellValue.setCellValue(value13);
										dataCellValue.setCellStyle(cellStyle);
										// sheet.setColumnWidth(dataCell,(short)cellObject.getValue().getBytes().length
										// * 256);
										dataCell++;
									}
									// 每次取出一条明细
									DBJSONObject condition = list_details.get(j);
									HSSFCell dataCellValue = row.createCell(dataCell);
									List<String> cellObjects = condition.getList();
									String string2 = "";
									for (String cellString : cellObjects) {
										DBJSONObject cellObject = JSONObject.parseObject(cellString,
												DBJSONObject.class);
										if (cellObject.getId().equals(configDetails.getReName())) {
											if ("DDPhotoField".equals(cellObject.getControlName())
													|| "DDAttachment".equals(cellObject.getControlName())) {// 判断是否是图片控件
												String url1 = request.getRequestURL().toString();// 获取地址栏中的地址
												url1 = url1.replace("http://", "");
												String str[] = url1.split("/");
												if (!StringUtils.isEmpty(cellObject.getValue())) {
													List<PictureVo> pictureVo_list = JSONObject
															.parseArray(cellObject.getValue(), PictureVo.class);
													for (PictureVo pv : pictureVo_list) {
														String cellStrings = "http://" + str[0] + "/" + str[1] + "/"
																+ "file/download/" + pv.getId() + "/" + pv.getName()
																+ ".do" + "\n";
														string2 = string2 + cellStrings;
														dataCellValue.setCellValue(string2);
													}
												}

												dataCellValue.setCellStyle(cellStyle);
												sheet.setColumnWidth(dataCell, 13000);
												dataCell++;

											} else {

												if ("LinkageSelectField".equals(cellObject.getControlName())) {
													String str = cellObject.getValue();
													String strNew = str.replace("[", "");
													String string = strNew.replace("]", "");
													String[] strings = string.split(",");
													for (String str1 : strings) {
														HSSFCell cells = row.createCell(dataCell);
														str1 = str1.replace("\"", "");
														cells.setCellValue(str1);
														cells.setCellStyle(cellStyle);
														dataCell++;
													}

												} else {
													dataCellValue.setCellValue(cellObject.getValue());
													dataCellValue.setCellStyle(cellStyle);
													// sheet.setColumnWidth(dataCell,(short)cellObject.getValue().getBytes().length
													// * 256);
													dataCell++;
												}

											}

										}
									}
									// 画非明细列的数据 循环list
									HSSFCell dataCellValues = row.createCell(dataCell);
									String string = "";
									for (DBJSONObject cellStringm : list_detailz) {
										if (cellStringm.getId().equals(configDetails.getReName())) {
											if ("DDPhotoField".equals(cellStringm.getControlName())
													|| "DDAttachment".equals(cellStringm.getControlName())) {// 判断是否是图片控件
												String url1 = request.getRequestURL().toString();// 获取地址栏中的地址
												url1 = url1.replace("http://", "");
												String str[] = url1.split("/");
												if (!StringUtils.isEmpty(cellStringm.getValue())) {
													List<PictureVo> pictureVo_list = JSONObject
															.parseArray(cellStringm.getValue(), PictureVo.class);
													for (PictureVo pv : pictureVo_list) {
														String cellStrings = "http://" + str[0] + "/" + str[1] + "/"
																+ "file/download/" + pv.getId() + "/" + pv.getName()
																+ ".do" + "\n";
														string = string + cellStrings;
														dataCellValues.setCellValue(string);
													}
												}

												dataCellValues.setCellStyle(cellStyle);
												sheet.setColumnWidth(dataCell, 13000);
												dataCell++;
											} else {
												if ("LinkageSelectField".equals(cellStringm.getControlName())) {
													String str = cellStringm.getValue();
													String strNew = str.replace("[", "");
													String stringl = strNew.replace("]", "");
													String[] strings = stringl.split(",");
													for (String str1 : strings) {
														HSSFCell cells = row.createCell(dataCell);
														str1 = str1.replace("\"", "");
														cells.setCellValue(str1);
														cells.setCellStyle(cellStyle);
														dataCell++;
													}

												} else {
													dataCellValues.setCellValue(cellStringm.getValue());
													dataCellValues.setCellStyle(cellStyle);
													dataCell++;
												}

											}

										}
									}

								}
							} else {
								String string = "";
								for (int k = 0; k < details.size(); k++) {
									ApprovalTableConfigDetails configDetails = details.get(k);
									if ("TextNote".equals(configDetails.getControlId())
											|| "PictureNote".equals(configDetails.getControlId())
											|| "ValidField".equals(configDetails.getControlId())) {// 如果是说明控件就跳过-renlinggao
										continue;
									}
									for (int c = 0; c < list.size(); c++) {
										DBJSONObject condition = list.get(c);

										if (condition.getId().equals(configDetails.getReName())) {
											HSSFCell dataCellValue = row.createCell(dataCell);
											// 判断是否为图片控件
											if ("DDPhotoField".equals(condition.getControlName())
													|| "DDAttachment".equals(condition.getControlName())) {// 判断是否是图片控件
												String url1 = request.getRequestURL().toString();// 获取地址栏中的地址
												url1 = url1.replace("http://", "");
												String str[] = url1.split("/");
												if (!StringUtils.isEmpty(condition.getValue())) {
													List<PictureVo> pictureVo_list = JSONObject
															.parseArray(condition.getValue(), PictureVo.class);
													for (PictureVo pv : pictureVo_list) {
														// 拼接地址
														String cellStrings = "http://" + str[0] + "/" + str[1] + "/"
																+ "file/download/" + pv.getId() + "/" + pv.getName()
																+ ".do" + "\n";
														string = string + cellStrings;
														dataCellValue.setCellValue(string);
													}
												}

												dataCellValue.setCellStyle(cellStyle);
												sheet.setColumnWidth(dataCell, 13000);
												dataCell++;

											} else {
												if ("LinkageSelectField".equals(condition.getControlName())) {
													String str = condition.getValue();
													String strNew = str.replace("[", "");
													String stringl = strNew.replace("]", "");
													String[] strings = stringl.split(",");
													for (String str1 : strings) {
														HSSFCell cells = row.createCell(dataCell);
														str1 = str1.replace("\"", "");
														cells.setCellValue(str1);
														cells.setCellStyle(cellStyle);
														dataCell++;
													}

												} else {
													dataCellValue.setCellValue(condition.getValue());
													dataCellValue.setCellStyle(cellStyle);
													dataCell++;
												}

											}

										}
									}
								}
							}
						}
					}
				}
			}

			OutputStream out = null;
			try {
				out = response.getOutputStream();
				workbook.write(out);
			} catch (Exception e) {
				// TODO: handle exception
				try {
					workbook.close();
					out.flush();
					out.close();
				} catch (IOException e1) {
					// TODO Auto-generated atch block
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 流程状态字符转换
	 * 
	 * @param status
	 * @return
	 */
	public static String status2String(String status, boolean isResult) {
		if (StringUtils.isNoneEmpty(status)) {
			int s = Integer.parseInt(status);
			if (s == 0)
				return isResult == true ? "" : "起草";
			else if (s == 1)
				return isResult == true ? "" : "正在处理";
			else if (s == 6)
				return isResult == true ? "通过" : "审批完成";
			else if (s == 8)
				return isResult == true ? "不通过" : "审批驳回";
			else if (s == 9)
				return isResult == true ? "" : "申请人撤销";

		}
		return "状态不明";
	}

	/**
	 * 参数封装
	 * 
	 * @param export
	 * @return
	 */
	public HashMap<String, Object> setParams(ExportVO export) {
		// 封装参数
		HashMap<String, Object> params = new HashMap<String, Object>();
		// 标题关键字
		if (StringUtils.isNotEmpty(export.getTitle())) {
			params.put("title", export.getTitle());
		}
		// 状态
		if (StringUtils.isNotEmpty(export.getStatus())) {
			params.put("status", export.getStatus());
		}
		// 审批编号
		if (StringUtils.isNotEmpty(export.getFlowId())) {
			params.put("flowId", export.getFlowId());
		}
		// 申请开始时间
		if (StringUtils.isNotEmpty(export.getApplyStartTime())) {
			params.put("applyStartTime", export.getApplyStartTime());
		}
		// 申请结束时间
		if (StringUtils.isNotEmpty(export.getApplyEndTime())) {
			params.put("applyEndTime", export.getApplyEndTime());
		}
		// 完成开始时间
		if (StringUtils.isNotEmpty(export.getApproveStartTime())) {
			params.put("approveStartTime", export.getApproveStartTime());
		}
		// 完成结束时间
		if (StringUtils.isNotEmpty(export.getApproveEndTime())) {
			params.put("approveEndTime", export.getApproveEndTime());
		}
		return params;
	}

}
