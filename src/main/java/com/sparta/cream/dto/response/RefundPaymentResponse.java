package com.sparta.cream.dto.response;

import lombok.Getter;

@Getter
public class RefundPaymentResponse {
    private final Long refundId;
    private final Long cancelledAmount;
    private final String status;

	public RefundPaymentResponse(Long refundId, Long cancelledAmount, String status) {
		this.refundId = refundId;
		this.cancelledAmount = cancelledAmount;
		this.status = status;
	}
}
