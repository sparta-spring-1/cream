package com.sparta.cream.controller;

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

@RestController
@RequestMapping("/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

	private final SettlementService settlementService;

	@GetMapping
	public ResponseEntity<List<SettlementListResponse>> getSettlements(@AuthenticationPrincipal CustomUserDetails user) {
		List<SettlementListResponse> response = settlementService.getSettlements(user.getId());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{settlementId}")
	public ResponseEntity<SettlementDetailsResponse> getSettlement(@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long settlementId) {
		SettlementDetailsResponse response = settlementService.getSettlement(user.getId(), settlementId);
		return ResponseEntity.ok(response);
	}
}
