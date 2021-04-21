package com.cos.reflect.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cos.reflect.anno.RequestMapping;
import com.cos.reflect.controller.UserController;

//목적 : 분기(라우터의 역할)
public class Dispatcher implements Filter{

	private boolean isMatching = false;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		
		//System.out.println("컨텍스트패스" + request.getContextPath());	// 프로젝트 시작주소
		//System.out.println("식별자주소" + request.getRequestURI());	// 끝주소
		//System.out.println("전체주소" + request.getRequestURL()); //전체 주소
		
		// /user 파싱하기
		String endPoint = request.getRequestURI().replaceAll(request.getContextPath(), "");
		System.out.println("엔드포인트" + endPoint);
		
		// 리플렉션 -> 메서드를 런타임 시점에서 찾아내서 실행
		UserController  userController = new UserController();
		Method[] methods = userController.getClass().getDeclaredMethods();
		
		for (Method method : methods) {
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			RequestMapping requestMapping = (RequestMapping) annotation;
			if(requestMapping.value().equals(endPoint)) {
				isMatching = true;
				try {
					Parameter[] params = method.getParameters();
					String path = null;
					if(params.length != 0) {
						// 언제는 logindto일거고 또 언제는 joindto 일거니까 Object
						// 0 주는 이유는 , dto 쓰니까 
						Object dtoInstance = params[0].getType().newInstance();
						
						//기본 자료형이면 리턴 받아야겠지만, 제네릭은 참조형인거같음.
						setData(dtoInstance, request);
						
						// invoke(메소드를 호출할 객체, 전달 할 파라미터)
						path = (String)method.invoke(userController, dtoInstance);
					}else {
						path =(String)method.invoke(userController);
					}

					// 리퀘스트디스패처는 내부실행이라 필터를 다시 타지 않는다
					// 내부에서 / 를 찾기때문에 인덱스파일로 가진다
					RequestDispatcher dis = request.getRequestDispatcher(path);
					dis.forward(request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			} 
		}
		if(isMatching == false) {
			System.out.println("요청주소없음");
			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.println("잘못된 주소 요청입니다. 404 에러");
			out.flush();
		}
	}
	
	//오브젝트보다는 제네릭으로 선언하는게 편하지
	private <T> void setData(T instance, HttpServletRequest request) {
		Enumeration<String> keys = request.getParameterNames(); // 로그인의 경우 크기 : 2(username, password)
		
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String methodKey = keyToMethodKey(key);
			
			Method[] methods = instance.getClass().getDeclaredMethods();
			
			for (Method method : methods) {
				if(method.getName().equals(methodKey)) {
					try {
						method.invoke(instance, request.getParameter(key));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			
		}
	}
	
	private String keyToMethodKey(String key) {
		String firstKey = "set";
		String upperKey = key.substring(0,1).toUpperCase();
		String remainKey = key.substring(1);
		
		return firstKey + upperKey + remainKey;
	}
}
