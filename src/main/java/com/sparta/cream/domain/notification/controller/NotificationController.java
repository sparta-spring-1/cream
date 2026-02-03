package com.sparta.cream.domain.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.domain.notification.dto.NotificationPageResponseDto;
import com.sparta.cream.domain.notification.service.NotificationQueryService;
import com.sparta.cream.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

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

	/**
	 * 알림 목록 조회 API
	 * Authorization 헤더의 Bearer Access Token을 검증한 뒤 사용자 알림 목록을 조회합니다.
	 * <p>
	 * Header:
	 * - Content-Type: application/json
	 * - Authorization: Bearer {access_token}
	 * </p>
	 * <p>
	 * Query Parameters:
	 * - page: 페이지 번호 (기본값: 0)
	 * - size: 페이지 크기 (기본값: 10)
	 * </p>
	 * @param userDetails 인증된 사용자 정보
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return 페이징된 알림 목록 응답 DTO
	 */
	@GetMapping("/notification")
	public ResponseEntity<NotificationPageResponseDto> getNotifications(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		NotificationPageResponseDto response = notificationQueryService.getNotifications(userDetails.getId(), page, size);
		return ResponseEntity.ok(response);
	}
}

