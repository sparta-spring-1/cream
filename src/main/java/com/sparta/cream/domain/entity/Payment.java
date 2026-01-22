package com.sparta.cream.domain.entity;

import java.time.LocalDateTime;

import com.sparta.cream.domain.entity.common.BaseTimeEntity;
import com.sparta.cream.domain.status.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 정보와 상태을 나타내는 Entity입니다.
 * <p>
 * 체결 1건에 관한 결제 금액, Portone 연동 정보, 결제 수단 등을 기록하며,
 * 결제 상태와 생성/결제 성공/수정 일시를 관리합니다.
 * 결제 생성 시 Portone V2 연동을 위해 merchantUid, amount, status만 입력됩니다.
 * 결제 완료 후 검증을 통과하면 기존 결제 정보의 impUid, status, method, paidAt이 저장됩니다.
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 22.
 */

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Payment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "merchant_uid", nullable = false, length = 100)
	private String merchantUid;

	@Column(name = "imp_uid", length = 100)
	private String impUid;

	@Column(nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(nullable = false, length = 50)
	private String method;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	/*TODO 연관관계 맞추기
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trade_id")
	private Trade trade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;
	*/
}
