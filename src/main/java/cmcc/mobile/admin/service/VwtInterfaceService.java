package cmcc.mobile.admin.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.entity.VwtCorp;
import cmcc.mobile.admin.entity.VwtCorpHis;
import cmcc.mobile.admin.entity.VwtCustomer;
import cmcc.mobile.admin.entity.VwtDept;
import cmcc.mobile.admin.entity.VwtDeptHis;
import cmcc.mobile.admin.entity.VwtMemberInfo;
import cmcc.mobile.admin.entity.VwtMemberInfoHis;

public interface VwtInterfaceService {

	
	public VwtCorp getInfoById(String id);
	
	public Customer getCustomerById(String id);
	
	public List<VwtDept> getDeptByParam(Map<String,Object> map);
	
	public List<VwtMemberInfo> getUserInfoByParams(Map<String,Object> map);
	
	public List<VwtMemberInfoHis> getDeleteUserByParams(Map<String,Object> map);
	
	public List<VwtDeptHis> getDeleteDeptByParams(Map<String,Object> map);
	
	public List<VwtCorpHis> getDeleteCropByParams(Map<String,Object> map);
	
	public boolean deleteByCorpId(String id);
	
	public boolean deleteTotalUserByCorpId(String id);
	
	public boolean updateCustomer(Customer customer);
	
	/**
	 * 手动同步分库数据
	 * @param deptlist
	 * @param addUserList
	 * @param updateUserList
	 * @param deleteUserList
	 * @param deleteDeptList
	 * @return
	 */
	public boolean synchroVwtDataFK(String companyId,List<VwtDept> deptlist,List<VwtMemberInfo> addUserList,List<VwtMemberInfo> updateUserList,List<VwtMemberInfoHis> deleteUserList,List<VwtDeptHis> deleteDeptList);
	
	/**
	 * 手动同步主库数据
	 * @param addUserList
	 * @param updateUserList
	 * @param deleteUserList
	 * @param dbName
	 * @return
	 */
	public boolean synchroVwtDataZK(String companyId,List<VwtMemberInfo> addUserList,List<VwtMemberInfo> updateUserList,List<VwtMemberInfoHis> deleteUserList,String dbName);

	public String selectMaxtime(String ecode, String maxTime);


	
}
