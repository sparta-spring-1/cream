package com.sparta.cream.domain.bid.controller;

import com.sparta.cream.domain.bid.dto.AdminBidCancelRequestDto;
import com.sparta.cream.domain.bid.dto.AdminBidCancelResponseDto;
import com.sparta.cream.domain.bid.dto.AdminBidPagingResponseDto;
import com.sparta.cream.domain.bid.service.BidService;
import com.sparta.cream.domain.trade.dto.AdminTradePagingResponseDto;
import com.sparta.cream.domain.trade.repository.TradeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 관리자 전용 입찰 관리 컨트롤러입니다.
 * 운영 정책에 위반되거나 비정상적인 입찰건에 대해
 * 관리자가 직접 개입하여 상태를 변경하는 기능을 제공합니다.
 * AdminBidController.java
 *
 * @author kimsehyun
 * @since 2026. 1. 27.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
public class AdminBidController {

	private final BidService bidService;
	private final TradeRepository tradeRepository;

	/**
	 * 관리자 권한으로 특정  입찰을 강제 취소합니다.
	 *
	 * @param bidId 취소 대상 입찰 식별자
	 * @param request 취소 사유 및 코멘트 정보
	 * @return 취소 결과 상세 정보가 포함된 응답 객체
	 */
	@PatchMapping("/bids/{bidId}")
	public ResponseEntity<Map<String, Object>> cancelBidByAdmin(
		@AuthenticationPrincipal UserDetails userIdAuth,
		@PathVariable Long bidId,
		@RequestBody AdminBidCancelRequestDto request) {

		Long adminId = Long.parseLong(userIdAuth.getUsername());
		AdminBidCancelResponseDto data = bidService.cancelBidByAdmin(bidId, request, adminId);

		Map<String, Object> response = new HashMap<>();
		response.put("status", 200);
		response.put("message", "입찰 강제 취소가 성공적으로 완료되었습니다.");
		response.put("data", data);

		return ResponseEntity.ok(response);
	}

	/**
	 * 관리자용 입찰(Bid) 모니터링 조회 API
	 * 다양한 조건(상품, 카테고리, 상태, 타입, 유저)을 기반으로
	 * 입찰 내역을 페이징 처리하여 조회합니다.
	 * 관리자 페이지에서 입찰 현황을 모니터링하기 위한 용도로 사용합니다.
	 *
	 * @param productId 조회할 상품 ID
	 * @param categoryId 조회할 카테고리 ID
	 * @param status 입찰 상태
	 * @param type 입찰 타입
	 * @param userId 특정 유저의 입찰 내역 조회 시 사용
	 * @param page 조회할 페이지 번호
	 * @return 입찰 모니터링 페이징 결과를 포함한 응답 객체
	 */
	@GetMapping("/bids")
	public ResponseEntity<Map<String, Object>> monitorBids(
		@RequestParam(required = false) Long productId,
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) String status,
		@RequestParam(required = false) String type,
		@RequestParam(required = false) Long userId,
		@RequestParam(defaultValue = "0") Integer page
	) {
		AdminBidPagingResponseDto data = bidService.getBidMonitoringList(productId,categoryId, status, type, userId, page);

		Map<String, Object> response = new HashMap<>();
		response.put("status", 200);
		response.put("message", "입찰 모니터링 조회가 완료되었습니다.");
		response.put("data", data);

		return ResponseEntity.ok(response);
	}

	/**
	 * 관리자용 실시간 거래(Trade) 체결 모니터링 조회 API
	 * 체결된 거래 내역을 기준으로 결제 상태, 배송 상태 등의
	 * 거래 진행 현황을 페이징 처리하여 조회합니다.
	 * 관리자 페이지에서 실시간 거래 흐름을 파악하기 위한 용도입니다.
	 *
	 * @param status 거래상태
	 * @param userId 특정유저의 거래 내역 조회시 사용
	 * @param page 조회할 페이지 번호
	 * @return 거래 체결 모니터링 페이징 결과를 포함한 응답객체
	 */
	@GetMapping("/trades")
	public ResponseEntity<Map<String, Object>> monitorTrades(
		@RequestParam(required = false) String status,
		@RequestParam(required = false) Long userId,
		@RequestParam(defaultValue = "0") Integer page
	) {

		AdminTradePagingResponseDto data = bidService.getTradeMonitoringList(status, userId, page);

		Map<String, Object> response = new HashMap<>();
		response.put("status", 200);
		response.put("message", "실시간 거래 체결 모니터링 조회가 완료되었습니다.");
		response.put("data", data);

		return ResponseEntity.ok(response);
	}
}
