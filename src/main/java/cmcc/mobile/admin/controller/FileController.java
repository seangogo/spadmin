package cmcc.mobile.admin.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cmcc.mobile.admin.base.BaseController;
import cmcc.mobile.admin.base.JsonResult;
import cmcc.mobile.admin.service.FileService;
import cmcc.mobile.admin.util.ImageUtil;
import cmcc.mobile.admin.util.PropertiesUtil;
import net.mikesu.fastdfs.FastdfsClientFactory;

/**
 *
 * @author renlinggao
 * @Date 2016年5月30日
 */
@Controller
@RequestMapping("file")
public class FileController extends BaseController {
	@Autowired
	FileService fileService;

	@RequestMapping("imageUpload")
	@ResponseBody
	public JsonResult imageUpload(MultipartHttpServletRequest mr, HttpServletRequest request,
			HttpServletResponse response, String userId, String companyId, String num) {
		if (userId == null)
			userId = getUser().getId();
		if (companyId == null)
			companyId = getCompany().getId();
		JsonResult result = new JsonResult();
		// 保存上传的图片
		cmcc.mobile.admin.entity.File file = fileService.fileUpload(mr, request, num, userId, companyId);
		result.setSuccess(true);
		result.setModel(file);
		return result;
	}

	/**
	 * 单文件上传
	 * 
	 * @param mr
	 * @param request
	 * @param response
	 * @param userId
	 * @param companyId
	 * @param num
	 * @return
	 */
	@RequestMapping("fileUpload/{num}/{companyId}/{userId}")
	@ResponseBody
	public JsonResult fileUpload(MultipartHttpServletRequest mr, HttpServletRequest request,
			HttpServletResponse response, @PathVariable String userId, @PathVariable String companyId,
			@PathVariable String num) {
		JsonResult result = new JsonResult();
		cmcc.mobile.admin.entity.File file = fileService.fileUpload(mr, request, num, userId, companyId);
		result.setSuccess(true);
		result.setModel(file);
		return result;
	}

	/**
	 * 文件下载
	 * 
	 * @param response
	 * @param id
	 * @return
	 */
	@RequestMapping("download/{id}/{name}")
	@ResponseBody
	public File download(HttpServletRequest request, HttpServletResponse response, @PathVariable int id,
			@PathVariable String name) {
		response.setCharacterEncoding("utf-8");
		response.setContentType("multipart/form-data");

		cmcc.mobile.admin.entity.File hyFile = fileService.getById(id);
		if (hyFile == null || (!hyFile.getAddr().equals(name) && !(("thum_" + hyFile.getAddr()).equals(name)))) {
			throw new RuntimeException("没有找到该文件");
		}
		try {
			response.setHeader("Content-Disposition",
					"attachment; filename=" + new String(hyFile.getName().getBytes("utf-8"), "ISO-8859-1"));
		} catch (UnsupportedEncodingException e1) {
			log.error(e1.getMessage());
		}
		File file = null;
		try {
			file = fileService.download(response, name);
			InputStream inputStream = new FileInputStream(file);

			OutputStream os = response.getOutputStream();
			byte[] b = new byte[2048];
			int length;
			while ((length = inputStream.read(b)) > 0) {
				os.write(b, 0, length);
			}
			// 这里主要关闭。
			os.flush();
			os.close();
			inputStream.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
		return null;
	}

	/**
	 * 多文件上传
	 * 
	 * @param mr
	 * @param request
	 * @param response
	 * @param userId
	 * @param companyId
	 * @param num
	 * @return
	 */
	@RequestMapping("fileUploads/{num}/{companyId}/{userId}")
	@ResponseBody
	public JsonResult fileUploads(MultipartHttpServletRequest mr, HttpServletRequest request,
			HttpServletResponse response, @PathVariable String userId, @PathVariable String companyId,
			@PathVariable String num) {
		List<cmcc.mobile.admin.entity.File> files;
		JsonResult result = new JsonResult();
		try {
			files = fileService.multipleFilesUpload(request, response, num, userId, companyId);
			result.setSuccess(true);
			result.setModel(files);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}
	
	/**
	 * 
	 * 删除附件
	 */
	@RequestMapping("fileDelete/{id}/{addr}")
	@ResponseBody
	public JsonResult fileDetele(HttpServletRequest request, HttpServletResponse response, @PathVariable int id,
			@PathVariable String addr) {
		JsonResult result = new JsonResult(true, null, null);
		fileService.delteFile(id, addr);
		return result;
	}
}
