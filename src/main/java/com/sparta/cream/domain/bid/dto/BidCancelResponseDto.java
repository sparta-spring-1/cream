package com.sparta.cream.domain.bid.dto;

import java.time.LocalDateTime;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 입찰 취소 응답 DTO 입니다
 * 사용자가 입찰 취소 요청을 성공적으로 수행했을때
 * 클라리언트에게 반환되는 응답 객체 입니다
 * 해당 DTO는 입찰의 현재 상태, 취소 안내 메세지, 입찰이 취소된 시각을 포합합니다.
 * BidCancelResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 25.
 */
@Getter
@AllArgsConstructor
public class BidCancelResponseDto {

	private Long bidId;
	private BidStatus status;
	private String message;
	private LocalDateTime canceledAt;

	/**
	 * Bid 엔티티를 기반으로 입찰 취소 응답 DTO를 생성합니다.
	 * 해당 메서드는 입찰이 정상적으로 취소된 이후 호출되어야 하며
	 * 입찰의 상태는 {@link BidStatus#CANCELED} 상태여야 합니다.
	 * @param bid 취소 처리된 입찰 엔티티
	 * @return 입찰 튀소 응답 DTO
	 */
	public static BidCancelResponseDto from(Bid bid) {
		return new BidCancelResponseDto(
			bid.getId(),
			bid.getStatus(),
			"입찰이 성공적으로 취소되었습니다.",
			bid.getUpdatedAt()
		);
	}
}
