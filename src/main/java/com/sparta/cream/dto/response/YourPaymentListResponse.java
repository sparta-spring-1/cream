package com.sparta.cream.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.Payment;

import lombok.Getter;

@Getter
public class YourPaymentListResponse {
	private final Long id;
	private final String merchantUid;
	private final String productName;
	private final BigDecimal amount;
	private final LocalDateTime paidAt;
	private final String status;

	public YourPaymentListResponse(Long id, String merchantUid, String productName, BigDecimal amount, LocalDateTime paidAt,
		String status) {
		this.id = id;
		this.merchantUid = merchantUid;
		this.productName = productName;
		this.amount = amount;
		this.paidAt = paidAt;
		this.status = status;
	}

	public static YourPaymentListResponse from(Payment payment) {
		return new YourPaymentListResponse(payment.getId(),
			payment.getMerchantUid(),
			payment.getProductName(),
			payment.getAmount(),
			payment.getPaidAt(),
			payment.getStatus().toString());
	}

}
