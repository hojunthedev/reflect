package com.cos.reflect.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
//		for (Method method : methods) {
//			if(endPoint.equals("/"+method.getName())) {
//				try {
//					method.invoke(userController);
//				} catch (Exception e) {
//					e.printStackTrace();
//				} 
//			}
//		}
	
		for (Method method : methods) {
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			RequestMapping requestMapping = (RequestMapping) annotation;
			System.out.println(requestMapping.value());
			if(requestMapping.value().equals(endPoint)) {
				try {
					String path =(String)method.invoke(userController);
					
					// 리퀘스트디스패처는 내부실행이라 필터를 타지 않는다.(?) 
					// 내부에서 / 를 찾기때문에 인덱스파일로 가진다
					RequestDispatcher dis = request.getRequestDispatcher(path);
					dis.forward(request, response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
		
	}
}
