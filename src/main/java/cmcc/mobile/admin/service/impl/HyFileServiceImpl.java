package cmcc.mobile.admin.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.ApprovalBatchTaskMapper;
import cmcc.mobile.admin.dao.ApprovalTableConfigDetailsMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.UserApprovalDefDataMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.ApprovalBatchTask;
import cmcc.mobile.admin.entity.ApprovalTableConfigDetails;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.UserApprovalDefData;
import cmcc.mobile.admin.service.ApprovalBatchTaskService;
import cmcc.mobile.admin.service.HyFileService;
import cmcc.mobile.admin.util.PropertiesUtil;

@Service("hyfileService")
public class HyFileServiceImpl implements HyFileService {

	@Autowired
	private UserApprovalDefDataMapper hyuserapprovaldefadataMapper;

	@Autowired
	private ApprovalTableConfigDetailsMapper approvalTableConfigDetailsMapper;

	@Autowired
	private ApprovalTypeMapper approvalTypeMapper;

	@Autowired
	private ApprovalBatchTaskMapper approvalBatchTaskMapper;

	@Autowired
	private UserMapper userMapper;

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public JsonResult importData(Long taskId, String id, HttpServletRequest request, JsonResult result) {
		String companyId = ((Customer) request.getSession().getAttribute("company")).getId();

		if (StringUtils.isEmpty(companyId)) {
			result.setSuccess(false);
			result.setMessage("session失效 请重新登录");
		}

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

				file = new File(filePath, companyId + "." + extension);

				if (file.exists()) {
					file.delete();
				}
				HSSFWorkbook hbook = null;
				XSSFWorkbook xbook = null;
				FileInputStream fileIn = null;
				try {
					if (extension.equals("xls")) {
						multipartFile.transferTo(file);
						fileIn = new FileInputStream(file);
						hbook = new HSSFWorkbook(fileIn);
						mess = hssfSave(taskId, id, hbook, null, null, companyId);
					} else if (extension.equals("xlsx")) {
						multipartFile.transferTo(file);
						xbook = new XSSFWorkbook(new FileInputStream(file));
						mess = hssfSave(taskId, id, xbook, null, null, companyId);
					} else {
						result.setSuccess(false);
						result.setMessage("只能上传xls和xlsx文件");
						return result;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					result.setSuccess(false);
					result.setMessage("文件数据错误");
					return result;
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
		result.setSuccess(true);
		result.setMessage(mess);
		return result;

	}

	/**
	 * 保存方法xls格式
	 * 
	 * @param book
	 * @param dbName
	 * @param mainDbName
	 * @param companyId
	 * @return
	 */
	private String hssfSave(Long taskId, String id, HSSFWorkbook book, String dbName, String mainDbName,
			String companyId) {

		HSSFSheet sheet = book.getSheetAt(0);// 获取第一个工作簿
		HSSFRow titleRow = sheet.getRow(0);// 获取第一行标题行
		int row = sheet.getPhysicalNumberOfRows();// 行数
		int column = titleRow.getPhysicalNumberOfCells();// 列数

		String mess = "";
		// int okNum = 0;
		String mobile = "";

		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(id);
		List<ApprovalTableConfigDetails> controlls = approvalTableConfigDetailsMapper
				.getApprovalInfoById(approvalType.getApprovalTableConfigId());

		// 导入的手机号
		List<String> mobiles = new ArrayList<String>();

		String[] unExportController = new String[] { "DDPhotoField", "DDAttachment", "TableField", "DDDateField",
				"DDDateRangeField", "ValidField", "PictureNote" };
		List<String> tempList = Arrays.asList(unExportController);
		List<ApprovalTableConfigDetails> controllerList = new ArrayList<ApprovalTableConfigDetails>();
		for (ApprovalTableConfigDetails d : controlls) {
			if (tempList.contains(d.getControlId()) || StringUtils.isNotEmpty(d.getPreviousId())) {
				continue;
			}
			controllerList.add(d);
		}
		controlls = controllerList;

		List<UserApprovalDefData> insertData = new ArrayList<UserApprovalDefData>();// 需要批量插入的数据集合
		for (int i = 1; i < row; i++) {
			HSSFRow cellRow = sheet.getRow(i);

			UserApprovalDefData had = new UserApprovalDefData();
			had.setApprovalTableConfigId(approvalType.getApprovalTableConfigId());
			had.setCompanyId(companyId);
			Date now = new Date();
			had.setCreateTime(now);// 创建日期
			had.setStatus("1");
			for (int j = 0; j < column; j++) {
				if ("手机号".equals(titleRow.getCell(j).getStringCellValue())) {
					mobile = (String) getCellContent(cellRow.getCell(j));
					// 判断手机号是否在这个公司存在
					HashMap<String, String> params = new HashMap<>();
					params.put("companyId", companyId);
					params.put("mobile", mobile);
					if (mobiles.contains(mobile) || userMapper.selectByMobile(params) == null)
						continue;
					had.setMobile(mobile);
					mobiles.add(mobile);
				} else if ("姓名".equals(titleRow.getCell(j).getStringCellValue())) {

				} else {
					String data = (String) getCellContent(cellRow.getCell(j));
					String cId = controlls.get(j - 2).getControlId();
					had.setControlId(controlls.get(j - 2).getReName());
					had.setTaskId(taskId);
					if (StringUtils.isNotEmpty(data)
							&& ("DDSelectField".equals(cId) || "DDMultiSelectField".equals(cId))) {
						had.setJsonData(splicingDate(cId, data));
						if (had.getMobile() != null) {
							UserApprovalDefData tem1 = new UserApprovalDefData();
							BeanUtils.copyProperties(had, tem1);
							insertData.add(tem1);
						}

						// hyuserapprovaldefadataMapper.insertSelective(had);
					} else if (StringUtils.isNotEmpty(data) && "LinkageSelectField".equals(cId)) {
						had.setJsonData(splicingDate(cId, data));
						if (had.getMobile() != null) {
							UserApprovalDefData tem1 = new UserApprovalDefData();
							BeanUtils.copyProperties(had, tem1);
							insertData.add(tem1);
						}
						// hyuserapprovaldefadataMapper.insertSelective(had);
					} else if (StringUtils.isNotEmpty(data)) {
						had.setJsonData(data);
						if (had.getMobile() != null) {
							UserApprovalDefData tem1 = new UserApprovalDefData();
							BeanUtils.copyProperties(had, tem1);
							insertData.add(tem1);
						}
						// hyuserapprovaldefadataMapper.insertSelective(had);
					}
				}
			}
		}
		// 把已经存在的状态改为删除
		if (mobiles.size() > 0 && !insertData.isEmpty()) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("taskId", taskId);
			params.put("status", "0");
			params.put("list", mobiles);
			params.put("updateTime", new Date());
			hyuserapprovaldefadataMapper.updateByTaskIdAndMobileSelective(params);
			// 批量插入
			hyuserapprovaldefadataMapper.insertBatch(insertData);
		}

		// 查询任务初始数据的人数
		UserApprovalDefData record = new UserApprovalDefData();
		record.setTaskId(taskId);
		int dataUserNum = hyuserapprovaldefadataMapper.findTaskDataCount(record);
		// 更新任务的初始化数据人数
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setInitdtUsers(dataUserNum);
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);
		return mess;
	}

