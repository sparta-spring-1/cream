package com.sparta.cream.domain.trade.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 실시간 체결(Trade) 모니터링 조회 API의
 * 페이징 응답 DTO입니다.
 * 거래 모니터링 목록, 현재 페이징 정보, 전체 데이터 수, 다음 페이지 존재 여부
 * 페이징 데이터를 포함합니다
 * AdminTradePagingResponseDto.java
 *
 * @author kimsehyun
 * @since 2026. 1. 30.
 */
@Getter
@AllArgsConstructor
public class AdminTradePagingResponseDto {
	private List<AdminTradeMonitoringResponseDto> items;
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
