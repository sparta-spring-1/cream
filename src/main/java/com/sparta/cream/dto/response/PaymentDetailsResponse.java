package com.sparta.cream.dto.response;

import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.Payment;

import lombok.Getter;

@Getter
public class PaymentDetailsResponse {
	private final Long id;
	private final String merchantUid;
	private final String productName;
	private final Long amount;
	private final String status;
	private final String method;
	private final LocalDateTime paidAt;

	public PaymentDetailsResponse(Long id, String merchantUid, String productName, Long amount, String status, String method, LocalDateTime paidAt) {
		this.id = id;
		this.merchantUid = merchantUid;
		this.productName = productName;
		this.amount = amount;
		this.status = status;
		this.method = method;
		this.paidAt = paidAt;
	}

	public static PaymentDetailsResponse from(Payment payment) {
		return new PaymentDetailsResponse(payment.getId(),
			payment.getMerchantUid(),
			payment.getProductName(),
			payment.getAmount(),
			payment.getStatus().toString(),
			payment.getMethod(),
			payment.getPaidAt());
	}
}
