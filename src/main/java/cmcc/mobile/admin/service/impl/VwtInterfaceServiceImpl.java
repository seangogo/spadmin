package cmcc.mobile.admin.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cmcc.mobile.admin.dao.CustomerMapper;
import cmcc.mobile.admin.entity.Customer;
import cmcc.mobile.admin.service.VwtInterfaceService;
import cmcc.mobile.admin.util.IdCreateUtil;
import cmcc.mobile.admin.dao.OrganizationMapper;
import cmcc.mobile.admin.dao.TotalUserMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.dao.VwtClientManagerMapper;
import cmcc.mobile.admin.dao.VwtCorpHisMapper;
import cmcc.mobile.admin.dao.VwtCorpMapper;
import cmcc.mobile.admin.dao.VwtCustomerMapper;
import cmcc.mobile.admin.dao.VwtDeptHisMapper;
import cmcc.mobile.admin.dao.VwtDeptMapper;
import cmcc.mobile.admin.dao.VwtMemberInfoHisMapper;
import cmcc.mobile.admin.dao.VwtMemberInfoMapper;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.entity.VwtCorp;
import cmcc.mobile.admin.entity.VwtCorpHis;
import cmcc.mobile.admin.entity.VwtCustomer;
import cmcc.mobile.admin.entity.VwtDept;
import cmcc.mobile.admin.entity.VwtDeptHis;
import cmcc.mobile.admin.entity.VwtMemberInfo;
import cmcc.mobile.admin.entity.VwtMemberInfoHis;
@Service
public class VwtInterfaceServiceImpl implements VwtInterfaceService{

	@Autowired
	private VwtCorpMapper vwtCorpMapper;
	
	@Autowired
	private VwtDeptMapper vwtDeptMapper;
	
	@Autowired
	private VwtMemberInfoMapper vwtMemebrtInfoMapper;
	
	@Autowired
	private CustomerMapper customerMapper; 
	
	@Autowired
	private VwtMemberInfoHisMapper vwtMemeberInfoHisMapper;
	
	@Autowired
	private VwtDeptHisMapper vwtDeptHisMapper;
	
	@Autowired
	private VwtCorpHisMapper vwtCorpHisMapper;
	
	@Autowired
	private OrganizationMapper organizationMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private TotalUserMapper totalUserMapper;
	
	@Autowired
	private VwtCustomerMapper vwtCustomerMapper;
	
	@Autowired
	private VwtClientManagerMapper vwtClientManagerMapper;
	
	public VwtCorp getInfoById(String id) {
		return vwtCorpMapper.getInfoById(id);
	}


	@Override
	public List<VwtDept> getDeptByParam(Map<String, Object> map) {
		
		return vwtDeptMapper.getDeptByParam(map);
	}

	@Override
	public List<VwtMemberInfo> getUserInfoByParams(Map<String, Object> map) {
		
		return vwtMemebrtInfoMapper.getUserInfoByParams(map);
	}

	@Override
	public List<VwtMemberInfoHis> getDeleteUserByParams(Map<String, Object> map) {
		
		return vwtMemeberInfoHisMapper.getDeleteByParams(map);
	}

	@Override
	public List<VwtDeptHis> getDeleteDeptByParams(Map<String, Object> map) {
		return vwtDeptHisMapper.getDeleteByParams(map);
	}

	
	public List<VwtCorpHis> getDeleteCropByParams(Map<String, Object> map) {
		return vwtCorpHisMapper.getDeleteByParams(map);
	}

	