	/**
	 * 重载方法xlsx格式
	 * 
	 * @param book
	 * @param dbName
	 * @param mainDbName
	 * @param companyId
	 * @return
	 */

	private String hssfSave(Long taskId, String id, XSSFWorkbook book, String dbName, String mainDbName,
			String companyId) {

		XSSFSheet sheet = book.getSheetAt(0);// 获取第一个工作簿
		XSSFRow titleRow = sheet.getRow(0);// 获取第一行标题行
		int row = sheet.getPhysicalNumberOfRows();// 行数
		int column = titleRow.getPhysicalNumberOfCells();// 列数

		String mess = "";
		// int okNum = 0;
		String mobile = "";

		ApprovalType approvalType = approvalTypeMapper.selectByPrimaryKey(id);
		List<ApprovalTableConfigDetails> controlls = approvalTableConfigDetailsMapper
				.getApprovalInfoById(approvalType.getApprovalTableConfigId());

		// // 更新原来导入的数据为删除状态
		// Map<String, String> params = new HashMap<String, String>();
		// params.put("approvalTableConfigId",
		// approvalType.getApprovalTableConfigId());
		// params.put("companyId", companyId);
		// params.put("status", "9");
		// hyuserapprovaldefadataMapper.updateStatusByMobile(params);

		// 导入的手机号
		List<String> mobiles = new ArrayList<String>();

		String[] unExportController = new String[] { "DDPhotoField", "DDAttachment", "TableField", "DDDateField",
				"DDDateRangeField", "ValidField", "PictureNote" };
		List<String> tempList = Arrays.asList(unExportController);
		List<ApprovalTableConfigDetails> controllerList = new ArrayList<ApprovalTableConfigDetails>();
		for (ApprovalTableConfigDetails d : controlls) {
			if (tempList.contains(d.getControlId()) || StringUtils.isNotEmpty(d.getPreviousId())) {
				continue;
			}
			controllerList.add(d);
		}
		controlls = controllerList;

		List<UserApprovalDefData> insertData = new ArrayList<UserApprovalDefData>();// 需要批量插入的数据集合
		for (int i = 1; i < row; i++) {
			XSSFRow cellRow = sheet.getRow(i);

			UserApprovalDefData had = new UserApprovalDefData();
			had.setApprovalTableConfigId(approvalType.getApprovalTableConfigId());
			had.setCompanyId(companyId);
			Date now = new Date();
			had.setCreateTime(now);// 创建日期
			had.setStatus("1");
			for (int j = 0; j < column; j++) {
				if ("手机号".equals(titleRow.getCell(j).getStringCellValue())) {
					mobile = (String) getCellContent(cellRow.getCell(j));
					// 判断手机号是否在这个公司存在
					HashMap<String, String> params = new HashMap<>();
					params.put("companyId", companyId);
					params.put("mobile", mobile);
					if (mobiles.contains(mobile) || userMapper.selectByMobile(params) == null)
						continue;
					had.setMobile(mobile);
					mobiles.add(mobile);
				} else if ("姓名".equals(titleRow.getCell(j).getStringCellValue())) {

				} else {
					String data = (String) getCellContent(cellRow.getCell(j));
					String cId = controlls.get(j - 2).getControlId();
					had.setControlId(controlls.get(j - 2).getReName());
					had.setTaskId(taskId);
					if (StringUtils.isNotEmpty(data)
							&& ("DDSelectField".equals(cId) || "DDMultiSelectField".equals(cId))) {
						had.setJsonData(splicingDate(cId, data));
						if (had.getMobile() != null)
							insertData.add(had);
						// hyuserapprovaldefadataMapper.insertSelective(had);
					} else if (StringUtils.isNotEmpty(data) && "LinkageSelectField".equals(cId)) {
						had.setJsonData(splicingDate(cId, data));
						if (had.getMobile() != null)
							insertData.add(had);
						// hyuserapprovaldefadataMapper.insertSelective(had);
					} else if (StringUtils.isNotEmpty(data)) {
						had.setJsonData(data);
						if (had.getMobile() != null)
							insertData.add(had);
						// hyuserapprovaldefadataMapper.insertSelective(had);
					}
				}
			}
		}
		// 把已经存在的状态改为删除
		if (mobiles.size() > 0) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("taskId", taskId);
			params.put("status", "0");
			params.put("list", mobiles);
			hyuserapprovaldefadataMapper.updateByTaskIdAndMobileSelective(params);
			// 批量插入
			hyuserapprovaldefadataMapper.insertBatch(insertData);
		}

