package com.sparta.cream.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.cream.config.PortOneConfig;
import com.sparta.cream.dto.request.CreatePaymentRequest;
import com.sparta.cream.dto.response.CreatePaymentResponse;
import com.sparta.cream.dto.response.PaymentConfigResponse;
import com.sparta.cream.security.CustomUserDetails;
import com.sparta.cream.service.PaymentService;

import lombok.RequiredArgsConstructor;

/**
 * 결제 관련 API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 결제 사전 준비(Prepare) 등
 * 결제 프로세스와 관련된 HTTP 요청을 처리합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 26.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {

	private final PaymentService paymentService;
	private final PortOneConfig portOneConfig;

	/**
	 * 프론트엔드 결제창 호출에 필요한 환경 설정 정보를 조회합니다.
	 *
	 * @return Store ID 및 Channel Key를 담은 응답 객체
	 */
	@GetMapping("/config")
	public ResponseEntity<PaymentConfigResponse> getPaymentConfig() {
		return ResponseEntity.ok(new PaymentConfigResponse(portOneConfig.getStoreId(), portOneConfig.getChannelKey()));
	}

	/**
	 * 결제 요청 전, 서버 측 결제 정보를 준비(생성)합니다.
	 * <p>
	 * 클라이언트가 PortOne 결제창을 띄우기 전 호출하며,
	 * 서버 DB에 결제 준비 상태의 Payment를 생성하고 merchantUid를 발급합니다.
	 * </p>
	 *
	 * @param request 	결제 준비 요청 데이터 (Trade ID)
	 * @param user      인증된 사용자 정보
	 * @return 발급된 merchantUid 및 결제 정보를 담은 응답 객체
	 */
	@PostMapping("/prepare")
	public ResponseEntity<CreatePaymentResponse> preparePayment(@RequestBody CreatePaymentRequest request,
		@AuthenticationPrincipal CustomUserDetails user) {
		CreatePaymentResponse response = paymentService.prepare(request.getTradeId(), user.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
