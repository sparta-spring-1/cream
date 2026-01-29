package com.sparta.cream.domain.status;

/**
 * 결제 상태을 나타내는 열거형 클래스입니다.
 * <p>
 * READY: 결제 준비 중(Portone 전송 대기 중)
 * PENDING: Portone 결제 대기 중
 * CANCELLED: 체결(결제)이 취소되었음
 * PAID_SUCCESS: 결제 성공
 * PAID_FAIL: 결제 실패, 재시도 필요
 * FULL_REFUNDED: 전액 환불
 * </p>
 *
 * @author 변채주
 * @version 1.1
 * @since 2026. 01. 26.
 */
public enum PaymentStatus {
	READY,
	PENDING,
	CANCELLED,
	PAID_SUCCESS,
	PAID_FAIL,
	FULL_REFUNDED
}
