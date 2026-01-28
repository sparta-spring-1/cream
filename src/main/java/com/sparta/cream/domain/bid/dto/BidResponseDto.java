package com.sparta.cream.domain.bid.dto;

import java.time.LocalDateTime;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;

import lombok.Getter;

/**
 * 입찰에 대한 응답 DTO 클래스입니다.
 * 입찰 생성, 조회시 클라이언트에게 입찰 정보를 반환하기 위해 사용됩니다.
 * BidResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@Getter
public class BidResponseDto {
	private final Long id;
	private final Long userId;
	private final Long productId;
	private final Long price;
	private final BidType type;
	private final BidStatus status;
	private final LocalDateTime createdAt;
	private final LocalDateTime expiresAt;

	/**
	 * Bid 엔티티를 받아 Response DTO로 반환하는 생성자
	 * @param bid 변환할 입찰 엔티티 객체
	 */
	public BidResponseDto(Bid bid) {
		this.id = bid.getId();
		this.userId = bid.getUserId();
		this.productId = bid.getProductId();
		this.price = bid.getPrice();
		this.type = bid.getType();
		this.status = bid.getStatus();
		this.createdAt = bid.getCreatedAt();
		this.expiresAt = bid.getExpiresAt();
	}
}
