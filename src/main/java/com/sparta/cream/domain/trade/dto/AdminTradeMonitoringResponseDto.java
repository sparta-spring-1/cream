package com.sparta.cream.domain.trade.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 페이지에서 실시간 체결(Trade) 현황을 모니터링하기 위해
 * 사용되는 거래 정보 응답 DTO 입니다.
 * 거래 상품 정보, 최종 체결가격, 거래 상태,
 * 판매자, 구매자 정보와 거래 생성 시각을 포함하여
 * 관리자 화면에서 체결현황을 모니터링할수 있도록 하였습니다.
 * AdminTradeMonitoringResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 30.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminTradeMonitoringResponseDto {
	private Long tradeId;
	private String productName;
	private Long price;
	private String status;
	private String sellerName;
	private String buyerName;
	private LocalDateTime createdAt;
}
