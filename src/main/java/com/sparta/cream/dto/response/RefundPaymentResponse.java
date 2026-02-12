package com.sparta.cream.dto.response;

import java.math.BigDecimal;

import lombok.Getter;

/**
 * 결제 환불 후 반환되는 응답 데이터를 나타내는 DTO 클래스입니다.
 * <p>
 * refundId, cancelledAmount, status를 포함합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 02. 12.
 */
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
