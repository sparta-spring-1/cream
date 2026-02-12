package com.sparta.cream.domain.entity;

import java.math.BigDecimal;

import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 결제 상태의 변경 이력을 기록하는 Entity입니다.
 * <p>
 * 특정 결제 건에 대한 상태 변화가 발생할 때마다 이전 상태와 새로운 상태를 기록하여
 * 결제 흐름을 추적할 수 있도록 관리합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.1
 * @since 2026. 01. 26.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PaymentHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "prev_status", nullable = false)
	private PaymentStatus prevStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false)
	private PaymentStatus newStatus;

	@Column(nullable = false)
	private BigDecimal amount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

    /**
     * PaymentHistory 생성자.
     *
     * @param prevStatus 변경 전 상태
     * @param newStatus  변경 후 상태
     * @param amount     당시 금액
     * @param payment    결제 엔티티
     */
    public PaymentHistory(PaymentStatus prevStatus, PaymentStatus newStatus, BigDecimal amount, Payment payment) {
        this.prevStatus = prevStatus;
        this.newStatus = newStatus;
        this.amount = amount;
        this.payment = payment;
    }
}
