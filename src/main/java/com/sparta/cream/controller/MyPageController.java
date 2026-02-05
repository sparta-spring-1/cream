package com.sparta.cream.controller;

import com.sparta.cream.dto.user.MeResponseDto;
import com.sparta.cream.security.CustomUserDetails;
import com.sparta.cream.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지(내 정보) API를 처리하는 컨트롤러입니다.
 * JWT 인증 필터가 Authorization 헤더의 Bearer 토큰을 검증하여
 * SecurityContext에 인증 정보를 등록하므로, 컨트롤러에서는 @AuthenticationPrincipal로
 * 사용자 정보를 바로 받아 사용할 수 있습니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class MyPageController {

	private final MyPageService myPageService;

	/**
	 * 내 정보 조회 API
	 * JWT 인증 필터가 Authorization 헤더의 Bearer Access Token을 검증하여
	 * SecurityContext에 인증 정보를 등록하므로, @AuthenticationPrincipal로
	 * 사용자 정보를 바로 받아 사용할 수 있습니다.
	 *
	 * Header:
	 * - Content-Type: application/json
	 * - Authorization: Bearer {access_token}
	 *
	 * @param user JWT 인증 필터가 검증한 사용자 정보 (CustomUserDetails)
	 * @return 내 정보 응답 DTO (id, email, name, createdAt, updatedAt)
	 */
	@GetMapping("/me")
	public ResponseEntity<MeResponseDto> me(@AuthenticationPrincipal CustomUserDetails user) {
		if (user == null) {
			throw new com.sparta.cream.exception.BusinessException(
				com.sparta.cream.exception.ErrorCode.AUTH_UNAUTHORIZED);
		}
		MeResponseDto response = myPageService.getMe(user.getId());
		return ResponseEntity.ok(response);
	}
}


