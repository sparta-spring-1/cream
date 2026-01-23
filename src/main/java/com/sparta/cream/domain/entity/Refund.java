package com.sparta.cream.domain.entity;

import com.sparta.cream.domain.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제에 대한 환불 정보를 기록하는 Entity입니다.
 * <p>
 * 특정 결제 건에 대한 환불이 발생했을 때, 환불 사유와 금액 등의 정보를 저장하여
 * 환불 내역을 관리합니다.
 * 전액 환불만 가능합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 22.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Refund extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 500)
	private String reason;

	@Column(nullable = false)
	private Long amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_history_id", nullable = false)
	private PaymentHistory paymentHistory;

	public Refund(String reason, Long amount, PaymentHistory paymentHistory) {
		this.reason = reason;
		this.amount = amount;
		this.paymentHistory = paymentHistory;
	}
}
