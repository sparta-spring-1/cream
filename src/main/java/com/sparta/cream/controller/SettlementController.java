package com.sparta.cream.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.dto.response.SettlementDetailsResponse;
import com.sparta.cream.dto.response.SettlementListResponse;
import com.sparta.cream.security.CustomUserDetails;
import com.sparta.cream.service.SettlementService;

import lombok.RequiredArgsConstructor;

/**
 * 정산 관련 API 요청을 처리하는 컨트롤러입니다.
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@RestController
@RequestMapping("/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

	private final SettlementService settlementService;

	/**
	 * 사용자 본인의 전체 정산 내역을 조회합니다.
	 *
	 * @param user     인증된 사용자 정보
	 * @param pageable 페이지네이션 정보 (페이지 번호, 페이지 크기, 정렬 기준 등)
	 * @return 페이징 처리된 정산 내역 응답 객체
	 */
	@GetMapping
	public ResponseEntity<Page<SettlementListResponse>> getSettlements(
		@AuthenticationPrincipal CustomUserDetails user,
		@PageableDefault(sort = "settledAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<SettlementListResponse> response = settlementService.getSettlements(user.getId(), pageable);
		return ResponseEntity.ok(response);
	}

	/**
	 * 특정 정산의 상세 정보를 조회합니다.
	 *
	 * @param user         인증된 사용자 정보
	 * @param settlementId 조회할 정산의 식별자
	 * @return 정산 상세 정보 응답 객체
	 */
	@GetMapping("/{settlementId}")
	public ResponseEntity<SettlementDetailsResponse> getSettlement(@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long settlementId) {
		SettlementDetailsResponse response = settlementService.getSettlement(user.getId(), settlementId);
		return ResponseEntity.ok(response);
	}
}
