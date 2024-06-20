package com.m3pro.groundflip.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
	DUPLICATED_USER(HttpStatus.BAD_REQUEST, "중복된 회원입니다");

	private final HttpStatus httpStatus;
	private final String message;
}
