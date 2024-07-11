package com.m3pro.groundflip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openApi() {
		SecurityScheme bearerAuth = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("Authorization")
			.in(SecurityScheme.In.HEADER)
			.name(HttpHeaders.AUTHORIZATION);

		SecurityRequirement addSecurityItem = new SecurityRequirement();
		addSecurityItem.addList("Authorization");

		return new OpenAPI()
			.info(new Info()
				.title("Ground Flip API")
				.description("Ground Flip 의 api 문서입니다.")
				.version("1.0.0"))
			.components(new Components()
				.addSecuritySchemes("Authorization", bearerAuth
				));
		// .addSecurityItem(addSecurityItem);

	}
}
