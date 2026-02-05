package com.sparta.cream.security;

import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.jwt.JwtTokenProvider;
import com.sparta.cream.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization 헤더의 Bearer Access Token을 검증하고, 인증 컨텍스트에 사용자 정보를 등록합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring("Bearer ".length()).trim();
		if (token.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			Long userId = jwtTokenProvider.getUserIdFromToken(token);
			Optional<Users> userOpt = userRepository.findById(userId);
			if (userOpt.isEmpty()) {
				request.setAttribute(SecurityErrorHandlers.ATTR_AUTH_ERROR_CODE, ErrorCode.AUTH_INVALID_TOKEN);
				filterChain.doFilter(request, response);
				return;
			}

			Users user = userOpt.get();
			CustomUserDetails principal = new CustomUserDetails(user.getId(), user.getEmail(), user.getRole());
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (JwtException | IllegalArgumentException e) {
			request.setAttribute(SecurityErrorHandlers.ATTR_AUTH_ERROR_CODE, ErrorCode.AUTH_INVALID_TOKEN);
		}

		filterChain.doFilter(request, response);
	}
}


