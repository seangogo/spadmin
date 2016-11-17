package cmcc.mobile.admin.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.ActivitiLogMapper;
import cmcc.mobile.admin.dao.ActivitiRoleGroupMapper;
import cmcc.mobile.admin.dao.ActivitiRoleMapper;
import cmcc.mobile.admin.entity.ActivitiLog;
import cmcc.mobile.admin.entity.ActivitiRole;
import cmcc.mobile.admin.entity.ActivitiRoleGroup;
import cmcc.mobile.admin.entity.Organization;
import cmcc.mobile.admin.entity.User;
import cmcc.mobile.admin.service.ActivitiRoleService;
import cmcc.mobile.admin.vo.ActivitiRoleVo;
import cmcc.mobile.admin.vo.GroupVo;
import cmcc.mobile.admin.vo.UserVo;


@Service
public class ActivitiRoleServiceImpl implements ActivitiRoleService{
@Autowired
private ActivitiRoleMapper roleMapper ;
@Autowired
private ActivitiLogMapper logMapper ;
@Autowired
private ActivitiRoleGroupMapper roleGroupMapper ;
//新增角色
	@Override
	public JsonResult addRole(ActivitiRoleVo role) {
		JsonResult result = new JsonResult();
		List<ActivitiRole> list = roleMapper.selectCount(role);
		if(list!=null&&list.size()>0){			
			result.setMessage("该角色已经存在！");
			return result;
		}
		Date date = new Date() ;
		SimpleDateFormat sdf =   new SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " );
		if(role.getParentId()==null){
			role.setParentId(0);
		}
		role.setStatus(1) ;
		role.setCreateTime(sdf.format(date));
		roleMapper.insertSelective(role) ;
		ActivitiRole roles = roleMapper.selectByListId(role) ;		
		ActivitiLog log = new ActivitiLog() ;
		log.setRoleId(roles.getId().intValue());
		log.setRoleName(role.getRoleName()) ;
		log.setCompanyId(role.getCompanyId());
		log.setCreateTime(sdf.format(date)) ;
		log.setType(role.getType()) ;
		log.setParentId(role.getParentId());
		log.setCreateId(role.getCreateId());
		logMapper.insertSelective(log) ;
	
		return new JsonResult(true,"新增成功！",roles) ;
	}
//修改角色
	@Override
	public JsonResult editRole(ActivitiRoleVo role) {
		Date date = new Date() ;
		SimpleDateFormat sdf =   new SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " );
		int result = roleMapper.updateByPrimaryKeySelective(role) ;
		ActivitiRole roles = roleMapper.selectByPrimaryKey(role.getId()) ;
		ActivitiLog log = new ActivitiLog() ;
		log.setRoleId(roles.getId().intValue());
		log.setRoleName(roles.getRoleName()) ;
		log.setCompanyId(roles.getCompanyId());
		log.setCreateTime(sdf.format(date)) ;
		log.setType(roles.getType()) ;
		log.setParentId(role.getParentId());
		log.setCreateId(role.getCreateId());
		int ret = logMapper.insertSelective(log) ;
	
		if(result==1&&ret==1){
			return new JsonResult(true,"修改成功",null) ;
		}
		   return new JsonResult(false,"操作失败",null) ;
		
	}
//删除,启用,禁用角色
	@Override
	public JsonResult deteleRole(ActivitiRoleVo role) {
		Date date = new Date() ;
		SimpleDateFormat sdf =   new SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " );
		if(role.getStatus()==null){
			role.setStatus(0);
		}
		int result = roleMapper.updateByPrimaryKeySelective(role) ;
		ActivitiRole roles = roleMapper.selectByPrimaryKey(role.getId()) ;
		//把删除，启用，禁用的信息新增到log日志表中
		ActivitiLog log = new ActivitiLog() ;
		log.setRoleId(roles.getId().intValue());
		log.setRoleName(roles.getRoleName()) ;
		log.setCompanyId(roles.getCompanyId());
		log.setCreateTime(sdf.format(date)) ;
		log.setType(roles.getType()) ;
		log.setParentId(role.getParentId());
		log.setCreateId(role.getCreateId());
		int ret = logMapper.insertSelective(log) ;
		if(result==1&&ret==1){
			return new JsonResult(true,"操作成功",null) ;
		}
		   return new JsonResult(false,"操作失败",null) ;
	}
//查询角色list
	@Override
	public JsonResult roleList(ActivitiRoleVo role) {
		List<ActivitiRole> roles = roleMapper.selectByList(role) ;
		if(roles==null||roles.size()==0){
			return new JsonResult(false,"无数据",null) ;
		}
		return new JsonResult(true ,"查询成功",roles) ;
	}
//所有组织成员
@Override
public JsonResult getAllDepet(String companyId) {
	List<Map> org = roleMapper.selectCompanyInfoByCompanyId(companyId);
	List<Map> user = roleMapper.selectUserByCompanyId(companyId);
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("org", org) ;
	map.put("user", user) ;
	//org.addAll(user) ;	
	return new JsonResult(true,"查询成功！",map);

}

//删除角色/群组成员
public JsonResult deleteRolePerson(ActivitiRoleVo role) {
	Date date = new Date() ;
	SimpleDateFormat sdf =   new SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " );
//删除之前先查一下状态
	ActivitiRoleGroup roles = roleGroupMapper.selectByRoleId(role) ;
	if(roles==null){
		return new JsonResult(false ,"删除失败",null) ;
	}
	int result  = roleGroupMapper.deleteRolePerson(roles);
	//ActivitiRoleGroup roles = roleGroupMapper.selectByRoleId(role) ;
	ActivitiRole rol = roleMapper.selectByPrimaryKey(role.getId()) ;
	//把删除的信息新增到log日志表中
	ActivitiLog log = new ActivitiLog() ;
	log.setRoleId(roles.getRoleId());
	log.setUserId(roles.getUserId());
	log.setCreateTime(sdf.format(date)) ;
	log.setCompanyId(rol.getCompanyId());
	log.setRoleName(rol.getRoleName());
	log.setType(2) ;
	log.setParentId(role.getParentId());
	log.setCreateId(role.getCreateId());
	int ret = logMapper.insertSelective(log) ;
	if(result!=0&&ret==1){
		return new JsonResult(true , "删除成功" ,null) ;
	}
	return new JsonResult(false ,"删除失败",null) ;
}


//获取角色/群组的成员
@Override
public JsonResult rolePersonList(ActivitiRoleVo role) {
	List<UserVo> user = roleGroupMapper.selectByrolePersonList(role) ;
	if(user==null||user.size()==0){
		return new JsonResult(false,"无数据",null) ;
	}
	return new JsonResult(true ,"查询成功",user) ;
}

//activiti流程流转
@SuppressWarnings("unchecked")
@Override
public JsonResult getNextNode(String userId,String companyId,List<Map<String, Object>> list) {
	Map<String,Object> user = new HashMap<>() ;
	Map<String,Object> map2 = new HashMap<>() ;
	Map<String,Object> map3 = new HashMap<>() ;
	List<ActivitiRoleGroup> lists = new ArrayList<ActivitiRoleGroup>() ;
	//获取发起人所在的部门
	Organization org = roleMapper.selectByOrg(userId) ;
	if(org==null){
		return new JsonResult(false,"该用户没有组织信息,请联系管理员加入相应的组织",org) ;
	}
	String orgId = org.getId() ;
	for(int i=0 ; i<list.size();i++){
		map2 = list.get(i) ;
		 boolean isBoolean = (boolean) map2.get("isBelong") ;
		 if(isBoolean==false){
			 if(Integer.parseInt(map2.get("__groupsType").toString())==1){
					if(map2.get("candidateGroups")!=null){
					  List<GroupVo> groups = (List<GroupVo>) map2.get("candidateGroups") ;  		  
					  String[] arr = new String[groups.size()] ;
					  for(int y=0 ; y<groups.size() ;y++){
						  arr[y] = groups.get(y).getId() ;
					  }
					  user.put("roleId", arr) ;
					  lists = roleGroupMapper.selectByUserId(user) ;
					  if(lists==null){
							return new JsonResult(false,"没有下一环节审核人流程设置错误",null) ;
						}
					  map3.put("user", lists) ;
				  	  map3.put("Task",map2.get("id"));
				  	  return new JsonResult(true,"查询成功！",map3) ;
					}else if(map2.get("candidateUsers")!=null){
						  List<GroupVo> groups = (List<GroupVo>) map2.get("candidateUsers") ;  		  
						  String[] arr = new String[groups.size()] ;
						  for(int y=0 ; y<groups.size() ;y++){
							  arr[y] = groups.get(y).getId() ;
						  }
						user.put("userId", arr) ;
						lists = roleGroupMapper.selectByUserOrg(user) ;	
						if(lists==null){
							return new JsonResult(false,"没有下一环节审核人流程设置错误",null) ;
						}
						map3.put("user", lists) ;
					  	map3.put("Task",map2.get("id"));
					  	return new JsonResult(true,"查询成功！",map3) ;
					}
						  
				  }else if(Integer.parseInt(map2.get("__groupsType").toString())==0){
					  if(map2.get("candidateGroups")!=null){
						  List<GroupVo> groups = (List<GroupVo>) map2.get("candidateGroups") ;  		  
						  String[] arr = new String[groups.size()] ;
						  for(int y=0 ; y<groups.size() ;y++){
							  arr[y] = groups.get(y).getId() ;
						  }
						  user.put("roleId", arr) ;
						  user.put("orgId", orgId) ;
						  lists = roleGroupMapper.selectByUserIds(user) ;
						  if(lists==null){
								return new JsonResult(false,"没有下一环节审核人流程设置错误",null) ;
							}
						  map3.put("user", lists) ;
					  	  map3.put("Task",map2.get("id"));
					  	  return new JsonResult(true,"查询成功！",map3) ;
						}else if(map2.get("candidateUsers")!=null){
							  List<GroupVo> groups = (List<GroupVo>) map2.get("candidateUsers") ;  		  
							  String[] arr = new String[groups.size()] ;
							  for(int y=0 ; y<groups.size() ;y++){
								  arr[y] = groups.get(y).getId() ;
							  }
							user.put("userId", arr) ;
							//user.put("orgId", orgId) ;
							lists = roleGroupMapper.selectByUserOrg(user) ;	
							if(lists==null){
								return new JsonResult(false,"没有下一环节审核人流程设置错误",null) ;
							}
							map3.put("user", lists) ;
						  	map3.put("Task",map2.get("id"));
						  	return new JsonResult(true,"查询成功！",map3) ;
						}
				  }
				
				  	  
			 
		 }else{
			 String uId = map2.get("userId").toString() ;
			 lists = roleGroupMapper.selectByUID(uId) ;
			 map3.put("Task",map2.get("id")) ;
			 map3.put("users", lists) ;
			 return new JsonResult(true,"查询成功！",map3) ;
		 }
		
	}
	
	 return new JsonResult(true,"流程选择错误！",null) ;
}

//批量新增角色/群组成员（用数组接受userId,然后将参数封装在map中）
@Override
public JsonResult addRolePerson(String roleId, String[] userId) {
	
	Date date = new Date() ;
	SimpleDateFormat sdf =   new SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " );
	 Map<String, Object> map = new HashMap<>();
	    map.put("userId", userId);
	    map.put("status",1) ;
	    map.put("roleId", roleId);
	    map.put("createTime", sdf.format(date)) ;
	    int count = roleGroupMapper.selectByPersons(map) ;
	    if(count!=0){
	    	return new JsonResult(false,"该成员已经存在！",null) ;
	    }
	    int a = roleGroupMapper.addGroup(map);
	    if(a==0){
	    	return new JsonResult(false ,"操作失败!",null) ;
	    }
	return new JsonResult(true ,"操作成功!",null) ;
}

}
