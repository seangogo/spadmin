package cmcc.mobile.admin.service.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.AdminUserMapper;
import cmcc.mobile.admin.dao.ApprovalAuthorityMapper;
import cmcc.mobile.admin.dao.ApprovalWyyCompanyMapper;
import cmcc.mobile.admin.dao.UserMapper;
import cmcc.mobile.admin.entity.AdminUser;
import cmcc.mobile.admin.entity.ApprovalAuthority;
import cmcc.mobile.admin.entity.ApprovalWyyCompany;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.service.AuthorityService;


@Service
public class AuthorityServiceImpl implements AuthorityService{
	@Autowired
	private ApprovalWyyCompanyMapper wyyCompanyMapper ;
	@Autowired
	private AdminUserMapper userMapper ;
	@Autowired
	private ApprovalAuthorityMapper approvalAuthorityMapper ;
	@Autowired
	private UserMapper usermapper ;
	/**
	 * 获取该公司所有产品
	 * @param companyId
	 * @return
	 */
	@Override
	public JsonResult getWyyList(String companyId) {
		List<ApprovalWyyCompany> wyyCompany = wyyCompanyMapper.selectByCompanyId(companyId) ;
		if(wyyCompany.size()==0){
			return new JsonResult(false,"没有产品请联系管理员",null) ;
		}
		return new JsonResult(true,"操作成功",wyyCompany) ;
	}

	//获取管理员
	@Override
	public JsonResult getManaher(String companyId,String userId) {
		Map<String,Object> map = new HashMap<>() ;
		map.put("companyId", companyId) ;
		map.put("userId", userId) ;
		List<AdminUser> list = userMapper.selectByCompanyUser(map) ;
		return new JsonResult(true,"操作成功！",list);
	}
	//授权新增接口
	@Override
	public JsonResult getApproval(ApprovalAuthority authority) {			
		int ret = approvalAuthorityMapper.insertSelective(authority) ;
		if(ret==1){
			return new JsonResult(true,"授权成功！",null) ;
		}
		return new JsonResult(false,"授权失败",null);
	}
	
	//修改授权接口
	@Override
	public JsonResult UpdateApproval(ApprovalAuthority authority) {
		int ret = approvalAuthorityMapper.updateByPrimaryKeySelective(authority) ;
		if(ret==1){
			return new JsonResult(true,"授权成功！",null) ;
		}
		return new JsonResult(false,"授权失败",null);
	}

	//获取授权人员接口
	@Override
	public JsonResult getPerson(String id) {
		Map<String,Object> map = new HashMap<>() ;
		ApprovalAuthority approvalAuthoritie = approvalAuthorityMapper.selectByPrimaryKey(id) ;
		if(approvalAuthoritie.getUserids()!=null){
			String[] userId = approvalAuthoritie.getUserids().split(",") ;
			map.put("userId", userId) ;
			List<User> list = usermapper.selectUser(map) ;
			return new JsonResult(true,"查询成功！",list) ;
		}
		return new JsonResult(false,"该流程没有授权人",null) ;
	}
	

}
