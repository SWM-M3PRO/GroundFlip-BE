package com.m3pro.groundflip.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m3pro.groundflip.exception.AppException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (AppException e) {
			setErrorResponse(request, response, e);
		}
	}

	public void setErrorResponse(HttpServletRequest request, HttpServletResponse response, AppException ex) throws
		IOException {
		final Map<String, Object> body = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper();

		body.put("result", "error");
		body.put("message", ex.getErrorCode().getMessage());
		body.put("data", null);

		response.setContentType("application/json; charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(mapper.writeValueAsString(body));
	}
}

