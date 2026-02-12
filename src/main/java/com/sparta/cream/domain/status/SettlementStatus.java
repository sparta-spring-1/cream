package com.sparta.cream.domain.status;

/**
 * 정산 상태을 나타내는 열거형 클래스입니다.
 * <p>
 * PENDING: 정산 대기 중
 * COMPLETED: 정산 완료
 * FAILED: 정산 실패(오류 발생 등의 사유로)
 * </p>
 *
 * @author 변채주
 * @version 1.0
 * @since 2026. 01. 23.
 */

public enum SettlementStatus {
    PENDING,
    COMPLETED,
    FAILED,
	REFUNDED
}
