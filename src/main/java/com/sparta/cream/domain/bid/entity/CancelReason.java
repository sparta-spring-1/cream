package com.sparta.cream.domain.bid.entity;

import lombok.Getter;

/**
 * 관리자에 의한 입찰 취소 사유를 정의하는 Enum 클래스입니다.
 * 관리자가 부정한 방법으로 등록된 입찰이나, 운영정책에 위배ㅐ되는 입찰을
 * 강제로 취소할때 사용되는 표준 코드를 관리합니다.
 * CancelReason.java
 *
 * @author kimsehyun
 * @since 2026. 1. 27.
 */
@Getter
public enum CancelReason {
	FRAUD("매크로 및 부정거래 의심"),
	OUT_OF_STOCK("상품 재고 없음"),
	MISTAKE("가격 오기입"),
	POLICY_VIOLATION("운영 정책 위반");

	private final String description;
	CancelReason(String description) { this.description = description; }
}
