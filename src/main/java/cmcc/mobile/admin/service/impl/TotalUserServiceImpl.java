package cmcc.mobile.admin.service.impl;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.dao.TotalUserMapper;
import cmcc.mobile.admin.entity.TotalUser;
import cmcc.mobile.admin.service.TotalUserService;

@Service
public class TotalUserServiceImpl implements TotalUserService{

	
	@Autowired
	private TotalUserMapper totalUserMapper;
	
	
	/**
	 * 删除用户
	 */
	public JsonResult deleteUser(HttpServletRequest request, String userId) {
		JsonResult json = new JsonResult();
		TotalUser totalUser = new TotalUser();
		totalUser = totalUserMapper.getTotalUserById(userId);
		if(totalUser == null){
			json.setSuccess(false);
			json.setMessage("人员信息不存在");
			return json;
		}
		totalUser.setStatus("9");
		
		if(totalUserMapper.updateByPrimaryKeySelective(totalUser)>0){
			json.setSuccess(true);
		}else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		
		return json;
	}



	/**
	 * 增加用户
	 */
	public JsonResult addUser(TotalUser totalUser) {
		JsonResult json = new JsonResult();
		if(totalUserMapper.insertSelective(totalUser)>0){
			json.setSuccess(true);
		}else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		
		return json;
	}


	/**
	 * 更新用户
	 */
	public JsonResult updateUser(HttpServletRequest request, TotalUser totalUser) {
		
		JsonResult json = new JsonResult();
		if(totalUserMapper.updateByPrimaryKeySelective(totalUser)>0){
			json.setSuccess(true);
		}else{
			json.setSuccess(false);
			json.setMessage("服务器异常");
		}
		
		return json;
	}
	
}
