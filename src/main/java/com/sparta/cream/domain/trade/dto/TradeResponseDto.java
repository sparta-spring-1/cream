package com.sparta.cream.domain.trade.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 거래(Trade) 응답 정보를 전달하기 위한 DTO 클래스입니다
 * TradeResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 2. 26.
 */
@Getter
@Builder
public class TradeResponseDto {
	private final Long id;
	private final String productName;
	private final String size;
	private final Long price;
	private final String status;
	private final LocalDateTime matchedAt;
	private final String role;

	public TradeResponseDto(Long id, String productName, String size, Long price, String status, LocalDateTime matchedAt, String role) {
		this.id = id;
		this.productName = productName;
		this.size = size;
		this.price = price;
		this.status = status;
		this.matchedAt = matchedAt;
		this.role = role;
	}
}
