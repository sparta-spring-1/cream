package com.sparta.cream.domain.bid.controller;

import com.sparta.cream.domain.bid.dto.AdminBidCancelRequestDto;
import com.sparta.cream.domain.bid.dto.AdminBidCancelResponseDto;
import com.sparta.cream.domain.bid.service.BidService;
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
@RequestMapping("/v1/admin/bids")
public class AdminBidController {

	private final BidService bidService;

	/**
	 * 관리자 권한으로 특정  입찰을 강제 취소합니다.
	 *
	 * @param bidId 취소 대상 입찰 식별자
	 * @param request 취소 사유 및 코멘트 정보
	 * @return 취소 결과 상세 정보가 포함된 응답 객체
	 */
	@PatchMapping("/{bidId}")
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
}
