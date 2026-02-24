package com.sparta.cream.domain.trade.event;

/**
 * 거래 체결이 취소되었을 때 발행되는 도메인 이벤트입니다.
 * 체결된 거래가 특정 사유로 인해 취소될 때 발행되며,
 * 취소 유저 에게는 패널티 안내를, 상대방에게는 상태 복구 안내를
 * 발송하기 위한 기초 데이터를 포함합니다.
 * TradeCancelledEvent.java
 *
 * @author kimsehyun
 * @since 2026. 2. 13.
 */
public record TradeCancelledEvent(
	Long cancelUserId,
	Long victimUserId,
	Long tradeId
) {}
