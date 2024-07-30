package com.m3pro.groundflip.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

@RestController
public class TestController {

	@GetMapping("/check")
	@ResponseStatus(HttpStatus.OK)
	public Response<String> test() {
		return Response.createSuccess("success");
	}

	@GetMapping("/exception")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Response<String> exception() {
		throw new AppException(ErrorCode.DUPLICATED_NICKNAME);
	}
}
