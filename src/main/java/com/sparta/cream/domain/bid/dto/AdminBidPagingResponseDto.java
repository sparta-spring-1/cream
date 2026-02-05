package com.sparta.cream.domain.bid.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 입찰(Bid) 모니터링 조회 API의 페이징 응답 DTO 입니다.
 * 입찰 모니터링 목록과 함께
 * 페이징 데이터를 포함합니다.
 * AdminBidPageResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@Getter
@AllArgsConstructor
public class AdminBidPagingResponseDto {
	private List<AdminBidMonitoringResponseDto> items;
	private PagingInfo paging;

	/**
	 * 페이징 데이터를 담는 내부 DTO
	 */
	@Getter
	@AllArgsConstructor
	public static class PagingInfo {
		private int currentPage;
		private long totalElements;
		private boolean hasNext;
	}
}
