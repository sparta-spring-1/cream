package com.sparta.cream.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.Payment;

import lombok.Getter;

/**
 * 결제 상세 정보를 나타내는 DTO 클래스입니다.
 * <p>
 * id, merchantUid, productName, amount, status, method, paidAt을 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
public class PaymentDetailsResponse {
	private final Long id;
	private final String merchantUid;
	private final String productName;
	private final BigDecimal amount;
	private final String status;
	private final String method;
	private final LocalDateTime paidAt;

	public PaymentDetailsResponse(Long id, String merchantUid, String productName, BigDecimal amount, String status, String method, LocalDateTime paidAt) {
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
