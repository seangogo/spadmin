package cmcc.mobile.admin.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author renlinggao
 * @Date 2016年7月3日
 */
public class HttpClientUtil {
	public static final int CACHE = 10 * 1024;

	public static File downFile(String url, String filePath) throws UnsupportedOperationException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = client.execute(httpget);
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();

		File file = new File(filePath);
		FileOutputStream fileout = new FileOutputStream(file);
		byte[] buffer = new byte[CACHE];
		int ch = 0;
		while ((ch = is.read(buffer)) != -1) {
			fileout.write(buffer, 0, ch);
		}
		is.close();
		fileout.flush();
		fileout.close();
		return file;
	}
}
