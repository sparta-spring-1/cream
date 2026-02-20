package com.sparta.cream.dto.request;

import java.math.BigDecimal;

import lombok.Getter;

/**
 * 결제 환불 요청 시 클라이언트로부터 받는 데이터를 나타내는 DTO 클래스입니다.
 * <p>
 * tradeId, reason, amount를 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
public class RefundPaymentRequest {
    private final Long tradeId;
    private final String reason;
    private final BigDecimal amount;

	public RefundPaymentRequest(Long tradeId, String reason, BigDecimal amount) {
		this.tradeId = tradeId;
		this.reason = reason;
		this.amount = amount;
	}
}
