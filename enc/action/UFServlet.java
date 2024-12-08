package action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uf.UF;
import uf.UFMap;
import util.JsonUtil;
import util.LogUtil;
import util.ThreadUtil;

@SuppressWarnings("serial")
@WebServlet({ "*.i", "*.o" })
public class UFServlet extends HttpServlet {

	public static int requestCount = 0;
	private static ConcurrentMap<String, Method> actionMap = new ConcurrentHashMap<String, Method>();

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		requestCount++;
		long startTime = System.currentTimeMillis();
		String uri = request.getRequestURI();// 获取请求的网址（不带参数）
		boolean isPrint = request.getParameter("UfPrint") == null;
		String logId = ThreadUtil.getLogId();
		try {
			if(isPrint) LogUtil.print("被请求的路径：" + uri, logId + ":");
			Enumeration<String> e = request.getParameterNames();
			UFMap map = null;// 把客户端发来的参数都放在map里
			if (e.hasMoreElements()) {
				map = new UFMap();
				do {					
					String paramName = e.nextElement();
					String paramValue = request.getParameter(paramName);
					map.put(paramName, paramValue);
				} while (e.hasMoreElements());
				if(isPrint) LogUtil.print("请求参数：" + map, logId + ":");
			}
			if (BaseAction.application == null) BaseAction.application = getServletContext();
			request.setCharacterEncoding(UF.UTF8);
			response.setContentType(UF.CONTENT_TYPE);
			ThreadUtil.set(ThreadUtil.Request, request);
			ThreadUtil.set(ThreadUtil.Response, response);
			Method method = actionMap.get(uri);
			if (method == null) {
				int slashIndex = uri.lastIndexOf("/");
				String actionName = uri.substring(uri.substring(0, slashIndex).lastIndexOf("/") + 1, slashIndex) + "Action";// 获取action名称
				String methodName = uri.substring(slashIndex + 1);
				Class<?> actionClass = null;
				if (uri.endsWith(".i")) {
					actionClass = Class.forName("action.in." + actionName);
				} else {
					actionClass = Class.forName("action.out." + actionName);
				}
				methodName = methodName.substring(0, methodName.indexOf("."));// 得到方法名
				for (Method m : actionClass.getDeclaredMethods()) {
					if (m.getName().equals(methodName)) {
						method = m;
						break;
					}
				}
				method.setAccessible(true);// 放开反射调用方法的权限
				actionMap.put(uri, method);
			}
			Object ret = method.getParameterCount() == 0 ? method.invoke(null) : method.invoke(null, map);
			if (ret != null) {
				if (ret instanceof Dispatcher) {
					String url = ((Dispatcher) ret).getUrl();
					if(isPrint) LogUtil.print("请求转发到：" + url, logId + " " + uri + ":");
					RequestDispatcher rd = request.getRequestDispatcher(url); // 定向的页面
					rd.forward(request, response);
				} else if (ret instanceof Redirect) {
					String url = ((Redirect) ret).getUrl();
					if (url.startsWith("/")) {
						url = request.getContextPath() + url;
						if(isPrint) LogUtil.print("重定向到：" + url, logId + " " + uri + ":");
						response.sendRedirect(url);
					} else {
						if(isPrint) LogUtil.print("重定向到：" + url, logId + " " + uri + ":");
						response.sendRedirect(url);
					}
				} else {
					String json = null;
					if (ret instanceof Map || ret instanceof List || ret.getClass().isArray()) {
						json = JsonUtil.toJson(ret);
					} else {
						json = ret.toString();
					}
					response.getWriter().write(json);//这个流不用手动关，会在回收request时自动关
					if(json.length() > 1000) {
						json = json.substring(0, 1000) + "...(总长度：" + json.length() + "，过长，日志只打印前1000位)";
					}
					if(isPrint) LogUtil.print("返回给客户端：" + json, logId + " " + uri + ":");
				}
			}
		} catch (InvocationTargetException ite) {
			LogUtil.print(ite.getCause());
		} catch (Exception e) {
			LogUtil.print(e);
		} finally {
			if(isPrint) LogUtil.print("本次请求消耗时间：" + (System.currentTimeMillis() - startTime) + " 毫秒", logId + " " + uri + ":");
			ThreadUtil.clear();
			requestCount--;
		}
	}
	
}
