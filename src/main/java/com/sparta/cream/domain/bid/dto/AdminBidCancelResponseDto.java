package com.sparta.cream.domain.bid.dto;

import com.sparta.cream.domain.bid.entity.BidStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 입찰 취소 성공시 반환되는 응답 DTO 클래스입니다.
 * 취소된 입찰의 식별자, 변경된 상태, 취소 처리한 관리자 정보 및 사유를 포함합니다.
 * AdminBidCancelResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 27.
 */
@Getter
@AllArgsConstructor
public class AdminBidCancelResponseDto {

	private final Long bidId;
	private final BidStatus status;
	private final String canceledBy;
	private final String canceledAt;
	private final String reason;

}
