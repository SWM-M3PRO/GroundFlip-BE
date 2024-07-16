package com.m3pro.groundflip.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
	DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "중복된 닉네임입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	PIXEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 픽셀입니다."),
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이미지입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러 입니다"),

	// 권한 관련 에러
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없습니다"),

	// JWT 관련 에러
	JWT_NOT_EXISTS(HttpStatus.UNAUTHORIZED, "요청에 JWT가 존재하지 않습니다."),
	INVALID_JWT(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT입니다."),
	JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT가 만료되었습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
