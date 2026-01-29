package com.sparta.cream.domain.bid.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;

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
	@Query("SELECT b FROM Bid b WHERE b.user.id = :userId ORDER BY b.createdAt ASC")
	Page<Bid> findAllByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, Pageable pageable);

	/**
	 * 특정 상품 옵션에 등록된 모든 입찰 내역을 입찰가 내림차순으로 조회합니다.
	 * @param productOptionId 상품옵션(사이즈에 대해 정의한 ID)
	 * @return 해당 상품 옵셥의 전체 입찰 리스트 (높은 가격순 정렬)
	 */
	List<Bid> findAllByProductOptionIdOrderByPriceDesc(Long productOptionId);

	/**
	 * 특정 상태를 가진 모든 입찰 내역을 조회합니다.
	 * @param status 입찰 상태 (PENDING, MATCHED등)
	 * @return 해당 상태의 입찰 리스트
	 */
	List<Bid> findAllByStatus(BidStatus status);

	/**
	 * 특정 타입과 상태를 가진 입찰 내역을 생성일자 순으로 조회합니다.
	 * 주로 전체 매칭 프로레스에서 대기중인 구매 입찰을 순차적으로 처리할 때 사용합니다.
	 * @param type 입찰타입(BUY, SELL)
	 * @param status 입찰 상태
	 * @return 정렬된 입찰 리스트
	 */
	List<Bid> findByTypeAndStatusOrderByCreatedAtAsc(BidType type, BidStatus status);

	/**
	 * 구매 입찰 발생시, 체결 가능한 최적의 판매 입찰을 조회합니다.
	 * 매칭 우선순위:
	 * 1. 가격이 구매가보다 낮거나 같을 것
	 * 2. 가격이 같다면 먼저 등록된 입찰 우선
	 * 3. 본인이 등록한 입찰은 제외
	 * @param productOptionId 상품 옵션 식별자
	 * @param price 구매 희망가
	 * @param userId 구매자 식별자
	 * @param pageable 조회ㅣ 개수 제한
	 * @return 매칭 후보 판매 입찰 리스트
	 */
	@Query("SELECT b FROM Bid b WHERE b.productOption.id = :productOptionId AND b.type = 'SELL' AND b.status = 'PENDING' AND b.price <= :price AND b.user.id != :userId ORDER BY b.price ASC, b.createdAt ASC")
	List<Bid> findMatchingSellBids(Long productOptionId, Long price, Long userId, Pageable pageable);

	/**
	 * 판매 입찰 발생시, 체결 가능한 최적의 구매 입찰을 조회합니다.
	 * 매칭 우선순위:
	 * 1. 가격이 판매가보다 높거나 같을 것
	 * 2. 가격이 같다면 먼저 등록된 입찰 수언
	 * 3. 본인이 등록한 입찰은 제회
	 * @param productOptionId 상품 옵션 식별자
	 * @param price 판매 희망가
	 * @param userId 판매자 식별자
	 * @param pageable 조회 개수 제한
	 * @return 매칭 후보 구매 입찰 리스트
	 */
	@Query("SELECT b FROM Bid b WHERE b.productOption.id = :productOptionId AND b.type = 'BUY' AND b.status = 'PENDING' AND b.price >= :price AND b.user.id != :userId ORDER BY b.price DESC, b.createdAt ASC")
	List<Bid> findMatchingBuyBids(Long productOptionId, Long price, Long userId, Pageable pageable);
}
