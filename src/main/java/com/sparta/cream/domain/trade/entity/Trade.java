package com.sparta.cream.domain.trade.entity;

import java.time.LocalDateTime;

import com.sparta.cream.domain.bid.entity.Bid;
import com.sparta.cream.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 체결(Trade) 엔티티 입니다.
 * 구매 입찰과 판매 입찰이 매칭되어 성사된 거래 정보를 관리합니다.
 * 거래 생성 시점부터 거래 상태를 추적합니다.
 * Trade.java
 *
 * @author kimsehyun
 * @since 2026. 1. 28.
 */
@Entity
@Getter
@NoArgsConstructor
public class Trade extends BaseEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_bid_id")
	private Bid purchaseBidId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sale_bid_id")
	private Bid saleBidId;

	private Long finalPrice;

	@Enumerated(EnumType.STRING)
	private TradeStatus status;

	private LocalDateTime completedAt;

	/**
	 * 새로운 거래르 ㄹ생성합니다.
	 * 초기 상태는 {@link TradeStatus#WAITING_PAYMENT}로 설정됩니다.
	 * @param purchaseBid 체결된 구매 입찰 객체
	 * @param saleBid 체결된 판매 입찰 객체
	 * @param finalPrice 최종 결정된 거래 가격
	 */
	public Trade(Bid purchaseBid, Bid saleBid, Long finalPrice) {
		this.purchaseBidId = purchaseBid;
		this.saleBidId = saleBid;
		this.finalPrice = finalPrice;
		this.status = TradeStatus.WAITING_PAYMENT;
	}

	/**
	 * 사용자의 결제가 완료되었을 때 거래 상태를 변경합니다.
	 * 상태를 {@link TradeStatus#PAYMENT_COMPLETED}로 변경하고 완료 시간을 기록합니다.
	 */
	public void completePayment() {
		this.status = TradeStatus.PAYMENT_COMPLETED;
		this.completedAt = LocalDateTime.now();
	}
}

