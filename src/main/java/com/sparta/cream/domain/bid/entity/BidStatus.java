package com.sparta.cream.domain.bid.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 입찰 상태에 대한 Enum 클래스입니다.
 * BidStatus.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public enum BidStatus {
	PENDING("대기 중"),
	MATCHED("체결 완료"),
	CANCELED("취소됨"),
	ADMIN_CANCELED("관리자 권한으로 취소됨");

	private final String description;
}
