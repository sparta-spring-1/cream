package com.sparta.cream.domain.bid.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.service.BidService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 입찰(Bid) 관련 요청 처리하는 REST 컨트롤러 입니다.
 * 인증된 사용자가 특정 상품 옵션에 대해 입찰에 대한 다양한 기능을 제공합니다.
 * BidController.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/bids")
public class BidController {

	private final BidService bidService;

	/**
	 * 입찰을 등록합니다.
	 * 현재 로그인한 식별자는 을 통해
	 * {@code CustomUserDetails} 객체로 주입받아 사용합니다.
	 * //@param user 인증된 사용자 정보
	 * @param requestDto 입찰 요청 데이터(상품 옵션 ID, 입찰 가격, 입찰 타입)
	 * @return 등록된 입찰 정보를 담은 응답 DTO
	 */
	@PostMapping
	public ResponseEntity<BidResponseDto> createBid(
		// 로그인 기능 구현 전이라 주석 처리
		// @AuthenticationPrincipal CustomUserDetails user,
		@Valid @RequestBody BidRequestDto requestDto) {

		//테스트용 UserId(1L)
		Long tempUserId = 1L;

		BidResponseDto response = bidService.createBid(tempUserId, requestDto);
		return ResponseEntity.ok(response);
	}
}
