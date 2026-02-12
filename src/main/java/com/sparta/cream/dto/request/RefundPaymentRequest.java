package com.sparta.cream.dto.request;

import java.math.BigDecimal;

import lombok.Getter;

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
