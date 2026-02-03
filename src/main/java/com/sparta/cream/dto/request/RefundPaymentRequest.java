package com.sparta.cream.dto.request;

import lombok.Getter;

@Getter
public class RefundPaymentRequest {
    private final Long tradeId;
    private final String reason;
    private final Long amount;

	public RefundPaymentRequest(Long tradeId, String reason, Long amount) {
		this.tradeId = tradeId;
		this.reason = reason;
		this.amount = amount;
	}
}
