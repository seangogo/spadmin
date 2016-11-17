package cmcc.mobile.admin.util;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cmcc.mobile.admin.entity.File;

/**
 *
 * @author renlinggao
 * @Date 2016年6月29日
 */
public class FileUtil {
	private static final String DEFAULT_PATH = "";
	
	public static File getUploadFile(HttpServletRequest request){
		//获取项目地址
		MultipartHttpServletRequest mr = (MultipartHttpServletRequest)request;
		for (Iterator<MultipartFile> ite = mr.getFileMap().values().iterator(); ite.hasNext();) {
			MultipartFile multipartFile = ite.next();
			if (multipartFile.getSize() > 0) {
				// 获取文件名
				String fileName = multipartFile.getOriginalFilename();
				// 获取文件后缀名
				String extension = FilenameUtils.getExtension(fileName);

			}

		}
		return null;
	}
}
