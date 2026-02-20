package com.sparta.cream.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 결제 완료 후 반환되는 응답 데이터를 나타내는 DTO 클래스입니다.
 * <p>
 * (PortOne의)paymentId, status, paidAt을 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
public class CompletePaymentResponse {
	private final String paymentId;
	private final String status;
	private final LocalDateTime paidAt;

	public CompletePaymentResponse(String paymentId, String status, LocalDateTime paidAt) {
		this.paymentId = paymentId;
		this.status = status;
		this.paidAt = paidAt;
	}
}
