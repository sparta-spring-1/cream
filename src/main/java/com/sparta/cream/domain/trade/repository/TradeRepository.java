package com.sparta.cream.domain.trade.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sparta.cream.domain.trade.entity.Trade;

/**
 * 거래(Trade) 레포지토리
 * {@link Trade} 엔티티에 대한 데이터베이스 접근 기능을 제공합니다.
 * Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD 및 페이징 기능을 지원합니다.
 * TradeRepository.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, TradeRepositoryCustom {

	/**
	 * 특정 사용자의 거래 내역을 조회합니다.
	 * @param userId 사용자 식별자
	 * @param pageable 페이징 및 정렬 정보
	 * @return 사용자가 구매자 혹은 판매자로 참여한 거래 내역
	 */
	@Query("SELECT t FROM Trade t WHERE t.purchaseBidId.user.id = :userId OR t.saleBidId.user.id = :userId")
	Page<Trade> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
