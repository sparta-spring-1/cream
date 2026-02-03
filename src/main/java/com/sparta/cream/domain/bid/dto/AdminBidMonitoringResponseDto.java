package com.sparta.cream.domain.bid.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 입찰 모니터링 개별 아이템 DTO
 * 입찰자정보, 상품 및 카테고리 정보, 입찰 가격과 상태 정보 제공
 * AdminBidMonitoringResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBidMonitoringResponseDto {

	private Long bidId;
	private Long userId;
	private String userName;
	private Long productId;
	private String productName;
	private Long categoryId;
	private String categoryName;
	private Long price;
	private String type;
	private String status;
	private LocalDateTime createdAt;

}
