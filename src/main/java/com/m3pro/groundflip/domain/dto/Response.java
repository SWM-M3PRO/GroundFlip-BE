package com.m3pro.groundflip.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
	private static final String SUCCESS_RESULT = "success";
	private static final String ERROR_RESULT = "error";

	private String result;
	private String message;
	private T data;

	public static <T> Response<T> createSuccess(T data) {
		return new Response<>(SUCCESS_RESULT, null, data);
	}

	public static Response<?> createSuccessWithNoData() {
		return new Response<>(SUCCESS_RESULT, null, null);
	}

	public static Response<?> createSuccessWithMessage(String message) {
		return new Response<>(SUCCESS_RESULT, message, null);
	}

	public static Response<?> createError(String message) {
		return new Response<>(ERROR_RESULT, message, null);
	}
}
