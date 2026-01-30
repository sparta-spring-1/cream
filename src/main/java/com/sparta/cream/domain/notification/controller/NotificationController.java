package com.sparta.cream.domain.notification.controller;

import com.sparta.cream.domain.notification.dto.NotificationPageResponseDto;
import com.sparta.cream.domain.notification.service.NotificationQueryService;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 조회 API를 처리하는 컨트롤러입니다.
 * 플로우차트에 따라 JwtTokenProvider를 직접 호출하여 Access Token을 검증합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class NotificationController {

	private final NotificationQueryService notificationQueryService;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 알림 목록 조회 API
	 * Authorization 헤더의 Bearer Access Token을 검증한 뒤 사용자 알림 목록을 조회합니다.
	 *
	 * Header:
	 * - Content-Type: application/json
	 * - Authorization: Bearer {access_token}
	 *
	 * Query Parameters:
	 * - page: 페이지 번호 (기본값: 0)
	 * - size: 페이지 크기 (기본값: 10)
	 *
	 * @param authorization Authorization 헤더 값
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return 페이징된 알림 목록 응답 DTO
	 */
	@GetMapping("/notification")
	public ResponseEntity<NotificationPageResponseDto> getNotifications(
		@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		// Access Token 검증
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
		}

		String token = authorization.substring("Bearer ".length()).trim();
		if (token.isEmpty()) {
			throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
		}

		Long userId;
		try {
			userId = jwtTokenProvider.getUserIdFromToken(token);
		} catch (JwtException | IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
		}

		NotificationPageResponseDto response = notificationQueryService.getNotifications(userId, page, size);
		return ResponseEntity.ok(response);
	}
}

