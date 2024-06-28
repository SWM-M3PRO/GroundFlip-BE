package com.m3pro.groundflip.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
	DUPLICATED_USER(HttpStatus.BAD_REQUEST, "중복된 회원입니다."),
	PIXEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 픽셀입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러 입니다");

	private final HttpStatus httpStatus;
	private final String message;
}