	/**
	 * 同步分库数据
	 * @param deptlist
	 * @param addUserList
	 * @param updateUserList
	 * @param deleteUserList
	 * @param deleteDeptList
	 * @return
	 */
	public boolean synchroVwtDataFK(String companyId,List<VwtDept> deptlist,List<VwtMemberInfo> addUserList,
			List<VwtMemberInfo> updateUserList, List<VwtMemberInfoHis> deleteUserList,
			List<VwtDeptHis> deleteDeptList) {
		
		SimpleDateFormat sfmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//同步部门
		for(int i=0;i<deptlist.size();i++){
			String orgId = deptlist.get(i).getDeptid();
			Organization org = new Organization();
			org.setId(deptlist.get(i).getDeptid());
			org.setOrgName(deptlist.get(i).getPartname());
			org.setShowindex(deptlist.get(i).getSort());
			org.setPreviousId(deptlist.get(i).getParentdeptnum().equals("1")?"":deptlist.get(i).getParentdeptnum());
			org.setCompanyId(companyId);
			org.setOrgFullname(deptlist.get(i).getPartfullname());
			org.setvId(deptlist.get(i).getDeptid());
			org.setStatus("1");
		
			if(organizationMapper.selectByPrimaryKey(orgId)==null){//新增
				organizationMapper.insertSelective(org);
			}else{//更新
				//org.setStatus("1");
				organizationMapper.updateByPrimaryKeySelective(org);
			}	
		}
		
		List<User> userList = new ArrayList<User>();
		//同步新增人员
		for(int i=0;i<addUserList.size();i++){
			User user = new User();
			user.setId(addUserList.get(i).getId()) ;
			user.setvId(addUserList.get(i).getMemid());
			user.setCompanyId(companyId);
			user.setMobile(addUserList.get(i).getTelnum());
			user.setUserName(addUserList.get(i).getMembername());
			user.setOrgId(addUserList.get(i).getDeptid());
			user.setShowindex(addUserList.get(i).getSort());
			user.setWorkNumber(addUserList.get(i).getJobnum());
			user.setStatus("0");
			user.setPassWord("111111");
			user.setCreatTime(sfmt.format(addUserList.get(i).getCreattime()));
			userMapper.insertSelective(user);
		}
		
		//同步更新人员
		for(int i=0;i<updateUserList.size();i++){
			User user = new User();
			user.setvId(updateUserList.get(i).getMemid());
			user.setCompanyId(companyId);
			user.setMobile(updateUserList.get(i).getTelnum());
			user.setUserName(updateUserList.get(i).getMembername());
			user.setOrgId(updateUserList.get(i).getDeptid());  
			
			user.setShowindex(updateUserList.get(i).getSort());
			user.setWorkNumber(updateUserList.get(i).getJobnum());
			//user.setStatus(String.valueOf(updateUserList.get(i).getMemstatus()));
			user.setCreatTime(sfmt.format(updateUserList.get(i).getCreattime()));
			userMapper.updateByPrimaryKeySelectives(user);
		}
		
		//同步删除人员
		for(int i=0;i<deleteUserList.size();i++){
			User user = new User();
			user.setvId(deleteUserList.get(i).getMemid());
			user.setStatus("9");
			userMapper.updateByPrimaryKeySelectives(user);
		}
		
		//同步删除部门
		for(int i=0;i<deleteDeptList.size();i++){
			Organization org = new Organization();
			org.setStatus("9");
			org.setId(deleteDeptList.get(i).getDeptid());
			organizationMapper.updateByPrimaryKeySelective(org);
		}
		
		return true;
	}

	/**
	 * 同步主库数据
	 * @param addUserList
	 * @param updateUserList
	 * @param deleteUserList
	 * @param dbName
	 * @return
	 */
	@Override
	public boolean synchroVwtDataZK(String companyId,List<VwtMemberInfo> addUserList, List<VwtMemberInfo> updateUserList,
			List<VwtMemberInfoHis> deleteUserList,String dbName) {
		
		//同步新增人员
		for(int i=0;i<addUserList.size();i++){
			TotalUser totalUser = new TotalUser();
			String id = IdCreateUtil.createGroupId() ;
			totalUser.setId(id) ;
			totalUser.setvId(addUserList.get(i).getMemid());
			totalUser.setDatabaseName(dbName);
			totalUser.setMobile(addUserList.get(i).getTelnum());
			totalUser.setName(addUserList.get(i).getMembername());
			totalUser.setType("2");
			totalUser.setStatus("0");
			totalUser.setCompanyId(companyId);
			totalUser.setPassword("11111");
			addUserList.get(i).setId(id);
			totalUserMapper.insertSelective(totalUser);
		}
		//同步更新人员
		for(int i=0;i<updateUserList.size();i++){
			TotalUser totalUser = new TotalUser();
			totalUser.setvId(updateUserList.get(i).getMemid());
			totalUser.setDatabaseName(dbName);
			totalUser.setMobile(updateUserList.get(i).getTelnum());
			totalUser.setName(updateUserList.get(i).getMembername());
			totalUser.setType("2");
			totalUser.setCompanyId(companyId);
			totalUserMapper.updateByPrimaryKeySelectives(totalUser);
		}
		
		//同步删除人员
		for(int i=0;i<deleteUserList.size();i++){
			TotalUser totalUser = new TotalUser();
			totalUser.setvId(deleteUserList.get(i).getMemid());
			totalUser.setStatus("9");
			totalUserMapper.updateByPrimaryKeySelectives(totalUser);
		}
		
		return true;
	}

	
	

	
	public VwtCustomer getVwtCustomerById(String id) {
		return vwtCustomerMapper.getCustomerById(id);
	}

