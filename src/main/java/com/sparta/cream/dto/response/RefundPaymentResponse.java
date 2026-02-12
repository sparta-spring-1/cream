package com.sparta.cream.dto.response;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class RefundPaymentResponse {
    private final Long refundId;
    private final BigDecimal cancelledAmount;
    private final String status;

	public RefundPaymentResponse(Long refundId, BigDecimal cancelledAmount, String status) {
		this.refundId = refundId;
		this.cancelledAmount = cancelledAmount;
		this.status = status;
	}
}
