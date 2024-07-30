package com.m3pro.groundflip.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	private static final List<String> WHITE_LIST = List.of(
		"/api/auth",
		"/api/docs",
		"/v3/api-docs",
		"/api/swagger-ui",
		"/check"
	);
	private static final List<String> WHITE_LIST_TMP = List.of(
		"/api",
		"/api/docs",
		"/v3/api-docs",
		"/api/swagger-ui");
	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String token = parseBearerToken(request);
		jwtProvider.validateToken(token);
		setAuthentication(token);

		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String accessToken) {
		Long userId = jwtProvider.parseUserId(accessToken);
		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(userId, "",
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return WHITE_LIST.stream().anyMatch(path::startsWith);
	}

	private String parseBearerToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (bearerToken != null && bearerToken.startsWith("Bearer")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}

