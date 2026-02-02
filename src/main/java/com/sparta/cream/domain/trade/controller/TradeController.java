package com.sparta.cream.domain.trade.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.domain.trade.service.TradeService;
import com.sparta.cream.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * 체결 관련 API를 처리하는 컨트롤러입니다.
 * TradeController.java
 *
 * @author kimsehyun
 * @since 2026. 2. 2.
 */
@RestController
@RequestMapping("/v1/trades")
@RequiredArgsConstructor
public class TradeController {

	private final TradeService tradeService;

	/**
	 * 체결된 거래를 취소합니다.
	 * 구매자 또는 판매자 본인만 요청할 수 있으며,
	 * 결제가 완료되지 않는 거래만 취소 간으합니다.
	 * @param tradeId 취소할 체결 ID
	 * @param userDetails 사용자 정보
	 * @return 체결 취소 성공 메세지
	 */
	@DeleteMapping("/{tradeId}")
	public ResponseEntity<Map<String,String>> cancelTrade(
		@PathVariable Long tradeId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		tradeService.cancelTrade(tradeId, userDetails.getId());

		return ResponseEntity.ok(
			Map.of("message", "체결이 정상적으로 취소되었습니다. 3일간 입찰 등록이 제한됩니다.")
		);

	}
}
