package cmcc.mobile.admin.service.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.base.VirtualActorConstant;
import cmcc.mobile.admin.dao.ApprovalBatchTaskMapper;
import cmcc.mobile.admin.dao.ApprovalDataMapper;
import cmcc.mobile.admin.dao.ApprovalRunManageMapper;
import cmcc.mobile.admin.dao.ApprovalTypeMapper;
import cmcc.mobile.admin.dao.MsgCompanyMapper;
import cmcc.mobile.admin.dao.MsgSendMapper;
import cmcc.mobile.admin.dao.TemporaryBatchStartMapper;
import cmcc.mobile.admin.dao.ThirdApprovalDealMapper;
import cmcc.mobile.admin.dao.ThirdApprovalStartMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.service.ApprovalService;
import cmcc.mobile.admin.util.DateUtil;
import cmcc.mobile.admin.util.PropertiesUtil;
import cmcc.mobile.admin.entity.ApprovalBatchTask;
import cmcc.mobile.admin.entity.ApprovalData;
import cmcc.mobile.admin.entity.ApprovalRunManage;
import cmcc.mobile.admin.entity.ApprovalType;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.MsgCompany;
import cmcc.mobile.admin.entity.MsgSend;
import cmcc.mobile.admin.entity.TemporaryBatchStart;
import cmcc.mobile.admin.entity.ThirdApprovalDeal;
import cmcc.mobile.admin.entity.ThirdApprovalStart;
import cmcc.mobile.admin.entity.User;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ApprovalServiceImpl implements ApprovalService {

	@Autowired
	private ApprovalTypeMapper approvalTypeMapper;

	@Autowired
	private ApprovalDataMapper approvalDataMapper;

	@Autowired
	private ApprovalRunManageMapper approvalRunManageMapper;

	@Autowired
	private ThirdApprovalDealMapper thirdApprovalDealMapper;

	@Autowired
	private ThirdApprovalStartMapper thirdApprovalStartMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TemporaryBatchStartMapper temporaryBatchStartMapper;

	@Autowired
	private MsgCompanyMapper msgCompanyMapper;

	@Autowired
	private MsgSendMapper msgSendMapper;

	@Autowired
	private ApprovalBatchTaskMapper approvalBatchTaskMapper;

	private static String serviceIp = PropertiesUtil.getServerIp("server");

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * 批量发起流程撤销
	 * 
	 * @param request
	 * @param typeId
	 * @return
	 */
	public JsonResult cancelApprovals(HttpServletRequest request, String typeId, Customer customer) {
		String companyId = customer.getId();

		JsonResult json = new JsonResult();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("companyId", customer.getId());
		map.put("id", typeId);
		ApprovalType approvalType = new ApprovalType();
		approvalType = approvalTypeMapper.getApprovalTypeById2(map);

		if (approvalType == null) {
			json.setSuccess(false);
			json.setMessage("流程不存在");
			return json;
		}
		String confId = approvalType.getApprovalTableConfigId();
		HashMap<String, String> map2 = new HashMap<String, String>();
		map2.put("confId", confId);
		map2.put("companyId", companyId);
		List<ThirdApprovalDeal> list = thirdApprovalDealMapper.getDealByConfig(map2);
		if (list == null || list.size() == 0) {
			json.setSuccess(false);
			json.setMessage("没有可撤销数据");
			return json;
		}

		List<String> thirdId = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			thirdId.add(list.get(i).getId());
		}

		if (thirdApprovalDealMapper.deleteById(thirdId) > 0 && temporaryBatchStartMapper.deleteByThirdId(thirdId) > 0) {
			json.setSuccess(true);
		} else {
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		return json;
		/**
		 * List<ApprovalData> dataList = new ArrayList<ApprovalData>(); dataList
		 * =approvalDataMapper.getAllDataByConfId(confId); List
		 * <String> flowIdList = new ArrayList<String>(); for(int
		 * i=0;i<dataList.size();i++){
		 * flowIdList.add(dataList.get(i).getFlowId()); }
		 * if(flowIdList.size()==0 || flowIdList==null){ json.setSuccess(false);
		 * json.setMessage("没有可撤销数据"); return json; }
		 * 
		 * List<ApprovalRunManage> runList=
		 * approvalRunManageMapper.getNoexecuteApproval(flowIdList);
		 * if(runList.size()==0 || runList==null){ json.setSuccess(false);
		 * json.setMessage("没有可撤销数据"); return json; }
		 * 
		 * List<String> flowIdList2 = new ArrayList<String>(); for(int
		 * i=0;i<runList.size();i++){
		 * flowIdList2.add(runList.get(i).getApprovalDataId()); }
		 * approvalDataMapper.deleteById(flowIdList2);
		 * approvalRunManageMapper.deleteById(flowIdList2);
		 * thirdApprovalDealMapper.deleteById(flowIdList2); int i =
		 * thirdApprovalStartMapper.deleteById(flowIdList2);
		 * 
		 * if(i==dataList.size()){ json.setSuccess(true); }else{
		 * json.setSuccess(false); json.setMessage("服务器异常"); } return json;
		 */
	}

	/**
	 * 通过execl导入人员
	 * 
	 * @param request
	 * @return
	 */

	public JsonResult importUserInfo(HttpServletRequest request, Customer customer, Long taskId, String typeId) {

		String companyId = customer.getId();

		JsonResult json = new JsonResult();
		String filePath = PropertiesUtil.getAppByKey("PERSON_INFO_FILE_PATH");
		MultipartHttpServletRequest mr = (MultipartHttpServletRequest) request;
		File file = null;
		// String mess = "";
		for (Iterator<MultipartFile> ite = mr.getFileMap().values().iterator(); ite.hasNext();) {
			MultipartFile multipartFile = ite.next();
			if (multipartFile.getSize() > 0) {
				// 获取文件名
				String fileName = multipartFile.getOriginalFilename();
				// 获取文件后缀名
				String extension = FilenameUtils.getExtension(fileName);
				InputStream fileIn = null;
				HSSFWorkbook hbook = null;
				XSSFWorkbook xbook = null;
				try {
					// file = new File(fileName);
					/**
					 * if(!file.exists()){ //如果文件夹不存在 创建文件夹
					 * file.createNewFile(); }
					 */
					file = new File(filePath, companyId + "." + extension);
					if (file.exists()) {
						file.delete();
					}
					if (extension.equals("xls")) {
						multipartFile.transferTo(file);
						fileIn = new DataInputStream(new FileInputStream(file));
						hbook = new HSSFWorkbook(fileIn);
						json = HSSFWorkbook(hbook, customer.getId(), taskId, typeId);
					} else if (extension.equals("xlsx")) {
						multipartFile.transferTo(file);
						fileIn = new DataInputStream(new FileInputStream(file));
						xbook = new XSSFWorkbook(fileIn);
						json = XSSFWorkbook(xbook, customer.getId(), taskId, typeId);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					json.setMessage("读取文件失败");
					json.setSuccess(false);
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
						logger.error(e.getMessage());
					}

				}
			}
		}
		return json;
	}

	public JsonResult HSSFWorkbook(HSSFWorkbook book, String companyId, Long taskId, String typeId) {

		JsonResult json = new JsonResult();
		User user = new User();
		// 批量插入的集合
		List<TemporaryBatchStart> batchList = new ArrayList<TemporaryBatchStart>();
		// 手机号集合（用户判断表格内重复）
		List<String> mobiles = new ArrayList<String>();
		HSSFSheet sheet = book.getSheetAt(0);
		int row = sheet.getPhysicalNumberOfRows();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < row; i++) {
			HSSFRow cellRow = sheet.getRow(i);
			HSSFCell cell = cellRow.getCell(0);
			// 如果是单元格为空继续循环
			if (cell == null)
				continue;
			// 设置单元格为字符属性
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			String mobile = cell.getStringCellValue().trim();
			// 如果表格内重复
			if (mobiles.contains(mobile))
				continue;
			// 添加到集合中(用于验证excel表格内的手机号重复)
			mobiles.add(mobile);
			// 查询用户在公司是否存在
			map.put("mobile", mobile);
			map.put("companyId", companyId);
			user = userMapper.selectByMobile(map);
			// 查询临时表是否存在改用户数据
			TemporaryBatchStart batchStart = new TemporaryBatchStart();
			batchStart.setUserId(user.getId());
			batchStart.setTaskId(taskId);
			List<TemporaryBatchStart> bl = temporaryBatchStartMapper.findByTaskIdAndUserId(batchStart);

			if (bl == null || bl.isEmpty()) {// 临时表对应的用户不存在時
				ApprovalType type = approvalTypeMapper.selectByPrimaryKey(typeId);

				batchStart.setThirdId(null);
				batchStart.setTypeId(typeId);
				batchStart.setApprovalTableConfigId(type.getApprovalTableConfigId());
				batchStart.setApprovalType(type.getName());
				batchStart.setCompanyId(companyId);
				batchStart.setCreateDate(DateUtil.getDateStr(new Date()));
				batchStart.setMobile(user.getMobile());
				batchStart.setUserName(user.getUserName());
				batchStart.setStatus(1);
				batchList.add(batchStart);
			}

		}
		json.setSuccess(true);
		// 批量插入
		temporaryBatchStartMapper.batchinsert(batchList);
		// 查询任务的所有没有删除的临时发起数据
		TemporaryBatchStart params = new TemporaryBatchStart();
		params.setTaskId(taskId);
		List<TemporaryBatchStart> list = temporaryBatchStartMapper.findByTaskIdAndUserId(params);
		// 更新一个任务的预期发起用户数
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setTocreatetaskUsers(list.size());
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);

		return json;
	}

	public JsonResult XSSFWorkbook(XSSFWorkbook book, String companyId, Long taskId, String typeId) {

		JsonResult json = new JsonResult();
		// List<String> userList = new ArrayList<String>();
		User user = new User();
		// 批量插入的集合
		List<TemporaryBatchStart> batchList = new ArrayList<TemporaryBatchStart>();
		// 手机号集合（用户判断表格内重复）
		List<String> mobiles = new ArrayList<String>();
		XSSFSheet sheet = book.getSheetAt(0);
		int row = sheet.getPhysicalNumberOfRows();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < row; i++) {
			XSSFRow cellRow = sheet.getRow(i);
			XSSFCell cell = cellRow.getCell(0);
			// 如果是单元格为空继续循环
			if (cell == null)
				continue;
			// 设置单元格为字符属性
			cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			String mobile = cell.getStringCellValue().trim();
			map.put("mobile", mobile);
			map.put("companyId", companyId);
			// 如果表格内重复
			if (mobiles.contains(mobile) || (user = userMapper.selectByMobile(map)) == null)
				continue;
			// 添加到集合中
			mobiles.add(mobile);
			// 查询用户在公司是否存在

			// 查询临时表是否存在改用户数据
			TemporaryBatchStart batchStart = new TemporaryBatchStart();
			batchStart.setUserId(user.getId());
			batchStart.setTaskId(taskId);
			List<TemporaryBatchStart> bl = temporaryBatchStartMapper.findByTaskIdAndUserId(batchStart);

			if (bl == null || bl.isEmpty()) {// 临时表对应的用户不存在時
				ApprovalType type = approvalTypeMapper.selectByPrimaryKey(typeId);
				// 临时数据初始化
				batchStart.setThirdId(null);
				batchStart.setTypeId(typeId);
				batchStart.setApprovalTableConfigId(type.getApprovalTableConfigId());
				batchStart.setApprovalType(type.getName());
				batchStart.setCompanyId(companyId);
				batchStart.setCreateDate(DateUtil.getDateStr(new Date()));
				batchStart.setMobile(user.getMobile());
				batchStart.setUserName(user.getUserName());
				batchStart.setStatus(1);
				batchList.add(batchStart);
			}

		}
		json.setSuccess(true);
		// 批量插入
		if (batchList != null && batchList.size() > 0)
			temporaryBatchStartMapper.batchinsert(batchList);
		// 查询任务的所有没有删除的临时发起数据
		TemporaryBatchStart params = new TemporaryBatchStart();
		params.setTaskId(taskId);
		List<TemporaryBatchStart> list = temporaryBatchStartMapper.findByTaskIdAndUserId(params);
		// 更新一个任务的预期发起用户数
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setTocreatetaskUsers(list.size());
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);

		return json;
	}

	/**
	 * @param request
	 * @param userIds
	 *            用户ID
	 * @param typeId
	 *            流程ID
	 * @param strartUserId
	 *            发起人ID 发起人 审批人 待阅人 流程id
	 * @return
	 */
	public JsonResult batchInsertApproval(HttpServletRequest request, String typeId, String userIds, String approvalIds,
			String readId, String startUserID, Customer customer, String wyyId) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String path = request.getContextPath();
		JsonResult json = new JsonResult();

		List<String> userIdList = new ArrayList<String>();
		Map<String, Object> map = new HashMap<String, Object>();
		map = (Map) JSON.parse(userIds);
		userIdList = (List) map.get("ids");
		String companyId = customer.getId();

		HashMap<String, String> hashmap = new HashMap<String, String>();
		hashmap.put("companyId", customer.getId());
		hashmap.put("id", typeId);

		ApprovalType approvalType = new ApprovalType();
		approvalType = approvalTypeMapper.getApprovalTypeById2(hashmap);

		if (approvalType == null) {
			json.setSuccess(false);
			json.setMessage("流程不存在");
			return json;
		}

		String typeName = approvalType.getName(); // 流程名称
		String confId = approvalType.getApprovalTableConfigId();// 流程表单配置ID
		/**
		 * 批量发起流程
		 */
		String startUserID2 = "";
		int j = 0;
		List<MsgSend> msgList = new ArrayList<MsgSend>();

		for (int i = 0; i < userIdList.size(); i++) {
			if (startUserID.equals(VirtualActorConstant.BATCH_PROMOTER_ROLE)) {// $ 发起人代表自己
				startUserID2 = userIdList.get(i);
			} else {
				startUserID2 = startUserID;
			}
			User user = userMapper.selectByPrimaryKey(userIdList.get(i));
			String username = user.getUserName();
			String approvalName = username + "的" + typeName + "申请";
			String link = request.getScheme() + "://" + serviceIp + "/" + "moblicApprove/toForm.do?typeId=" + typeId;
			// insertThirdStart(request, "1", startUserID, "", approvalName,
			// companyId, "",wyyId);
			//
			// insertThirdDeal(request, startUserID2, "1", userIdList.get(i),
			// approvalName, link, companyId, "", "1",user,approvalType,wyyId);
			List<ThirdApprovalDeal> dealList = new ArrayList<ThirdApprovalDeal>();
			List<TemporaryBatchStart> batchList = new ArrayList<TemporaryBatchStart>();

			ThirdApprovalDeal deal = new ThirdApprovalDeal();
			UUID uuid = UUID.randomUUID();
			String id = uuid.toString();
			deal.setId(id);
			deal.setStatus("1");
			deal.setArriveDate(sdf.format(date));
			deal.setUserId(userIdList.get(i));
			deal.setApprovalName(approvalName);
			deal.setCompanyId(companyId);
			deal.setLink(link + "&thirdId=" + id + "&status=" + "1");
			// deal.setRunId(runId);
			deal.setApprovalTableConfigId(confId);
			deal.setNodeStatus("1");
			deal.setUserStartId(startUserID2);
			deal.setWyyId(wyyId);
			dealList.add(deal);

			TemporaryBatchStart batchStart = new TemporaryBatchStart();
			batchStart.setUserId(userIdList.get(i));
			batchStart.setThirdId(id);
			batchStart.setTypeId(approvalType.getId());
			batchStart.setApprovalTableConfigId(confId);
			batchStart.setApprovalType(approvalType.getName());
			batchStart.setCompanyId(companyId);
			batchStart.setCreateDate(sdf.format(date));
			batchStart.setMobile(user.getMobile());
			batchStart.setUserName(user.getUserName());
			batchList.add(batchStart);

			if (thirdApprovalDealMapper.batchinsert(dealList) > 0
					&& temporaryBatchStartMapper.batchinsert(batchList) > 0) {
				json.setSuccess(true);
			} else {
				json.setSuccess(false);
				json.setMessage("服务器异常");
			}
			// 发短信
			MsgSend msgsend = new MsgSend();
			msgsend.setMobile(user.getMobile());
			msgsend.setContent("V网通移动审批中您有一条新待办[" + typeName + "]，请您及时处理。");
			msgsend.setInserttime(new Date());

			msgList.add(msgsend);

			j++;
		}

		// msgsSend(msgList, companyId);
		json.setModel(msgList);
		json.setSuccess(true);
		json.setMessage("成功发起" + j + "个流程");
		return json;
	}

	/**
	 * 插入start
	 * 
	 * @param request
	 * @param status
	 * @param userId
	 * @param link
	 * @param approvalName
	 * @param companyId
	 * @return
	 */

	public boolean insertThirdStart(HttpServletRequest request, String status, String userId, String link,
			String approvalName, String companyId, String runId, String wyyId) {
		boolean flag = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString();
		ThirdApprovalStart start = new ThirdApprovalStart();
		start.setId(id);
		start.setStartDate(sdf.format(date));
		start.setUserId(userId);
		start.setLink(link + "&thirdId=" + id + "&status=" + status);
		start.setCompanyId(companyId);
		start.setApprovalName(approvalName);
		start.setRunId(runId);
		start.setWyyId(wyyId);
		start.setStatus(status);
		if (thirdApprovalStartMapper.insertSelective(start) > 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 插入 thirdDeal
	 * 
	 * @param thirdId
	 * @param status
	 * @return
	 */
	public boolean insertThirdDeal(HttpServletRequest request, String startUserId, String status, String userId,
			String approvalName, String link, String companyId, String runId, String nodeStatus, User user,
			ApprovalType approval, String wyyId) {
		boolean flag = false;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		ThirdApprovalDeal deal = new ThirdApprovalDeal();
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString();
		deal.setId(id);
		deal.setStatus(status);
		deal.setArriveDate(sdf.format(date));
		deal.setUserId(userId);
		deal.setApprovalName(approvalName);
		deal.setCompanyId(companyId);
		deal.setLink(link + "&thirdId=" + id + "&status=" + status);
		deal.setWyyId(wyyId);
		// deal.setRunId(runId);
		deal.setApprovalTableConfigId(approval.getApprovalTableConfigId());
		deal.setNodeStatus(nodeStatus);
		deal.setUserStartId(startUserId);

		TemporaryBatchStart batchStart = new TemporaryBatchStart();
		batchStart.setUserId(userId);
		batchStart.setThirdId(id);
		batchStart.setTypeId(approval.getId());
		batchStart.setApprovalTableConfigId(approval.getApprovalTableConfigId());
		batchStart.setApprovalType(approval.getName());
		batchStart.setCompanyId(companyId);
		batchStart.setCreateDate(sdf.format(date));
		batchStart.setMobile(user.getMobile());
		batchStart.setUserName(user.getUserName());

		if (thirdApprovalDealMapper.insertSelective(deal) > 0
				&& temporaryBatchStartMapper.insertSelective(batchStart) > 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 批量发短信
	 * 
	 * @param list
	 * @param companyId
	 * @return
	 */
	public boolean msgsSend(List<MsgSend> list, String companyId) {
		List<MsgCompany> companys = msgCompanyMapper.getMsgCompanyByCompanyId(companyId);
		if (companys != null && companys.size() > 0 && !list.isEmpty()) {
			msgSendMapper.insertBatch(list);
		}
		return true;
	}

	@Override
	public List<MsgSend> batchInsertApproval(String companyId, String typeId, Long taskId, String startUserId,
			String wyyId) {

		ApprovalType type = approvalTypeMapper.selectByPrimaryKey(typeId);
		if (type == null)
			throw new RuntimeException("流程不存在");
		// 获取临时发起表的任务数据
		TemporaryBatchStart params = new TemporaryBatchStart();
		params.setTaskId(taskId);
		List<TemporaryBatchStart> tembatchList = temporaryBatchStartMapper.findByTaskIdAndUserId(params);
		// 代办的批量集合
		List<ThirdApprovalDeal> dealList = new ArrayList<ThirdApprovalDeal>();
		// 发短信的批量集合
		List<MsgSend> msgList = new ArrayList<MsgSend>();
		String link = "/mobile/moblicApprove/toForm.do?typeId=" + typeId + "&isBatch=1";
		// 根据零时表的任务数据插入代办表
		for (TemporaryBatchStart s : tembatchList) {
			if (VirtualActorConstant.BATCH_PROMOTER_ROLE.equals(startUserId)) {// 如果发起用户是自己
				startUserId = s.getUserId();
			}
			// 初始化一条代办表数据
			ThirdApprovalDeal deal = new ThirdApprovalDeal();
			UUID uuid = UUID.randomUUID();
			String id = uuid.toString();
			deal.setId(id);
			deal.setStatus("1");
			deal.setArriveDate(DateUtil.getDateStr(new Date()));
			deal.setUserId(s.getUserId());
			deal.setApprovalName(s.getUserName() + "的" + s.getApprovalType() + "申请");
			deal.setCompanyId(companyId);
			deal.setLink(link + "&thirdId=" + id + "&status=" + "1" + "&taskId=" + taskId);
			// deal.setRunId(runId);
			deal.setApprovalTableConfigId(s.getApprovalTableConfigId());
			deal.setNodeStatus("1");
			deal.setUserStartId(startUserId);
			deal.setWyyId(wyyId);
			dealList.add(deal);

			// 更新临时表存的代办表id
			s.setThirdId(id);
			temporaryBatchStartMapper.updateByPrimaryKeySelective(s);

			// 发短信
			MsgSend msgsend = new MsgSend();
			msgsend.setMobile(s.getMobile());
			msgsend.setContent("V网通移动审批中您有一条新待办[" + s.getApprovalType() + "]，请您及时处理。");
			msgsend.setInserttime(new Date());

			msgList.add(msgsend);
		}
		// 批量插入代办
		if (dealList != null && !dealList.isEmpty())
			thirdApprovalDealMapper.batchinsert(dealList);
		// 获取更新实际的发起数量
		int dealNum = temporaryBatchStartMapper.findCountByTaskId(taskId);
		ApprovalBatchTask task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setCreatedtaskUsers(dealNum);
		task.setStatus(1);// 状态改为完成发起
		task.setStartTime(new Date());
		task.setStartUserId(startUserId);
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);

		return msgList;
	}

	@Override
	public void cancelApprovals(Long taskId) {
		ApprovalBatchTask task = approvalBatchTaskMapper.selectByPrimaryKey(taskId);
		if (task == null)
			throw new RuntimeException("任务不存在");
		int tc = temporaryBatchStartMapper.cancelByTaskId(taskId);
		int dc = thirdApprovalDealMapper.deleteByTaskId(taskId);
		if (tc != dc) {
			logger.error("任务id：" + taskId);
			logger.error("删除deal表数量:" + dc);
			logger.error("更新临时表数量:" + tc);
		}
		int undoNum = task.getUndotaskUsers() != null ? task.getUndotaskUsers() : 0;
		task = new ApprovalBatchTask();
		task.setId(taskId);
		task.setUndotaskUsers(dc + undoNum);
		task.setCancelTime(new Date());
		approvalBatchTaskMapper.updateByPrimaryKeySelective(task);
	}

}
