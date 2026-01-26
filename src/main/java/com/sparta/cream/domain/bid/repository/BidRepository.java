package com.sparta.cream.domain.bid.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.cream.domain.bid.entity.Bid;

/**
 * 입찰(Bid) 도메인을 위한 데이터 접근 저장소입니다.
 * BidRepository.java
 *
 * @author kimsehyun
 * @since 2026. 1. 22.
 */
public interface BidRepository extends JpaRepository<Bid, Long> {

	/**
	 * 특정 사용자가 등록한 모든 입찰 내역을 생성일자 오름차순으로 조회합니다.
	 * @param userId 사용자 식별자
	 * @return 사용자 전체 입찰 리스트 (과거순 정렬)
	 */
	List<Bid> findAllByUserIdOrderByCreatedAtAsc(Long userId);

	/**
	 * 특정 상품 옵션에 등록된 모든 입찰 내역을 입찰가 내림차순으로 조회합니다.
	 * @param productOptionId 상품옵션(사이즈에 대해 정의한 ID)
	 * @return 해당 상품 옵셥의 전체 입찰 리스트 (높은 가격순 정렬)
	 */
	List<Bid> findAllByProductOptionIdOrderByPriceDesc(Long productOptionId);
}
