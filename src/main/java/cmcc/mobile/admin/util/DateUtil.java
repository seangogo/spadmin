package cmcc.mobile.admin.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author renlinggao
 * @Date 2016年6月28日
 */
public class DateUtil {
	private static final String FORMART_STYLE = "yyyy-MM-dd HH:mm:ss"; 
	
	private static SimpleDateFormat format = new SimpleDateFormat(FORMART_STYLE);
	
	public static String getDateStr(Date date){
		return format.format(date);
	}
}
