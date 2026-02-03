package com.sparta.cream.domain.trade.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.cream.domain.trade.entity.Trade;

/**
 * 관리자 체결(Trade) 모니터링을 위한 커스텀 조회 기능을 정의하는 인터페이스 입니다.
 * 거래 상태, 특정 유저의 거래 내역을
 * 동적 조건으로 조회하기 위해 사용되며,
 * QueryDSL 에서 구현됩니다,
 * TradeRepositoryCustom.java
 *
 * @author kimsehyun
 * @since 2026. 1. 30.
 */
public interface TradeRepositoryCustom {
	/**
	 * 관리자 거래 모니터링 조건에 따라 거래 목록을 조회합니다.
	 * @param status 거래 상태
	 * @param userId 특정유저의 거래 조회
	 * @param pageable 페이징 정보
	 * @return 필터 조건에 맞는 거래 목록
	 */
	Page<Trade> findAllByTradeFilter(
		String status,
		Long userId,
		Pageable pageable
	);
}
