package com.sparta.cream.domain.trade.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 거래 상태에 대한 Enum 클래스입니다.
 * 체결된 거래의 결제 단계를 관리합니다.
 * TradeStatus.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@Getter
@RequiredArgsConstructor
public enum TradeStatus {
	WAITING_PAYMENT("결제 대기 중"),
	PAYMENT_COMPLETED("결제 완료"),
	PAYMENT_CANCELED("결제 취소");

	private final String description;
}
