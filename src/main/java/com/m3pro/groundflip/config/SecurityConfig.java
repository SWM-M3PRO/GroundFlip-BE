package com.m3pro.groundflip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.m3pro.groundflip.jwt.JwtAuthorizationFilter;
import com.m3pro.groundflip.jwt.JwtExceptionFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthorizationFilter jwtAuthorizationFilter;

	@Bean
	protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement((sessionManagement) ->
				sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests((authorizeHttpRequests) ->
				authorizeHttpRequests
					.requestMatchers("/api/auth/**").permitAll()
					.requestMatchers("/api/version").permitAll()
					.requestMatchers("/v3/api-docs/**").permitAll()
					.requestMatchers("/api/swagger-ui/**").permitAll()
					.requestMatchers("/api/docs/**").permitAll()
					.requestMatchers("/check").permitAll()
					.requestMatchers("/actuator/prometheus").permitAll()
					.anyRequest().hasRole("USER"));

		httpSecurity.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

		httpSecurity.addFilterBefore(new JwtExceptionFilter(), JwtAuthorizationFilter.class);

		return httpSecurity.build();
	}
}

