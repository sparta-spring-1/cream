package com.sparta.cream.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.cream.domain.status.PaymentStatus;
import com.sparta.cream.domain.trade.entity.Trade;
import com.sparta.cream.entity.BaseEntity;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.PaymentErrorCode;

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
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 정보와 상태를 관리하는 Entity입니다.
 * <p>
 * 체결 1건에 관한 결제 금액, Portone 연동 정보, 결제 수단 등을 기록하며,
 * 결제 상태와 생성/결제 성공/수정 일시를 관리합니다.
 * </p>
 *
 * @author 변채주
 * @version 1.1
 * @since 2026. 01. 26.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "merchant_uid", nullable = false, length = 100)
	private String merchantUid;

	@Column(name = "imp_uid", length = 100)
	private String impUid;

	@Column(name = "product_name", nullable = false, length = 100)
	private String productName;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(length = 50)
	private String method;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trade_id")
	private Trade trade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	/**
	 * 결제 준비(Prepare) 단계에서 사용되는 생성자입니다.
	 *
	 * @param merchantUid 상점 거래 고유 번호
	 * @param productName 상품명
	 * @param amount      결제 금액
	 * @param status      초기 상태 (READY)
	 */
	public Payment(String merchantUid, String productName, BigDecimal amount, PaymentStatus status, Trade trade, Users user) {
		this.merchantUid = merchantUid;
		this.productName = productName;
		this.amount = amount;
		this.status = status;
		this.trade = trade;
		this.user = user;
	}

	/**
	 * 전체 필드를 초기화하는 빌더 생성자입니다.
	 *
	 * @param merchantUid    상점 거래 고유 번호
	 * @param impUid        PortOne 결제 고유 번호
	 * @param productName    상품명
	 * @param amount        결제 금액
	 * @param status        결제 상태
	 * @param method        결제 수단
	 * @param paidAt        결제 완료 일시
	 */
	@Builder
	private Payment(String merchantUid,
		String impUid,
		String productName,
		BigDecimal amount,
		PaymentStatus status,
		String method,
		LocalDateTime paidAt,
		Trade trade,
		Users user) {
		this.merchantUid = merchantUid;
		this.impUid = impUid;
		this.productName = productName;
		this.amount = amount;
		this.status = status;
		this.method = method;
		this.paidAt = paidAt;
		this.trade = trade;
		this.user = user;
	}

	/**
	 * 결제 상태를 안전하게 변경하고 변경 이력을 반환합니다.
	 * <p>
	 * 동시성 이슈를 방지하기 위해 입력되는 이전 상태(prevStatus)를 검증하며,
	 * 이미 취소되거나 환불된 건에 대해서는 상태 변경을 거부합니다.
	 * 이미 결제 완료된 건은 결제 준비(READY) 또는 결제 중(PENDING) 상태로 변경을 거부합니다.
	 * </p>
	 *
	 * @param prevStatus    변경 전 기대하는 현재 상태
	 * @param newStatus    변경할 새로운 상태
	 * @return 상태 변경 이력 객체 (PaymentHistory)
	 * @throws BusinessException 현재 상태가 기대값과 다르거나, 변경 불가능한 상태일 경우
	 */
	public PaymentHistory changeStatus(PaymentStatus prevStatus, PaymentStatus newStatus) {
		validateStatus(prevStatus, newStatus);
		this.status = newStatus;

		return new PaymentHistory(prevStatus, newStatus, this.amount, this);
	}

	private void validateStatus(PaymentStatus expectedPrevStatus, PaymentStatus newStatus) {
		if (this.status != expectedPrevStatus) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_CONFLICT);
		}

		if (this.status == PaymentStatus.CANCELLED || this.status == PaymentStatus.FULL_REFUNDED) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_CONFLICT);
		}

		if (this.status == PaymentStatus.PAID_SUCCESS &&
			(newStatus == PaymentStatus.PENDING || newStatus == PaymentStatus.READY)) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_PAID);
		}
	}

	public PaymentHistory completePayment(String impUid, String method, PaymentStatus status) {
		PaymentHistory changed = changeStatus(status, PaymentStatus.PAID_SUCCESS);
		this.impUid = impUid;
		this.method = method;
		this.paidAt = LocalDateTime.now();

		return changed;
	}

	public PaymentHistory refund() {
		if (this.status == PaymentStatus.FULL_REFUNDED) {
			throw new BusinessException(PaymentErrorCode.ALREADY_REFUNDED);
		}
		if (this.status != PaymentStatus.PAID_SUCCESS) {
			throw new BusinessException(PaymentErrorCode.PAYMENT_STATUS_MISMATCH);
		}

		PaymentHistory refunded = changeStatus(PaymentStatus.PAID_SUCCESS, PaymentStatus.FULL_REFUNDED);

		return refunded;
	}
}
