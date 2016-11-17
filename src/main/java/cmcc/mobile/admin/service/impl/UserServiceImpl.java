package cmcc.mobile.admin.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.AdminUserMapper;
import cmcc.mobile.admin.dao.CustomerMapper;
import cmcc.mobile.admin.dao.OrganizationMapper;
import cmcc.mobile.admin.dao.TotalUserMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.AdminUser;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.service.UserService;
import cmcc.mobile.admin.util.DateUtil;
import cmcc.mobile.admin.util.IdCreateUtil;
import cmcc.mobile.admin.util.PropertiesUtil;
import cmcc.mobile.admin.util.StringUtil;
import cmcc.mobile.admin.vo.UserInfoVo;

/**
 *
 * @author renlinggao
 * @Date 2016年6月29日
 */
@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private AdminUserMapper adminUserMapper;

	@Autowired
	private CustomerMapper customerMapper;

	@Autowired
	private OrganizationMapper organizationMapper;

	@Autowired
	private TotalUserMapper totalUserMapper;

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public JsonResult checkPass(HttpServletRequest request, String mobile, String password) {
		List<Customer> list = null;
		JsonResult result = new JsonResult(true, null, null);
		AdminUser params = new AdminUser();
		params.setMobile(mobile);
		params.setPassword(password);
		params = adminUserMapper.checkPass(params);
		if (params != null) {
			list = customerMapper.findComByRole(params.getId());
		} else {
			result.setSuccess(false);
			result.setMessage("手机号或密码错误");
			return result;
		}

		if (list == null || list.size() == 0) {
			result.setSuccess(false);
			result.setMessage("用户没有集团");
		}
		result.setModel(list);
		request.getSession().setAttribute("user", params);// 往session放用户信息
		return result;
	}

	@Override
	public Customer selectCompany(String companyId) {
		Customer c = customerMapper.selectByPrimaryKey(companyId);
		return c;
	}

	@Override
	public File saveImportFile(MultipartHttpServletRequest mr) throws IllegalStateException, IOException {
		File file = null;
		String filePath = PropertiesUtil.getAppByKey("PERSON_INFO_FILE_PATH");// 文件上传保存的地址
		for (Iterator<MultipartFile> ite = mr.getFileMap().values().iterator(); ite.hasNext();) {
			MultipartFile multipartFile = ite.next();
			if (multipartFile.getSize() > 0) {
				// 获取文件名
				String fileName = multipartFile.getOriginalFilename();
				// 获取文件后缀名
				String extension = FilenameUtils.getExtension(fileName);

				file = new File(filePath, "importUser_" + UUID.randomUUID().toString() + "." + extension);

				if (file.exists()) {
					file.delete();
				}
				multipartFile.transferTo(file);
			}

		}
		return file;
	}

	@Override
	public Map<String, Object> saveOrgAndUser(File file, Customer c, AdminUser user, String dbName) {
		String extension = FilenameUtils.getExtension(file.getName());
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if ("xls".equals(extension)) {
			HSSFWorkbook book = null;
			FileInputStream filein = null;
			try {
				filein = new FileInputStream(file);
				book = new HSSFWorkbook(filein);
				resultMap = hssfSave(book, c.getId(), dbName);
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			} finally {
				try {
					filein.close();
					book.close();
					file.delete();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}

		} else if ("xlsx".equals(extension)) {
			XSSFWorkbook book = null;
			FileInputStream filein = null;
			try {
				filein = new FileInputStream(file);
				book = new XSSFWorkbook(filein);
				resultMap = xssfSave(book, c.getId(), dbName);
			} catch (FileNotFoundException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			} finally {
				try {
					filein.close();
					book.close();
					file.delete();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}

		}
		return resultMap;
	}

	@Override
	public void saveTotalUsers(List<TotalUser> users) {
		if (users != null && users.size() > 0)
			totalUserMapper.insertBatch(users);
		// for (TotalUser user : users) {
		// totalUserMapper.insert(user);
		// }
	}

	/**
	 * 低版本excel导入
	 * 
	 * @param book
	 * @param companyId
	 * @return
	 */
	private Map<String, Object> hssfSave(HSSFWorkbook book, String companyId, String dbName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<TotalUser> totalUsers = new ArrayList<TotalUser>();

		List<User> importUsers = new ArrayList<User>();// 用户集合
		List<String> importMobiles = new ArrayList<String>();// 导入的手机号

		HSSFSheet sheet = book.getSheetAt(0);// 获取第一个工作簿
		HSSFRow titleRow = sheet.getRow(0);// 获取第一行标题行
		int row = sheet.getPhysicalNumberOfRows();// 行数
		int column = titleRow.getPhysicalNumberOfCells();// 列数

		String mess = "";
		// int okNum = 0;

		for (int i = 1; i < row; i++) {
			try {
				HSSFRow cellRow = sheet.getRow(i);
				String orgId = null;
				String fullOrgName = "";
				String orgName = "";
				User user = new User();
				for (int j = 0; j < column; j++) {
					HSSFCell cell = cellRow.getCell(j);
					if (cell == null || titleRow.getCell(j).getStringCellValue().indexOf("部门") > -1) {
						orgName = getCellValue(cell);
						if (orgName != null) {
							if (fullOrgName.length() > 0)
								fullOrgName += "/";
							fullOrgName += orgName;
						}

						Organization organization = getOrganization(companyId, orgName, orgId, fullOrgName);

						if (organization != null) {
							orgId = organization.getId();
							orgName = organization.getOrgName();
						}
					} else if ("姓名".equals(titleRow.getCell(j).getStringCellValue())) {
						user.setUserName(getCellValue(cell));
					} else if ("手机号".equals(titleRow.getCell(j).getStringCellValue())) {
						String mobile = getCellValue(cell);
						if (StringUtil.isMobile(mobile) && !importMobiles.contains(mobile)) {
							user.setMobile(mobile);
							importMobiles.add(mobile);
						} else if (importMobiles.contains(mobile)) {
							throw new RuntimeException("0");
						} else {
							throw new RuntimeException("1");
						}

					} else if ("工号".equals(titleRow.getCell(j).getStringCellValue())) {
						user.setWorkNumber(getCellValue(cell));
					} else if ("顺序号".equals(titleRow.getCell(j).getStringCellValue())) {
						String showIndex = getCellValue(cell);
						user.setShowindex(new Integer(StringUtils.isNotEmpty(showIndex) ? showIndex : "100"));
					} else if ("职位".equals(getCellValue(cell))) {
						user.setPost(cell.getStringCellValue());
					}
				}
				user.setId(IdCreateUtil.createUserId());
				user.setCreatTime(DateUtil.getDateStr(new Date()));
				user.setOrgId(orgId);
				user.setStatus("1");
				user.setCompanyId(companyId);
				importUsers.add(user);
				// userMapper.insertSelective(user);

				TotalUser totalUser = new TotalUser();
				totalUser.setId(user.getId());
				totalUser.setDatabaseName(dbName);
				totalUser.setIsManager("0");
				totalUser.setMobile(user.getMobile());
				totalUser.setName(user.getUserName());
				totalUser.setStatus("0");
				totalUser.setCompanyId(companyId);
				totalUser.setCreatetime(DateUtil.getDateStr(new Date()));

				totalUsers.add(totalUser);

				// okNum++;
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().equals("0")) {
					mess += "第" + (i + 1) + "行手机号已经存在文件中\r\n";
				} else if (e.getMessage() != null && e.getMessage().equals("1")) {
					mess += "第" + (i + 1) + "行手机号格式错误\r\n";
				} else {
					mess += "第" + (i + 1) + "行导入失败\r\n";
				}
				importUsers.add(null);
				totalUsers.add(null);
			}

		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mobiles", importMobiles);
		params.put("companyId", companyId);
		List<String> updateMobile = userMapper.getExistMobiles(params);// 获取需要更新的mobile
		List<User> insertUser = new ArrayList<User>();// 需要批量插入的用户
		List<User> updateUser = new ArrayList<User>();// 需要批量更新的数据
		List<TotalUser> insertTotalUser = new ArrayList<TotalUser>();// 总库要插入的数据

		mess += "\r\n";
		for (int i = 0; i < importUsers.size(); i++) {
			User u = importUsers.get(i);
			if (u == null)
				continue;
			if (updateMobile.contains(u.getMobile())) {
				updateUser.add(u);
				mess += "第" + (i + 2) + "行人员信息更新成功\r\n";
			} else {
				insertUser.add(u);
				insertTotalUser.add(totalUsers.get(i));
			}
		}

		if (updateUser != null && updateUser.size() > 0) {
			params.put("users", updateUser);
			userMapper.batchUpdate(params);
			mess += updateUser.size() + "条记录更新成功\r\n";
		}

		if (insertUser != null && insertUser.size() > 0) {
			userMapper.insertBatch(insertUser);
			mess += insertUser.size() + "条记录导入成功";
		}
		// mess += okNum + "条记录导入成功";
		resultMap.put("users", insertTotalUser);
		resultMap.put("mess", mess);
		return resultMap;

	}

	/**
	 * 高版本excel导入
	 * 
	 * @param book
	 * @param companyId
	 * @return
	 */
	private Map<String, Object> xssfSave(XSSFWorkbook book, String companyId, String dbName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();// 返回的结果集
		List<TotalUser> totalUsers = new ArrayList<TotalUser>();// 需要插入主库的数据

		List<User> importUsers = new ArrayList<User>();// 用户集合
		List<String> importMobiles = new ArrayList<String>();// 导入的手机号

		XSSFSheet sheet = book.getSheetAt(0);// 获取第一个工作簿
		XSSFRow titleRow = sheet.getRow(0);// 获取第一行标题行
		int row = sheet.getPhysicalNumberOfRows();// 行数
		int column = titleRow.getPhysicalNumberOfCells();// 列数

		String mess = "";

		for (int i = 1; i < row; i++) {
			try {
				XSSFRow cellRow = sheet.getRow(i);// 当前excel的一行数据
				String orgId = null;
				String fullOrgName = "";
				String orgName = "";
				User user = new User();
				for (int j = 0; j < column; j++) {
					XSSFCell cell = cellRow.getCell(j);
					if (titleRow.getCell(j).getStringCellValue().indexOf("部门") > -1) {
						String lastOrgName = orgName;
						orgName = getCellValue(cell);
						if ((j == 0 && StringUtils.isEmpty(orgName))
								|| (j != 0 && StringUtils.isNotEmpty(orgName) && StringUtils.isEmpty(lastOrgName))) {
							throw new RuntimeException("2");
						}
						if (StringUtils.isNotEmpty(orgName)) {
							if (fullOrgName.length() > 0)
								fullOrgName += "/";
							fullOrgName += orgName;
						}

						Organization organization = getOrganization(companyId, orgName, orgId, fullOrgName);

						if (organization != null) {
							orgId = organization.getId();
							orgName = organization.getOrgName();
						}
					} else if ("姓名".equals(titleRow.getCell(j).getStringCellValue())) {
						user.setUserName(getCellValue(cell));
					} else if ("手机号".equals(titleRow.getCell(j).getStringCellValue())) {
						String mobile = getCellValue(cell);
						if (StringUtil.isMobile(mobile) && !importMobiles.contains(mobile)) {
							user.setMobile(mobile);
							importMobiles.add(mobile);
						} else if (StringUtil.isMobile(mobile) && importMobiles.contains(mobile)) {
							throw new RuntimeException("0");
						} else {
							throw new RuntimeException("1");
						}
					} else if ("工号".equals(titleRow.getCell(j).getStringCellValue())) {
						user.setWorkNumber(getCellValue(cell));
					} else if ("顺序号".equals(titleRow.getCell(j).getStringCellValue())) {
						String showIndex = getCellValue(cell);
						user.setShowindex(new Integer(StringUtils.isNotEmpty(showIndex) ? showIndex : "100"));
					} else if ("职位".equals(getCellValue(cell))) {
						user.setPost(cell.getStringCellValue());
					}
				}
				user.setId(IdCreateUtil.createUserId());
				user.setCreatTime(DateUtil.getDateStr(new Date()));
				user.setUpdateTime(DateUtil.getDateStr(new Date()));
				user.setOrgId(orgId);
				user.setStatus("1");
				user.setCompanyId(companyId);
				importUsers.add(user);

				// userMapper.insertSelective(user);

				TotalUser totalUser = new TotalUser();
				totalUser.setId(user.getId());
				totalUser.setDatabaseName(dbName);
				totalUser.setIsManager("0");
				totalUser.setMobile(user.getMobile());
				totalUser.setName(user.getUserName());
				totalUser.setStatus("0");
				totalUser.setCompanyId(companyId);
				totalUser.setCreatetime(DateUtil.getDateStr(new Date()));

				totalUsers.add(totalUser);

			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().equals("0")) {
					mess += "第" + (i + 1) + "行手机号已经存在文件中\r\n";
				} else if (e.getMessage() != null && e.getMessage().equals("1")) {
					mess += "第" + (i + 1) + "行手机号格式错误\r\n";
				} else if (e.getMessage() != null && e.getMessage().equals("2")) {
					mess += "第" + (i + 1) + "行部门数据错误\r\n";
				}
				importUsers.add(null);
				totalUsers.add(null);
			}

		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mobiles", importMobiles);
		params.put("companyId", companyId);
		List<String> updateMobile = new ArrayList<String>();
		if (importMobiles != null && importMobiles.size() > 0) {
			updateMobile = userMapper.getExistMobiles(params);// 获取需要更新的mobile
		}
		List<User> insertUser = new ArrayList<User>();// 需要批量插入的用户
		List<User> updateUser = new ArrayList<User>();// 需要批量更新的数据
		List<TotalUser> insertTotalUser = new ArrayList<TotalUser>();// 总库要插入的数据

		mess += "\r\n";
		for (int i = 0; i < importUsers.size(); i++) {
			User u = importUsers.get(i);
			if (u == null)
				continue;
			if (updateMobile.contains(u.getMobile())) {
				updateUser.add(u);
				mess += "第" + (i + 2) + "行人员信息更新成功\r\n";
			} else {
				insertUser.add(u);
				insertTotalUser.add(totalUsers.get(i));
			}
		}

		if (updateUser != null && updateUser.size() > 0) {
			params.put("users", updateUser);
			userMapper.batchUpdate(params);
			mess += updateUser.size() + "条记录更新成功\r\n";
		}

		if (insertUser != null && insertUser.size() > 0) {
			userMapper.insertBatch(insertUser);
			mess += insertUser.size() + "条记录导入成功";
		}

		resultMap.put("users", insertTotalUser);
		resultMap.put("mess", mess);
		return resultMap;

	}

	/**
	 * 获取并判断部门
	 * 
	 * @param companyId
	 * @param name
	 * @param previousId
	 * @param fullOrgName
	 * @return
	 */
	private Organization getOrganization(String companyId, String name, String previousId, String fullOrgName) {

		Organization organization = new Organization();
		organization.setOrgName(name);
		organization.setPreviousId(previousId);
		organization.setCompanyId(companyId);
		organization = organizationMapper.findDeptByNameAndPreviousId(organization);
		if (organization == null && StringUtils.isNotEmpty(name)) {
			organization = new Organization();
			organization.setId(UUID.randomUUID().toString());
			organization.setCreatTime(DateUtil.getDateStr(new Date()));
			organization.setOrgName(name);
			organization.setStatus("1");
			organization.setOrgFullname(fullOrgName);
			organization.setPreviousId(previousId);
			organization.setShowindex(100);
			organization.setCompanyId(companyId);
			organizationMapper.insertSelective(organization);
		}
		return organization;
	}

	/**
	 * 获取单元格的字符串值
	 * 
	 * @param cell
	 * @return
	 */
	private String getCellValue(XSSFCell cell) {
		String result = null;
		if (cell != null) {
			cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			result = cell.getStringCellValue().trim();
		}
		return result;

	}

	/**
	 * 获取单元格的字符串值（高版本）
	 * 
	 * @param cell
	 * @return
	 */
	private String getCellValue(HSSFCell cell) {
		String result = null;
		if (cell != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			result = cell.getStringCellValue().trim();
		}
		return result;
	}

	@Override
	public Map<String, UserInfoVo> getCompanyUserInfos(String companyId, List<String> userIds) {

		Map<String, UserInfoVo> result = new HashMap<String, UserInfoVo>();
		if (userIds != null && userIds.size() > 0) {
			Map<String, Object> params = new HashMap<>();
			params.put("companyId", companyId);
			params.put("list", userIds);
			List<UserInfoVo> users = userMapper.findByCompanyId(params);
			for (UserInfoVo userInfo : users) {
				result.put(userInfo.getId(), userInfo);
			}
		}
		return result;
	}

}
