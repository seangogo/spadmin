package cmcc.mobile.admin.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.UserService;
import cmcc.mobile.admin.util.PropertiesUtil;

/**
 *
 * @author renlinggao
 * @Date 2016年6月29日
 */
@Controller
@RequestMapping("user")
public class UserController extends BaseController {
	@Autowired
	private UserService userService;

	@RequestMapping("/checkPass")
	@ResponseBody
	public JsonResult login(String username, String password, HttpServletRequest request, HttpSession session,
			String selfCompanyId, String secretKey) {
		MultipleDataSource.setDataSourceKey(null);
		return userService.checkPass(request, username, password);
	}

	@RequestMapping("checkLogin")
	@ResponseBody
	public JsonResult selectCompany(HttpServletRequest request, Customer customer) {
		JsonResult result = new JsonResult(true, null, null);
		MultipleDataSource.setDataSourceKey(null);
		customer = userService.selectCompany(customer.getId());
		if (customer == null) {
			result.setSuccess(false);
			result.setMessage("公司不存在");
		}
		request.getSession().setAttribute("company", customer);
		return result;
	}

	@RequestMapping("userImport")
	@ResponseBody
	public JsonResult userImport(HttpServletRequest request) throws IllegalStateException, IOException {

		JsonResult result = new JsonResult(true, null, null);
		String dbName = getCompany().getDbname();
		MultipartHttpServletRequest mr = (MultipartHttpServletRequest) request;
		java.io.File file = userService.saveImportFile(mr);
		// 切库插入业务库数据
		MultipleDataSource.setDataSourceKey(getCompany().getDbname());
		Map<String, Object> resultMap = userService.saveOrgAndUser(file, getCompany(), getUser(), dbName);
		// 插入主库的数据
		String mainDB = PropertiesUtil.getDbByKey("MAIN_DB");
		MultipleDataSource.setDataSourceKey(mainDB);
		List<TotalUser> users = (List<TotalUser>) resultMap.get("users");
		if (users != null)
			userService.saveTotalUsers(users);
		result.setMessage((String) resultMap.get("mess"));
		return result;
	}
}
