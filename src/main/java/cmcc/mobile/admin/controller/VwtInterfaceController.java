package cmcc.mobile.admin.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.TotalUserMapper;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.VwtInterfaceService;
import cmcc.mobile.admin.entity.VwtCorp;
import cmcc.mobile.admin.entity.VwtCorpHis;
import cmcc.mobile.admin.entity.VwtDept;
import cmcc.mobile.admin.entity.VwtDeptHis;
import cmcc.mobile.admin.entity.VwtMemberInfo;
import cmcc.mobile.admin.entity.VwtMemberInfoHis;




@Controller
@RequestMapping("vwt")
public class VwtInterfaceController extends BaseController{
	
	@Autowired
	private VwtInterfaceService vwtInterfaceService;
	@Autowired
	private TotalUserMapper totalUserMapper;
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * ,String companyId,String synchroDate,String dbName
	 * @param request
	 * @return
	 */
	@RequestMapping("synchroVwtData")
	@ResponseBody
	public JsonResult synchroVwtData(HttpServletRequest request) {
		SimpleDateFormat sfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JsonResult json = new JsonResult();
		String companyId = getCompany().getId();
		//String companyId = "31A09CED2CFB0062E053AC10023C5340";
		MultipleDataSource.setDataSourceKey("");
		Customer customer = vwtInterfaceService.getCustomerById(companyId);
		if(customer.getEcode()==null || customer.getEcode().equals("")){
			json.setSuccess(false);
			json.setMessage("没有需要同步的数据");
			return json;
		}
		String synchroDate = customer.getSynchroDate();
		String dbName = getCompany().getDbname();
		Map<String,Object> map = new HashMap<String,Object>();
		
		MultipleDataSource.setDataSourceKey("oracle");
		String maxTime = "" ;
		VwtCorp corp = vwtInterfaceService.getInfoById(customer.getEcode());
			if(corp !=null){
				map.put("corpId", customer.getEcode());
				map.put("actTime", synchroDate);
				
				List<VwtDept> deptlist = vwtInterfaceService.getDeptByParam(map);
				map.put("createtime", synchroDate);
				List<VwtMemberInfo> addUserList = vwtInterfaceService.getUserInfoByParams(map);
				map.put("createtime", null);
				map.put("operationTime",synchroDate);
				map.put("date",synchroDate);
				List<VwtMemberInfo> updateUserList = vwtInterfaceService.getUserInfoByParams(map);
				List<VwtMemberInfoHis> deleteUserList = vwtInterfaceService.getDeleteUserByParams(map);
				List<VwtDeptHis> deleteDeptList = vwtInterfaceService.getDeleteDeptByParams(map);
				if(deptlist.size()==0&&addUserList.size()==0&&updateUserList.size()==0&&deleteUserList.size()==0&&deleteDeptList.size()==0){
					json.setSuccess(false);
					json.setMessage("没有需要同步的数据");
					return json;
				}	
				maxTime = vwtInterfaceService.selectMaxtime(customer.getEcode(),maxTime) ;
				MultipleDataSource.setDataSourceKey("");
				vwtInterfaceService.synchroVwtDataZK(companyId,addUserList, updateUserList, deleteUserList,dbName);
				MultipleDataSource.setDataSourceKey(dbName);
				vwtInterfaceService.synchroVwtDataFK(companyId,deptlist, addUserList, updateUserList, deleteUserList, deleteDeptList);
			
			}else{
				map.put("corpId", companyId);
				map.put("date", synchroDate);
				List<VwtCorpHis> corphis = vwtInterfaceService.getDeleteCropByParams(map);
				if(corphis!=null && corphis.size()>0){
					MultipleDataSource.setDataSourceKey(dbName);
					vwtInterfaceService.deleteByCorpId(companyId);
					MultipleDataSource.setDataSourceKey("");
					vwtInterfaceService.deleteTotalUserByCorpId(companyId);
				}
			}
			MultipleDataSource.setDataSourceKey("");
			
			customer.setSynchroDate(maxTime);
			vwtInterfaceService.updateCustomer(customer);
			json.setSuccess(true);
			json.setMessage("同步成功");		
		return json;
	}
}