		// 查询任务初始数据的人数
		UserApprovalDefData record = new UserApprovalDefData();
		record.setTaskId(taskId);
		int dataUserNum = hyuserapprovaldefadataMapper.findTaskDataCount(record);
		// 更新任务的初始化数据人数
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setInitdtUsers(dataUserNum);
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);
		return mess;
	}

	/**
	 * 被调用的方法
	 * 
	 * @param cell
	 * @return
	 */
	private Object getCellContent(HSSFCell cell) {
		if (cell == null) {
			return null;
		} else if (HSSFCell.CELL_TYPE_NUMERIC == cell.getCellType()) {
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			return cell.getStringCellValue();
		} else {
			return cell.getStringCellValue();
		}
	}

	/**
	 * 重载方法
	 * 
	 * @param cell
	 * @return
	 */

	private Object getCellContent(XSSFCell cell) {
		if (cell == null) {
			return null;
		} else if (XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType()) {
			cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			return cell.getStringCellValue();
		} else {
			return cell.getStringCellValue();
		}
	}

	/**
	 * 拼接数据
	 * 
	 * @return
	 */
	private String splicingDate(String controlId, String data) {
		// 获取数据切成数组
		String[] datas = null;
		if (!"TextNote".equals(controlId)) {
			datas = data.split("\\\\");
		}

		List<String> data_list = new ArrayList<String>();// chilidren集合
		if ("DDSelectField".equals(controlId) || "DDMultiSelectField".equals(controlId)) {
			for (String values : datas) {
				// 封装Map成一个List
				data_list.add(values);
			}
			JSONObject result = new JSONObject();
			result.put("options", data_list);
			return result.toString();

		} else if ("TextNote".equals(controlId)) {

			String dataNew = data.trim();
			JSONObject result = new JSONObject();
			result.put("options", dataNew);
			return result.toString();

		} else if ("LinkageSelectField".equals(controlId)) {

			List<Map> select_list = new ArrayList<Map>();
			Map<String, Object> select_maps = new HashMap<String, Object>();
			for (String value : datas) {
				List<Map> children_list = new ArrayList<Map>();
				value = value.replaceAll("\\(", ",");
				value = value.replaceAll("\\)", "");
				String[] childrens = value.split(",");
				// 封装第一层value循环
				for (int i = 1; i < childrens.length; i++) {
					Map<String, Object> children_map = new HashMap<String, Object>();
					children_map.put("value", childrens[i]);
					children_list.add(children_map);
				}
				Map<String, Object> select_map = new HashMap<String, Object>();
				select_map.put("children", children_list);
				select_map.put("value", childrens[0]);
				select_list.add(select_map);
			}
			select_maps.put("selects", select_list);
			select_maps.put("mustnumber", true);
			JSONObject result = new JSONObject();
			result.put("linkageOption", select_maps);
			return result.toString();
		}

		return "";
	}

}
