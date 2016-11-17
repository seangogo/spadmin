package cmcc.mobile.admin.util;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author renlinggao
 * @Date 2016年7月7日
 */
public class StringUtil {

	/**
	 * 是否是手机号
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isMobile(String str) {
		String regex = "^[1][3,4,5,7,8][0-9]{9}$";
		return StringUtils.isNotEmpty(str) && str.matches(regex);
	}
}
