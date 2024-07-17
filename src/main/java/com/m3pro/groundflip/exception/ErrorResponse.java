package com.m3pro.groundflip.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorResponse {
	String result;

	String message;

	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.result("error")
			.message(errorCode.getMessage())
			.build();
	}
}

