package com.cos.reflect.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		
		UserController  userController = new UserController();
		if(endPoint.equals("/join")) {
			userController.join();
		} else if(endPoint.equals("/login")) {
			userController.login();
		}
	
	}
}
