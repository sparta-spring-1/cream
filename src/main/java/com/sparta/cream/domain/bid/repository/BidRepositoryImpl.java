package com.sparta.cream.domain.bid.repository;

import static com.sparta.cream.domain.bid.entity.QBid.bid;
import static com.sparta.cream.entity.QProduct.*;
import static com.sparta.cream.entity.QProductCategory.*;
import static com.sparta.cream.entity.QProductOption.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.domain.bid.entity.BidStatus;
import com.sparta.cream.domain.bid.entity.BidType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 관리자 입찰(Bid) 모니터링 조회를 위한 QueryDSL 기반 커스텀 레포지토리입니다.
 * 다양한 선택적 조건(상품, 카테고리, 상태, 타입, 유저)dmf
 * 조합하여 입찰 데이터를 조회하며
 * 페이징 처리를 통해 관리자 화면에서 효율적인 모니터링이 가능하도록 설계 하였습니다.
 * BidRepositoryImpl.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@RequiredArgsConstructor
public class BidRepositoryImpl implements BidRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	/**
	 * 관리자 입찰 모니터링 조건에 따라 입찰 목록을 조회합니다
	 * 전달된 파라민터가 null 인경우 해당 조건은 조회 조건에서 제외,
	 * QueryDSL의 동적 조건을 활용하여 유연한 검색 기능을 제공합니다.

	 * @param productId 조회할 상품ID
	 * @param categoryId 조회할 카테고리 ID
	 * @param status 입찰상태
	 * @param type 입찰 타입
	 * @param pageable 페이징 정보
	 * @param userId 특정 유저의 입찰 내역 조회
	 * @return 필터 조건에 맞는 입찰 목록의 페이징 결과
	 */
	@Override
	public Page<Bid> findAllByMonitoringFilter(Long productId, Long categoryId, String status, String type, Pageable pageable, Long userId) {
		List<Bid> content = queryFactory
			.selectFrom(bid)
			.leftJoin(bid.productOption, productOption).fetchJoin()
			.leftJoin(productOption.product, product).fetchJoin()
			.leftJoin(product.productCategory, productCategory).fetchJoin()
			.where(
				eqProductId(productId),
				eqCategoryId(categoryId),
				eqStatus(status),
				eqType(type),
				eqUserId(userId)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(bid.createdAt.desc())
			.fetch();

		Long total = queryFactory
			.select(bid.count()) // bid.count()로 변경
			.from(bid)           // selectFrom 대신 from 사용
			.where(
				eqProductId(productId),
				eqCategoryId(categoryId),
				eqStatus(status),
				eqType(type),
				eqUserId(userId)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	/**
	 * 입찰 상태 필터 조건
	 * @param status 입찰 상태 문자열
	 * @return 상태 조건 BooleanExpression, 값이 없으면 null
	 */
	private BooleanExpression eqStatus(String status) {
		return status != null ? bid.status.eq(BidStatus.valueOf(status)) : null;
	}

	/**
	 * 입찰 타입 필터 조건 생성
	 *
	 * @param type 입찰 타입 문자열
	 * @return 타입 조건 BooleanExpression, 값이 없으면 null
	 */
	private BooleanExpression eqType(String type) {
		return type != null ? bid.type.eq(BidType.valueOf(type)) : null;
	}

	/**
	 * 유저 ID 필터 조건 생성
	 *
	 * @param searchUserId 조회할 유저 ID
	 * @return 유저 조건 BooleanExpression, 값이 없으면 null
	 */
	private BooleanExpression eqUserId(Long searchUserId) {
		return searchUserId != null ? bid.user.id.eq(searchUserId) : null;
	}

	/**
	 * 상품 ID 필터 조건 생성
	 *
	 * @param productId 조회할 상품 ID
	 * @return 상품 조건 BooleanExpression, 값이 없으면 null
	 */
	private BooleanExpression eqProductId(Long productId) {
		return productId != null ? bid.productOption.product.id.eq(productId) : null;
	}

	/**
	 * 카테고리 ID 필터 조건 생성
	 *
	 * @param categoryId 조회할 카테고리 ID
	 * @return 카테고리 조건 BooleanExpression, 값이 없으면 null
	 */
	private BooleanExpression eqCategoryId(Long categoryId) {
		return categoryId != null ? bid.productOption.product.productCategory.id.eq(categoryId) : null;
	}
}
