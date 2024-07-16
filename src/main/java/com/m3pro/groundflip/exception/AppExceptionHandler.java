package com.m3pro.groundflip.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class AppExceptionHandler {
	private static final String LOG_ID = "logId";

	@ExceptionHandler(AppException.class)
	ResponseEntity<ErrorResponse> handleAppException(AppException appException, HttpServletRequest request) {
		log.info("EXCEPTION [{}] [{}] [{}] [{}]", request.getAttribute(LOG_ID), request.getRequestURI(),
			request.getMethod(), appException.getErrorCode().getMessage());

		return ResponseEntity.status(appException.getErrorCode().getHttpStatus())
			.body(ErrorResponse.of(appException.getErrorCode()));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
