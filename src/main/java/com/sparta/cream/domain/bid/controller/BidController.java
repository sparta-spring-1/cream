package com.sparta.cream.domain.bid.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.domain.bid.dto.BidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.BidRequestDto;
import com.sparta.cream.domain.bid.dto.BidResponseDto;
import com.sparta.cream.domain.bid.service.BidService;
import com.sparta.cream.security.CustomUserDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody BidRequestDto requestDto) {

		Long userId = userDetails.getId();
		BidResponseDto response = bidService.createBid(userId, requestDto);

		return ResponseEntity.ok(response);
	}

	/**
	 * 현재 로그인한 사용자의 압찰 내역 목록을 조회합니다.
	 * @return 사용자의 입찰 정보 목록
	 */
	@GetMapping("/me")
	public ResponseEntity<Page<BidResponseDto>> getMyBids(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size) {

		Long userId = userDetails.getId();
		return ResponseEntity.ok(bidService.getMyBids(userId, page, size));

	}

	/**
	 * 특정 상품 옵션의 전체 입찰 내역을 조회합니다.
	 * 성퓸에 대해 입찰중인 정보와 시세를 확인 하기 위해 사용됩니다.
	 * @param productOptionId 상품 옵션 ID
	 * @return 상품 옵션별 입찰 목록(입찰가 내립차순 리스트)
	 */
	@GetMapping
	public ResponseEntity<List<BidResponseDto>> getBidsByProduct(
		@NotNull(message = "상품 옵션 아이디는 필수 입니다.")
		@RequestParam Long productOptionId) {

		List<BidResponseDto> bids = bidService.getBidsByProductOption(productOptionId);
		return ResponseEntity.ok(bids);
	}

	/**
	 * 본인이 입찰한 정보를 수정하는 API입니다.
	 * 사용자가 등록한 입찰의 상품 옵션, 가격, 타입을 수정합니다.
	 * 체결 대기인 상태만 입찰을 수정할 수 있으며, 본인의 입찰이 아닐 경우 예외가 발생합니다.
	 * @param bidId 수정할 입찰의 ID
	 * @param requestDto 수정할 정보를 담은 DTO
	 * @return 함께 수정된 입찰 상세 정보 반환
	 */
	@PatchMapping("/{bidId}")
	public ResponseEntity<BidResponseDto> updateBid(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long bidId,
		@Valid @RequestBody BidRequestDto requestDto) {

		Long userId = userDetails.getId();

		return ResponseEntity.ok(bidService.updateBid(userId, bidId, requestDto));
	}

	/**
	 * 본인이 입찰한 입찰을 취소하는 API입니다.
	 * 사용자가 등록한 입찰을 취소합니다.
	 *
	 * @param bidId 취소할 입찰의 ID
	 * @return 입찰 취소 결과 응답 DTO
	 */
	@DeleteMapping("/{bidId}")
	public ResponseEntity<BidCancelResponseDto> cancelBid(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long bidId
	) {
		Long userId = userDetails.getId();
		return ResponseEntity.ok(bidService.cancelBid(userId, bidId));
	}

}
