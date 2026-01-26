package com.sparta.cream.domain.bid.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 입찰 타입에 대한 Enum 클래스입니다.
 * 해당 입찰이 상품 구매입찰인지, 상품 판매 입찰인지 구분을 위해 생성했습니다.
 * BidType.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public enum BidType {
	BUY("구매"),
	SELL("판매");

	private final String description;
}

