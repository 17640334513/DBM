package action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uf.UFMap;
import util.JsonUtil;
import util.LogUtil;
import util.ThreadUtil;

public class BaseAction {			
	
	public static ServletContext application;
	
	public static HttpServletRequest getRequest(){
		return (HttpServletRequest) ThreadUtil.get(ThreadUtil.Request);
	}
	public static HttpServletResponse getResponse(){
		return (HttpServletResponse) ThreadUtil.get(ThreadUtil.Response);
	}
	public static HttpSession getSession(){
		return getRequest().getSession();
	}
	
	/**
	 * 组装成功时的返回信息
	 * @param datas : 返回数据，按data、data1、data2...依次命名
	 * */
	public static UFMap right(Object...datas){
		UFMap returnMap = new UFMap();
		returnMap.put("code", "right");
		if(datas.length>0){
			returnMap.put("data", datas[0]);
			for(int i=1;i<datas.length;i++){
				returnMap.put("data"+i, datas[i]);
			}
		}
		return returnMap;
	}
	/**
	 * 组装失败时的返回信息
	 * @param msg : 错误描述
	 * @param datas : 返回数据，按data、data1、data2...依次命名
	 * */
	public static UFMap wrong(String msg,Object...datas){
		UFMap returnMap = new UFMap();
		returnMap.put("code", "wrong");
		returnMap.put("msg", msg);
		if(datas.length>0){
			returnMap.put("data", datas[0]);
			for(int i=1;i<datas.length;i++){
				returnMap.put("data"+i, datas[i]);
			}
		}
		return returnMap;
	}
	
	/**
	 * 获取转发实例
	 * @param url : 从工程名之后开始写，如：/html/home.html
	 * */
	public static Dispatcher dispatcher(String url){
		return new Dispatcher(url);
	}
	/**
	 * 获取重定向实例
	 * @param url : 从工程名之后开始写，如：/login.html；或写完整路径，如：https://www.baidu.com
	 * */
	public static Redirect redirect(String url){
		return new Redirect(url);
	}
	
	/** 接收http协议发来的报文 */
	public static String getMsg() throws Exception{
		try (InputStream is = getRequest().getInputStream(); InputStreamReader isr = new InputStreamReader(is,"UTF-8"); BufferedReader br = new BufferedReader(isr)) {
			String line = null;
	        StringBuilder sb = new StringBuilder();
	        while ((line = br.readLine()) != null) {
	        	sb.append(line);
	        }
	        String msg = URLDecoder.decode(sb.toString(), "UTF-8");
	        LogUtil.print("收到POST报文："+msg,2);
			return msg;
		}		
	}
	
	/**
	 * 返回给客户端成功信息（只有用return达不到效果时才用这个方法，一帮情况下要用return）
	 * @param datas : 返回数据，按data、data1、data2...依次命名
	 * */
	public static void returnRight(Object...datas){
		UFMap resultMap = new UFMap();
		resultMap.put("code", "right");
		if(datas.length>0){
			resultMap.put("data", datas[0]);
			for(int i=1;i<datas.length;i++){
				resultMap.put("data"+i, datas[i]);
			}
		}
		returnObj(resultMap);
	}
	/**
	 * 返回给客户端错误信息（只有用return达不到效果时才用这个方法，一帮情况下要用return）
	 * @param msg : 错误描述
	 * @param datas : 返回数据，按data、data1、data2...依次命名
	 * */
	public static void returnWrong(String msg,Object...datas){
		UFMap resultMap = new UFMap();
		resultMap.put("code", "wrong");
		resultMap.put("msg", msg);
		if(datas.length>0){
			resultMap.put("data", datas[0]);
			for(int i=1;i<datas.length;i++){
				resultMap.put("data"+i, datas[i]);
			}
		}
		returnObj(resultMap);
	}	
	/**
	 * （只有用return达不到效果时才用这个方法，一帮情况下要用return）
	 * 返回给客户端obj 
	 * */
	public static void returnObj(Object obj){	
		if(obj!=null){
			String json=null;			
			try {	
				if(obj instanceof Map||obj instanceof List||obj.getClass().isArray()){
					json=JsonUtil.toJson(obj);
				}else{
					json=obj.toString();
				}
				getResponse().getWriter().write(json);
				LogUtil.print("返回给客户端："+json, BaseAction.class);											
			} catch(Exception e) {
				LogUtil.print(e);
			}
		}		
	}
	
	/**
	 * 重定向（只有用return达不到效果时才用这个方法，一帮情况下要用return）<br>
	 * 本工程跳转：从工程名之后开始写，如：/jsp/login.jsp，写绝对路径也行<br>
	 * 向外跳转：写绝对路径<br>
	 * */
	public static void skip(String path){
		try {
			if(path.startsWith("/")){
				path=getRequest().getContextPath()+path;
				LogUtil.print("重定向到："+path,2);
				getResponse().sendRedirect(path);
			}else{
				LogUtil.print("重定向到："+path,2);
				getResponse().sendRedirect(path);				
			}
		} catch (IOException e) {
			LogUtil.print(e);
		}
	}	
	/**
	 * （只有用return达不到效果时才用这个方法，一帮情况下要用return）<br>
	 * (转发)访问本工程页面，访问后地址栏为原servlet地址<br>
	 * 路径参数：从工程名之后开始写，如：/jsp/login.jsp<br>
	 * */
	public static void visit(String path){		
		try {
			HttpServletRequest request = getRequest();
			LogUtil.print("请求转发到："+path,2);
			RequestDispatcher rd = request.getRequestDispatcher(path); //定向的页面  
			rd.forward(request, getResponse());
		} catch (Exception e) {
			LogUtil.print(e);
		}
	}
	
	/** 获取客户端IP */
	public static String getClientIP() { 
		HttpServletRequest request = getRequest();
	    String ip = request.getHeader("x-forwarded-for"); 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	    	ip = request.getHeader("Proxy-Client-IP"); 
	    	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	    		ip = request.getHeader("WL-Proxy-Client-IP");
	    		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	    			ip = request.getHeader("HTTP_CLIENT_IP");
	    			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	    				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
	    				if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	    					ip = request.getRemoteAddr(); 
	    				}
	    			}
	    		}
		    }
	    } 
	    return ip; 
	}
	
	/**获取指定cookie值*/
	public static String getCookie(String key) {
		Cookie[] cookies = getRequest().getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				if(key.equals(cookie.getName())) {
					return cookie.getValue();										
				}
			}
		}
		return null;
	}
	
	/**增加cookie（有效期10年）*/
	public static void addCookie(String key, String value) {
		addCookie(key, value, 10*365*24*60*60);
	}
	/**
	 * 增加cookie
	 * @param expiry: 有效期（秒），0-立即失效；-1-本次会话结束后失效
	 * */
	public static void addCookie(String key, String value, int expiry) {
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(expiry);
		cookie.setPath("/");
		getResponse().addCookie(cookie);
	}
	
	/**修改cookie（先判断cookie是否存在，存在才改，有效期10年）*/
	public static void modCookie(String key, String newValue) {
		modCookie(key, newValue, 10*365*24*60*60);
	}
	/**
	 * 修改cookie（先判断cookie是否存在，存在才改）
	 * @param expiry: 有效期（秒），0-立即失效；-1-本次会话结束后失效
	 * */
	public static void modCookie(String key, String newValue, int expiry) {
		if(getCookie(key) != null) {
			addCookie(key, newValue, expiry);
		}
	}
	
	/**删除cookie*/
	public static void delCookie(String key) {
		addCookie(key, "", 0);
	}
	
}
