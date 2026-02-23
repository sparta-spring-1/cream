package com.sparta.cream.domain.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
@RequestMapping("/v1/notification")
public class NotificationController {

	private final NotificationQueryService notificationQueryService;
	private final SseEmitters sseEmitters;

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
	@GetMapping("")
	public ResponseEntity<NotificationPageResponseDto> getNotifications(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		NotificationPageResponseDto response = notificationQueryService.getNotifications(userDetails.getId(), page, size);
		return ResponseEntity.ok(response);
	}

	/**
	 * 사용자의 실시간 알림 구독을 위한 SSE 연결을 생성합니다.
	 * 클라이언트가 GET 요청을 보내면 서버와 HTTP 연결을 유지하며,
	 * 이후 서버에서 발생하는 알림 이벤트를 실시간으로 수신할 있게 됩니다.
	 * - 미디어 타입: {@code text/event-stream} 형식을 사용하여 지속적인 스트리밍을 지원합니다.
	 * - 인코딩: UTF-8 설정을 통해 한글 메시지 깨짐을 방지합니다.
	 * - 관리: {@link SseEmitters} 클래스를 통해 각 사용자별 Emitter의 생명주기를 관리합니다.
	 * @param userDetails 인증된 사용자의 정보
	 * @return 사용자에게 할당된 {@link SseEmitter} 객체
	 */
	@GetMapping(value = "/subscribe", produces = "text/event-stream;charset=UTF-8")
	public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		return sseEmitters.add(userId);
	}
}

