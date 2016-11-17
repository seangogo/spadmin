package cmcc.mobile.admin.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.ActivationDataFlavor;
import javax.servlet.http.HttpServletRequest;
import javax.swing.JScrollBar;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import ch.qos.logback.core.db.dialect.HSQLDBDialect;
import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.entity.ApprovalTableConfig;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.AssignPermissions;
import cmcc.mobile.admin.entity.VerifyThirdCompany;
import cmcc.mobile.admin.entity.VerifyThirdInfo;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.IApproveDataService;
import cmcc.mobile.admin.service.PublicExportService;
import cmcc.mobile.admin.service.impl.PublicExportServiceImpl;
import cmcc.mobile.admin.util.DBJSONObject;
import cmcc.mobile.admin.util.MD5Util;
import cmcc.mobile.admin.vo.PictureVo;

@Controller
@RequestMapping("third")
public class PublicExportController extends BaseController {

	@Autowired
	private PublicExportService publicExportService;

	@Autowired
	private IApproveDataService approvalDataService;

	/**
	 * 验证公司是否存在
	 * 
	 * @param thirdCompanyId
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/createKey")
	public JsonResult createKey(String thirdCompanyId) {
		// 根据第三方公司Id查询是否存在
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		VerifyThirdCompany verifyThirdCompany = publicExportService.selectByThirdCompanyId(thirdCompanyId);
		// 如果存在
		String ukey = "";
		if (null != verifyThirdCompany) {

			// 发送给第三方的key
			ukey = UUID.randomUUID().toString().replace("-", "");
			String key = ukey + verifyThirdCompany.getNumber() + verifyThirdCompany.getThridCompanyId();
			String unionKey = MD5Util.MD5(key);
			Date createTime = new Date();
			// 将数据放入到实体类中
			VerifyThirdInfo verifyThirdInfo = new VerifyThirdInfo();
			verifyThirdInfo.setCreatetime(createTime);
			verifyThirdInfo.setUnionKey(unionKey);
			verifyThirdInfo.setThirdCompanyId(thirdCompanyId);
			verifyThirdInfo.setuKey(ukey);
			// 插入到verifyThirdInfo表中
			int m = publicExportService.insert(verifyThirdInfo);

			if (m == 1) {
				return new JsonResult(true, "验证成功", ukey);
			}

		}
		return new JsonResult(false, "", "");

	}

	/**
	 * 验证公司是否可导出数据
	 * 
	 * @param thirdCompanyId
	 * @param keys
	 * @param typeId
	 * @param startTime
	 * @param endTime
	 * @return
	 */

	@ResponseBody
	@RequestMapping("/verifyInterface")
	public JsonResult verifyInterface(String thirdCompanyId, String keys, HttpServletRequest request) {
		// 根据第三方公司Id和秘钥来验证
		Date time = new Date(new Date().getTime() - 5 * 60 * 1000);
		VerifyThirdInfo verifyThirdInfo = new VerifyThirdInfo();
		verifyThirdInfo.setThirdCompanyId(thirdCompanyId);
		verifyThirdInfo.setUnionKey(keys);

		// 查询是否存在信息
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		VerifyThirdInfo verifyThirdInfos = publicExportService.selectByThirdCompanyIdAndUnionKey(verifyThirdInfo);

		if (null == verifyThirdInfos || verifyThirdInfos.getCreatetime().before(time)) {
			return new JsonResult(false, "验证失效", "");
		} else {
			request.getSession().setAttribute("thirdCompanyId", thirdCompanyId);
			return new JsonResult(false, "验证成功", "");

		}

	}

