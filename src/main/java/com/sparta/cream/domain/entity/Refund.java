package com.sparta.cream.domain.entity;

import com.sparta.cream.entity.BaseEntity;

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
 * 결제에 대한 환불 상세 정보를 기록하는 Entity입니다.
 * <p>
 * 특정 결제 건에 대해 환불이 발생했을 때 환불 사유와 금액 등의 정보를 저장하며,
 * 환불 처리와 상태 변경 이력이 원자적으로 관리될 수 있도록 {@link PaymentHistory}와 연관관계를 맺습니다.
 * 이는 추후 발생할 수 있는 부분 환불 건에 대한 개별 추적과 이력 관리를 용이하게 하기 위함입니다.
 * </p>
 *
 * @author 변채주
 * @version 1.1
 * @since 2026. 01. 26.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Refund extends BaseEntity {

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

    /**
     * Refund 생성자.
     *
     * @param reason         환불 사유
     * @param amount         환불 금액
     * @param paymentHistory 연관된 결제 이력
     */
    public Refund(String reason, Long amount, PaymentHistory paymentHistory) {
        this.reason = reason;
        this.amount = amount;
        this.paymentHistory = paymentHistory;
    }
}
