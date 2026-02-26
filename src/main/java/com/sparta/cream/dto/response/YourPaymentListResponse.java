package com.sparta.cream.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.Payment;

import lombok.Getter;

/**
 * 사용자 본인의 결제 목록 응답 데이터를 나타내는 DTO 클래스입니다.
 * <p>
 * id, merchantUid, productName, amount, paidAt, status를 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
@Getter
public class YourPaymentListResponse {
	private final Long id;
	private final Long tradeId;
	private final String merchantUid;
	private final String productName;
	private final BigDecimal amount;
	private final LocalDateTime paidAt;
	private final String status;

	public YourPaymentListResponse(Long id, Long tradeId, String merchantUid, String productName, BigDecimal amount, LocalDateTime paidAt,
		String status) {
		this.id = id;
		this.tradeId = tradeId;
		this.merchantUid = merchantUid;
		this.productName = productName;
		this.amount = amount;
		this.paidAt = paidAt;
		this.status = status;
	}

	public static YourPaymentListResponse from(Payment payment) {
		return new YourPaymentListResponse(payment.getId(),
			payment.getTrade().getId(),
			payment.getMerchantUid(),
			payment.getProductName(),
			payment.getAmount(),
			payment.getPaidAt(),
			payment.getStatus().toString());
	}

}
