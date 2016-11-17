package cmcc.mobile.admin.service;

import javax.servlet.http.HttpServletRequest;

import cmcc.mobile.admin.base.JsonResult;


/**
 * 服务于FileController
 * @author shaoweiwei
 *
 */
public interface HyFileService {
	
	JsonResult importData(Long taskId,String id,HttpServletRequest request, JsonResult result);
	
}
