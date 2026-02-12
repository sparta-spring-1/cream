package com.sparta.cream.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.Settlement;
import com.sparta.cream.domain.status.SettlementStatus;

import lombok.Getter;

@Getter
public class SettlementListResponse {
	private final Long id;
	private final BigDecimal settlementAmount;
	private final String status;
	private final LocalDateTime settledAt;
	private final String productName;

	public SettlementListResponse(Long id, BigDecimal settlementAmount, SettlementStatus status, LocalDateTime settledAt,
		String productName) {
		this.id = id;
		this.settlementAmount = settlementAmount;
		this.status = status.toString();
		this.settledAt = settledAt;
		this.productName = productName;
	}

	public static SettlementListResponse from(Settlement settlement) {
		return new SettlementListResponse(settlement.getId(),
			settlement.getSettlementAmount(),
			settlement.getStatus(),
			settlement.getSettledAt(),
			settlement.getPayment().getTrade().getSaleBidId().getProductOption().getProduct().getName());
	}
}