	public Customer getCustomerById(String id) {
		return customerMapper.selectByPrimaryKey(id);
	}


	public boolean deleteByCorpId(String id) {
		if(organizationMapper.updateOrgByCompanyId(id)>0 && userMapper.updateUserByCompanyId(id)>0){
			return true;
		}else{
			return false;
		}
	}


	public boolean deleteTotalUserByCorpId(String id) {
		if(totalUserMapper.updateByCompanyId(id)>0){
			return true;
		}else{
			return false;
		}
	}


	@Override
	public boolean updateCustomer(Customer customer) {
		if(customerMapper.updateByPrimaryKeySelective(customer)>0){
			return true;
		}else{
			return false;
		}
	}


	@Override
	public String selectMaxtime(String ecode,String maxTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
		//获取最大时间的部门信息，有可能时间相同所以用list集合
		List<VwtDept> vwtDept =  vwtDeptMapper.selectBycorpId(ecode) ;
		//获取最大时间的删除部门信息
		List<VwtDeptHis> vwtDeptHis = vwtDeptHisMapper.selectBycorpId(ecode) ;
		//获取最大时间的人员新增信息
		List<VwtMemberInfo> vwtMemberInfos = vwtMemebrtInfoMapper.selectBycorpId(ecode) ;
		//获取最大时间的人员修改信息
		List<VwtMemberInfo> vwtMemberInfo = vwtMemebrtInfoMapper.selectBycorp(ecode) ;
		//获取最大时间的人员删除信息
		List<VwtMemberInfoHis> vwtMemberInfoHis = vwtMemeberInfoHisMapper.selectBycorpId(ecode) ;
		//设置初始值
		long maxactime = 0 ;
		long maxdeletetime = 0 ;
		long maxcreatime = 0 ;
		long maxupdatetime = 0 ;
		long maxmemberdeletetime = 0 ;
		//获取最大时间
		if (vwtDept.size()!=0) {
			maxactime = vwtDept.get(0).getActTime().getTime() ;
		}
		if(vwtDeptHis.size()!=0){
			maxdeletetime = vwtDeptHis.get(0).getDeleteTime().getTime() ;
		}
		if(vwtMemberInfos.size()!=0){
			maxcreatime = vwtMemberInfos.get(0).getCreattime().getTime() ;
		}
		if(vwtMemberInfoHis.size()!=0){
			maxmemberdeletetime = vwtMemberInfoHis.get(0).getDeleteTime().getTime() ;
		}
		if(vwtMemberInfo.size()!=0){
			maxupdatetime = vwtMemberInfo.get(0).getOperationTime().getTime() ;
		}
		//两两比较得到最大值
		long maxdepettime = Math.max(maxactime, maxdeletetime) ;
		long maxmembertime = Math.max(maxcreatime, maxmemberdeletetime) ;
		long maxdate = Math.max(maxdepettime, maxmembertime) ;
		long maxtime = Math.max(maxdate, maxupdatetime) ;
		maxTime = sdf.format(maxtime) ;
		return maxTime ;
	}

	

	
	
	
}
