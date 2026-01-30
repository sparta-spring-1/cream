package com.sparta.cream.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;

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
