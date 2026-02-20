package com.sparta.cream.domain.trade.event;

/**
 * 거래 체결 완료 도메인 이벤트
 * 매칭 엔진에 의해 거래가 성사되었을 때 발행됩니다.
 */
public record TradeMatchedEvent(
	Long buyerId,      // 구매자 ID
	Long sellerId,     // 판매자 ID
	Long price,        // 체결 가격
	Long tradeId,      // 생성된 거래(Trade) ID
	String productSize // 상품 옵션 정보 (사이즈 등)
) {}