	@RequestMapping("/export")
	@ResponseBody
	public JsonResult export(String companyId, String processdureId, String startTime, String endTime,
			HttpServletRequest request) {

		String thirdCompanyId = (String) request.getSession().getAttribute("thirdCompanyId");
		// 如果有存在的话导出数据
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("thirdCompanyId", thirdCompanyId);
		maps.put("processdureId", processdureId);
		maps.put("companyId", companyId);
		MultipleDataSource.setDataSourceKey(getCompany().getId());
		List<AssignPermissions> assignPermissions = publicExportService.selectAssignPerssiByThirdCompnay(maps);
		if (null == assignPermissions || assignPermissions.size() == 0) {
			return new JsonResult(false, "没有权限", "");
		}

		Map<String, String> typeInfo_map = new HashMap<String, String>();
		 typeInfo_map.put("id", processdureId);
		 typeInfo_map.put("companyId", companyId);
		//测试数据
		//typeInfo_map.put("companyId", "testenterprise");
		//typeInfo_map.put("id", "eda2d784-3fbf-454c-bc62-e310feffb597");

		// 获取流程
		MultipleDataSource.setDataSourceKey(getCompany().getId());
		ApprovalType approvalType = publicExportService.selectByCompanyIdAndTypeId(typeInfo_map);
		List<ApprovalTableConfig> approvalTableConfigs = publicExportService
				.selectByApprovalTypeId(approvalType.getId());

		//
		List<Object> list_final = new ArrayList<Object>();
		for (ApprovalTableConfig atc : approvalTableConfigs) {

			// 根据tableid获取tableDeatail数据
			List<ApprovalTableConfigDetails> detaila = publicExportService.getApprovalInfoById1(atc.getId());
			//
			List<ApprovalTableConfigDetails> details = new ArrayList<ApprovalTableConfigDetails>();
			// 过滤掉说明等控件
			for (ApprovalTableConfigDetails detailm : detaila) {

				if ("TextNote".equals(detailm.getControlId()) || "PictureNote".equals(detailm.getControlId())
						|| "ValidField".equals(detailm.getControlId())) {// 如果是说明控件就跳过
					continue;
				}
				details.add(detailm);

			}

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("configId", atc.getId());
			params.put("applyStartTime", startTime);
			params.put("applyEndTime", endTime);
			List<ApprovalData> datas = approvalDataService.selectByParams(params);

			// 封装第二层
			List<List<Map<String, String>>> list_second = new ArrayList<List<Map<String, String>>>();
			for (ApprovalData data : datas) {
				List<Map<String, String>> list_table = new ArrayList<Map<String, String>>();
				DBJSONObject object = JSONObject.parseObject(data.getJsonData(), DBJSONObject.class);
				List<DBJSONObject> list = JSONObject.parseArray(object.getData(), DBJSONObject.class);
				// 明细数据
				List<DBJSONObject> list_details = new ArrayList<DBJSONObject>();
				List<DBJSONObject> list_detailm = new ArrayList<DBJSONObject>();
				// 非明细数据
				List<DBJSONObject> list_detailz = new ArrayList<DBJSONObject>();
				Boolean isDetail = false;
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

				if (isDetail) {

					int j = 0;
					for (int k = 0; k < details.size(); k++) {
						ApprovalTableConfigDetails configDetails = details.get(k);
						if ("TextNote".equals(configDetails.getControlId())
								|| "PictureNote".equals(configDetails.getControlId())
								|| "ValidField".equals(configDetails.getControlId())) {// 如果是说明控件就跳过
							continue;
						}
						// if
						// (configDetails.getControlId().equals("TableField"))
						// {}
						// 每次取出一条明细
						DBJSONObject condition = list_details.get(j);
						List<String> cellObjects = condition.getList();
						String string2 = "";

						for (String cellString : cellObjects) {
							DBJSONObject cellObject = JSONObject.parseObject(cellString, DBJSONObject.class);
							if (cellObject.getId().equals(configDetails.getReName())) {
								if ("DDPhotoField".equals(cellObject.getControlName())
										|| "DDAttachment".equals(cellObject.getControlName())) {// 判断是否是图片控件
									String url1 = request.getRequestURL().toString();// 获取地址栏中的地址
									url1 = url1.replace("http://", "");
									String str[] = url1.split("/");
									if (!StringUtils.isEmpty(cellObject.getValue())) {
										List<PictureVo> pictureVo_list = JSONObject.parseArray(cellObject.getValue(),
												PictureVo.class);
										for (PictureVo pv : pictureVo_list) {
											String cellStrings = "http://" + str[0] + "/" + str[1] + "/"
													+ "file/download/" + pv.getId() + "/" + pv.getName() + ".do" + "\n";
											string2 = string2 + cellStrings;

										}

									}
									Map<String, String> map = new HashMap<String, String>();
									map.put(cellObject.getId(), string2);
									list_table.add(map);

								} else {

									if ("LinkageSelectField".equals(cellObject.getControlName())) {
										String str = cellObject.getValue();
										String strNew = str.replace("[", "");
										String string = strNew.replace("]", "");
										String[] strings = string.split(",");
										for (String str1 : strings) {
											Map<String, String> map = new HashMap<String, String>();
											str1 = str1.replace("\"", "");
											map.put(cellObject.getId(), str1);
											list_table.add(map);
										}

									} else {
										Map<String, String> map = new HashMap<String, String>();
										map.put(cellObject.getControlName(), cellObject.getValue());
										list_table.add(map);

									}

								}

							}
						}
						// 画非明细列的数据 循环list

						String string = "";
						for (DBJSONObject cellStringm : list_detailz) {
							if (cellStringm.getId().equals(configDetails.getReName())) {
								if ("DDPhotoField".equals(cellStringm.getControlName())
										|| "DDAttachment".equals(cellStringm.getControlName())) {// 判断是否是图片控件
									String url1 = request.getRequestURL().toString();// 获取地址栏中的地址
									url1 = url1.replace("http://", "");
									String str[] = url1.split("/");
									Map<String, String> map = new HashMap<String, String>();
									if (!StringUtils.isEmpty(cellStringm.getValue())) {
										List<PictureVo> pictureVo_list = JSONObject.parseArray(cellStringm.getValue(),
												PictureVo.class);
										for (PictureVo pv : pictureVo_list) {
											String cellStrings = "http://" + str[0] + "/" + str[1] + "/"
													+ "file/download/" + pv.getId() + "/" + pv.getName() + ".do" + "\n";
											string = string + cellStrings;
										}

										map.put(cellStringm.getId(), string);
										list_table.add(map);
									} else {
										map.put(cellStringm.getId(), "");
										list_table.add(map);
									}

								} else {
									String strs = "";
									if ("LinkageSelectField".equals(cellStringm.getControlName())) {
										String str = cellStringm.getValue();
										String strNew = str.replace("[", "");
										String stringl = strNew.replace("]", "");
										String[] strings = stringl.split(",");
										for (String str1 : strings) {
											strs = str1.replace("\"", "");

										}
										Map<String, String> map = new HashMap<String, String>();
										map.put(cellStringm.getId(), strs);
										list_table.add(map);

									} else {
										Map<String, String> map = new HashMap<String, String>();
										map.put(cellStringm.getId(), cellStringm.getValue());
										list_table.add(map);

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
								// 判断是否为图片控件
								if ("DDPhotoField".equals(condition.getControlName())
										|| "DDAttachment".equals(condition.getControlName())) {// 判断是否是图片控件
									String url1 = request.getRequestURL().toString();// 获取地址栏中的地址
									url1 = url1.replace("http://", "");
									String str[] = url1.split("/");
									Map<String, String> map = new HashMap<String, String>();
									if (!StringUtils.isEmpty(condition.getValue())) {
										List<PictureVo> pictureVo_list = JSONObject.parseArray(condition.getValue(),
												PictureVo.class);

										for (PictureVo pv : pictureVo_list) {
											// 拼接地址
											String cellStrings = "http://" + str[0] + "/" + str[1] + "/"
													+ "file/download/" + pv.getId() + "/" + pv.getName() + ".do" + "\n";
											string = string + cellStrings;
										}

										map.put(condition.getId(), string);
										list_table.add(map);
									} else {
										map.put(condition.getId(), string);
										list_table.add(map);
									}

								} else {
									if ("LinkageSelectField".equals(condition.getControlName())) {
										String str = condition.getValue();
										String strNew = str.replace("[", "");
										String stringl = strNew.replace("]", "");
										String[] strings = stringl.split(",");
										for (String str1 : strings) {
											str1 = str1.replace("\"", "");
											Map<String, String> map = new HashMap<String, String>();
											map.put(condition.getId(), str1);
											list_table.add(map);
										}

									} else {
										Map<String, String> map = new HashMap<String, String>();
										map.put(condition.getId(), condition.getValue());
										list_table.add(map);

									}

								}

							}
						}
					}

				}

				// 封装最新的数据
				List<Map<String, String>> list2 = new ArrayList<Map<String, String>>();
				// 遍历tableDeatil

				for (Map<String, String> map : list_table) {// 遍历解析出来的单条数据明细
					for (ApprovalTableConfigDetails atcd : details) {
						Map<String, String> new_map = new HashMap<String, String>();
						String value = "";
						String key = "";
						for (String keyz : map.keySet()) {
							key = keyz;
							value = map.get(keyz);
						}

						if (atcd.getReName().equals(key)) {
							new_map.put(atcd.getDescribeName(), value);
							list2.add(new_map);
						}

					}
				}
				list_second.add(list2);

			}
			list_final.add(list_second);
		}

		return new JsonResult(true, "", list_final);
	}

}
