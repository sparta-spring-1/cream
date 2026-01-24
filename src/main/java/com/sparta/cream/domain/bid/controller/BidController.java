package com.sparta.cream.domain.bid.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.service.BidService;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;

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

	/**
	 * 현재 로그인한 사용자의 압찰 내역 목록을 조회합니다.
	 * @return 사용자의 입찰 정보 목록
	 */
	@GetMapping("/me")
	public ResponseEntity<List<BidResponseDto>> getMyBids() {
		// 로그인 기능 구현 전이라 주석 처리
		// @AuthenticationPrincipal CustomUserDetails user,

		// 유저 기능 구현 전까지 임시값 사용
		Long userId = 1L;
		List<BidResponseDto> myBids = bidService.getMyBids(userId);
		return ResponseEntity.ok(myBids);
	}

	/**
	 * 특정 상품 옵션의 전체 입찰 내역을 조회합니다.
	 * 성퓸에 대해 입찰중인 정보와 시세를 확인 하기 위해 사용됩니다.
	 * @param productOptionId 상품 옵션 식별자
	 * @return 상품 옵션별 입찰 목록(입찰가 내립차순 리스트)
	 */
	@GetMapping
	public ResponseEntity<List<BidResponseDto>> getBidsByProduct(
		@RequestParam(required = false) Long productOptionId) {

		if (productOptionId == null) {
			throw new BusinessException(ErrorCode.PRODUCT_ID_REQUIRED);
		}

		List<BidResponseDto> bids = bidService.getBidsByProductOption(productOptionId);
		return ResponseEntity.ok(bids);
	}

}
