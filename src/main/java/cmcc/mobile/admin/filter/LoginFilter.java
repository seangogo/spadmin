package cmcc.mobile.admin.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 登录过滤器
 * 
 * @author zhuzy
 *
 */
public class LoginFilter extends OncePerRequestFilter {

	private Logger logger = Logger.getLogger(this.getClass());

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String[] notFilter = new String[] {"/checkPass","/checkLogin","/login","workflow","/file/download"};

		String uri = request.getRequestURI();

		if (uri.indexOf(".htm") != -1 || uri.indexOf(".do") != -1) {
			boolean doFilter = true;
			for (String s : notFilter) {
				if (uri.indexOf(s) != -1) {
					doFilter = false;
					break;
				}
			}
			if (doFilter) {
				Object object = request.getSession().getAttribute("user");
				if (null == object) {
					response.sendRedirect(request.getContextPath() + "/login.htm?msg=301");
				} else {
					filterChain.doFilter(request, response);
				}
			} else {
				filterChain.doFilter(request, response);
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}

}
