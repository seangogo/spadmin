package cmcc.mobile.admin.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.ApprovalAuthorityMapper;
import cmcc.mobile.admin.entity.ApprovalAuthority;
import cmcc.mobile.admin.server.db.MultipleDataSource;
import cmcc.mobile.admin.service.AuthorityService;

/**
 * 后台管理界面
 * @author wubj
 *
 */
@Controller
@RequestMapping("authority")
public class AuthorityController extends BaseController{
	@Autowired
	private AuthorityService authorityService ;
	@Autowired
	private ApprovalAuthorityMapper approvalAuthorityMapper ;
	//获取产品list
	@RequestMapping("/getWyyList")
	@ResponseBody
	public JsonResult getWyyList(HttpServletRequest request){
		String companyId = getCompany().getId() ;
		if(StringUtils.isNotEmpty(companyId)){
		return authorityService.getWyyList(companyId) ;
		}
		return new JsonResult(false,"非法请求",null) ;
	}
	
	
	//获取管理员list
	@RequestMapping("/getManager")
	@ResponseBody
	public JsonResult getManager(HttpServletRequest request){
		MultipleDataSource.setDataSourceKey("");
		String companyId = getCompany().getId() ; 
		String userId = getUser().getId() ;
		if(StringUtils.isNotEmpty(companyId)&&StringUtils.isNotEmpty(userId)){
			return authorityService.getManaher(companyId,userId) ;
		}
		return new JsonResult(false,"非法请求",null) ;
	} 
	
	//流程授权接口
	@RequestMapping("/getApproval")
	@ResponseBody
	public JsonResult getApproval(HttpServletRequest request,ApprovalAuthority authority){
		
		String companyId = getCompany().getId() ;
		String userId = getUser().getId() ;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;		
		ApprovalAuthority approval = approvalAuthorityMapper.selectByPrimaryKey(authority.getId()) ;
		if(StringUtils.isNotEmpty(companyId)&&StringUtils.isNotEmpty(userId)){
			if(approval==null){
				authority.setCreateTime(sdf.format(new Date()));
				authority.setCreateUserId(userId);
				return authorityService.getApproval(authority) ;
			}else{
				authority.setUpdateTime(sdf.format(new Date()));
				authority.setUpdateUserId(userId);
				return authorityService.UpdateApproval(authority) ;
			}
		}
		return new JsonResult(false,"非法请求",null) ;
	}
	
	//获取已经授权的人员信息
	public JsonResult getPerson(HttpServletRequest request,String id){
		if(StringUtils.isNotEmpty(id)){
			return authorityService.getPerson(id) ;
		}
		return new JsonResult(false,"非法请求",null) ;
	}
}
