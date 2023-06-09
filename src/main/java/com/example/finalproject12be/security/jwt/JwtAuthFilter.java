package com.example.finalproject12be.security.jwt;

import static com.example.finalproject12be.security.jwt.JwtUtil.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.finalproject12be.domain.member.dto.response.ErrorResponse;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.entity.MemberRoleEnum;
import com.example.finalproject12be.domain.member.repository.MemberRepository;
import com.example.finalproject12be.exception.ErrorCode;
import com.example.finalproject12be.exception.MemberErrorCode;
import com.example.finalproject12be.exception.RestApiException;
import com.example.finalproject12be.exception.TokenErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
		ServletException,
		IOException {

		String accessToken = jwtUtil.resolveToken(request, jwtUtil.ACCESS_KEY);
		String refreshToken = jwtUtil.resolveToken(request, jwtUtil.REFRESH_KEY);

		if(accessToken != null) {
			if(jwtUtil.validateToken(accessToken)) {
				Claims accessInfo =jwtUtil.getUserInfoFromToken(accessToken);
				setAuthentication(accessInfo.getSubject());
			} else if(refreshToken != null && jwtUtil.refreshTokenValidation(refreshToken)) {

				Claims refreshInfo = jwtUtil.getUserInfoFromToken(refreshToken);
				 Member member = memberRepository.findByEmail(refreshInfo.getSubject()).orElseThrow(
					 () -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

				String newAccessToken = jwtUtil.createToken(refreshInfo.getSubject(), "Access", member.getRole());
				jwtUtil.setHeaderAccessToken(response, newAccessToken);
				setAuthentication(refreshInfo.getSubject());
			} else if(refreshToken == null) {
				jwtExceptionHandler(response, TokenErrorCode.EXPIRED_ACCESS_TOKEN);
				return;
			} else {
				jwtExceptionHandler(response, TokenErrorCode.EXPIRED_REFRESH_TOKEN);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	public void setAuthentication(String username) {

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = jwtUtil.createAuthentication(username);
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

	public void jwtExceptionHandler(HttpServletResponse response, ErrorCode errorCode) {

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json");
		try {
			String json = new ObjectMapper()
				.writeValueAsString(new ErrorResponse(
					errorCode.name(),
					errorCode.getHttpStatus().toString(),
					errorCode.getMessage()));
			response.getWriter().write(json);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}
