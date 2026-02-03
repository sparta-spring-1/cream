package com.sparta.cream.domain.bid.repository;

import com.sparta.cream.domain.bid.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 관리자 입찰(Bid) 모니터링을 위한 커스텀 조회 기능을 정의하는ㄴ 인터페이스 입니다
 * 다양한 필터 조건을 조합하여 입찰 데이터를 조회하기 위해
 * QueryDSL에서 사용됩니다
 * BidRepositoryCustom.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
public interface BidRepositoryCustom {
	/**
	 * 관리자 입찰 모니터링 조건에 따른 입찰 목록을 조회합니다
	 * 상품, 카테고리, 상태, 타입, 유저등의
	 * 필터 조건을 조합하여 페이징된 목록을 반환합니다.
	 * @param productId 조회할 상품ID
	 * @param categoryId 조회할 카테고리 ID
	 * @param status 입찰상태
	 * @param type 입찰 타입
	 * @param pageable 페이징 정보
	 * @param userId 특정 유저의 입찰 내역 조회
	 * @return 필터 조건이 맞는 입찰 목록 결과
	 */
	Page<Bid> findAllByMonitoringFilter(Long productId, Long categoryId, String status, String type, Pageable pageable , Long userId);
}
