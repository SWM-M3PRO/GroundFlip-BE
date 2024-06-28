package com.m3pro.groundflip.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class AppExceptionHandler {
	@ExceptionHandler(AppException.class)
	ResponseEntity<ErrorResponse> handleAppException(AppException appException, HttpServletRequest request) {
		return ResponseEntity
			.status(appException.getErrorCode().getHttpStatus())
			.body(ErrorResponse.of(appException.getErrorCode()));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
