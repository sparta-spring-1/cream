package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseCode {

	PAYMENT_CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 결제 정보입니다."),
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보입니다."),

	PAYMENT_STATUS_MISMATCH(HttpStatus.BAD_REQUEST, "결제 정보 상태가 일치하지 않습니다."),
	PAYMENT_ALREADY_PAID(HttpStatus.BAD_REQUEST, "이미 결제 완료된 건입니다."),
	PAYMENT_STATUS_IMMUTABLE(HttpStatus.BAD_REQUEST, "취소 또는 환불 처리된 결제는 변경이 불가합니다."),
	PAYMENT_PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
	PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "결제 검증에 실패했습니다."),

	REFUND_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "환불 가능 금액보다 요청 금액이 큽니다. (초과 환불 불가)"),
	UNAUTHORIZED_REFUND(HttpStatus.FORBIDDEN, "판매자와 관리자만 환불을 요청할 수 있습니다."),
	ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "이미 환불이 완료된 거래입니다."),

	PORTONE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "포트원 API 호출 중 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;
}
