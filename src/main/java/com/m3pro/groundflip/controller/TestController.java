package com.m3pro.groundflip.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;

@RestController
public class TestController {

	@GetMapping("/")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public Response<String> test() {
		return Response.createSuccess("hello");
	}
}
