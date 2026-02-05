package com.sparta.cream.domain.trade.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.cream.domain.bid.entity.QBid;
import com.sparta.cream.domain.trade.entity.QTrade;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.domain.trade.entity.TradeStatus;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 체결(Trade) 모니터링 조회를 위한 QueryDSL 기반 커스텀 레포지토리입니다.
 * 거래 상태 및 특정유저의 거래 내역을
 * 동적 조건으로 조회하며
 * 관리자 화면에서 거래 흐름을 효휼적으로 모니터링 할수 있도록 설계하였습니다.
 * TradeRepositoryImpl.java
 *
 * @author kimsehyun
 * @since 2026. 1. 30.
 */
@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	/**
	 * 관리자 거래 모니터링 조건에 따른 거래 목록 조회
	 * 거래 상태와 특정 유저 조건을 기반으로 데이터 조회하며,
	 * 유저 조건의 경우 판매자, 구매자로 참여한 거래를 모두 포합합니다
	 * @param status 거래 상태
	 * @param userId 특정유저의 거래 조회
	 * @param pageable 페이징 정보
	 * @return 필터 조건에 맞는 거래 목록
	 */
	@Override
	public Page<Trade> findAllByTradeFilter(String status, Long userId, Pageable pageable) {
		QTrade t = QTrade.trade;
		QBid pBid = new QBid("purchaseBid");
		QBid sBid = new QBid("saleBid");

		List<Trade> content = queryFactory
			.selectFrom(t)
			.leftJoin(t.purchaseBidId, pBid).fetchJoin()
			.leftJoin(t.saleBidId, sBid).fetchJoin()
			.where(
				eqStatus(status),
				eqUserId(userId)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(t.id.desc())
			.fetch();

		Long total = queryFactory
			.select(t.count()) // QTrade 인스턴스인 't'의 count를 구함
			.from(t)
			.where(
				eqStatus(status),
				eqUserId(userId)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	/**
	 * 거래 상태 필터 조건 생성
	 * 전달된 상태 값이 유요하지 않는 경우
	 * 예외 발생시키지 않고 해당 조건 무시
	 * @param status 거래 상태
	 * @return 거래 상태 조건 BooleanExpression, 값이 없거나 유효하지 않으면 null
	 */
	private BooleanExpression eqStatus(String status) {
		if (status == null || status.isEmpty()) return null;
		try {
			return QTrade.trade.status.eq(TradeStatus.valueOf(status.toUpperCase()));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * 유저 ID 필터 조건 생성
	 * 판매자 또는 구매자로 참여한 거래를 모두 조회하기 위해
	 * OR 조건으로 필터를 구성합니다.
	 * @param userId 조회할 유저 ID
	 * @return 유저 조건 BooleanExpression, 값이 없으면 null
	 */
	private BooleanExpression eqUserId(Long userId) {
		if (userId == null) return null;
		return QTrade.trade.purchaseBidId.user.id.eq(userId)
			.or(QTrade.trade.saleBidId.user.id.eq(userId));
	}
}
