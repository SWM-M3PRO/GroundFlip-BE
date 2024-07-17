package com.m3pro.groundflip.config;

import java.util.UUID;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerInterceptor implements HandlerInterceptor {
	private static final String LOG_ID = "logId";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {
		String requestUri = request.getRequestURI();
		String uuid = UUID.randomUUID().toString().substring(0, 5);

		request.setAttribute(LOG_ID, uuid);

		log.info("REQUEST [{}] [{}] [{}] [{}]", uuid, requestUri, request.getMethod(), handler);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
		Exception ex) throws Exception {
		String requestUri = request.getRequestURI();
		String logId = (String)request.getAttribute(LOG_ID);

		log.info("RESPONSE [{}] [{}] [{}] [{}]", logId, requestUri, response.getStatus(), handler);
	}
}
