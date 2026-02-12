package com.sparta.cream.domain.entity;

import java.time.LocalDateTime;

import com.sparta.cream.domain.status.SettlementStatus;
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
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정산 정보를 나타내는 Entity입니다.
 * <p>
 * 결제 건에 대한 결제 금액, 수수료, 최종 정산 금액(판매자 지급) 및 정산 상태를 기록하며,
 * 정산 생성일과 정산 완료일을 관리합니다.
 * 판매자에게 지급될 최종 금액을 계산하고 추적하는 데 사용됩니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 23.
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(nullable = false)
    private Long feeAmount;

	@Column(nullable = false)
    private Long settlementAmount;

	@Column(nullable = false)
	private Long totalAmount;

	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

	private LocalDateTime settledAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	/* TODO: Users 엔티티 생성 후 연관관계 설정
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id", nullable = false)
	private Users seller;
	*/

    /**
     * Settlement 생성자.
     * <p>생성 시점에 총 금액(totalAmount)은 수수료와 정산 금액의 합으로 자동 계산되어 설정됩니다.</p>
     *
     * @param feeAmount        수수료
     * @param settlementAmount 실 지급액
     * @param status           초기 상태
     * @param payment          결제 정보
     */
    public Settlement(Long feeAmount, Long settlementAmount, SettlementStatus status, Payment payment) {
        this.feeAmount = feeAmount;
        this.settlementAmount = settlementAmount;
        this.totalAmount = feeAmount + settlementAmount;
        this.status = status;
        this.payment = payment;
    }
}
