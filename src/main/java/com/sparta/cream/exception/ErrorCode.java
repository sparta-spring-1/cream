package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseCode {

	//@Validation 유효성 검사 실패시 처리
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
	//common
	INVALID_JSON(HttpStatus.BAD_REQUEST,"요청값 파싱에 실패하였습니다."),
	INVALID_JSON_ENUM(HttpStatus.BAD_REQUEST,"올바른 Enum 값을 입력해주세요."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "입력값을 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "정의되지 않은 서버 오류가 발생했습니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 user 에 권한이 없습니다"),
	//auth
	AUTH_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	AUTH_REFRESH_STORE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh Token 저장 중 오류가 발생했습니다."),
	AUTH_TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생했습니다.");
	AUTH_TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생했습니다."),
	AUTH_REFRESH_STORE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh Token 저장 중 오류가 발생했습니다."),
	AUTH_REDIS_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Redis 연결에 실패했습니다."),
	AUTH_USER_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 저장 중 오류가 발생했습니다."),

	//Bid(입찰)관련
	PRODUCT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "상품 ID가 누락되었습니다."),
	PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품 옵션(사이즈)입니다."),
	INVALID_BID_PRICE(HttpStatus.BAD_REQUEST, "입찰 가격은 0원보다 커야 합니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	BID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 입찰 정보를 찾을 수 없습니다."),
	NOT_YOUR_BID(HttpStatus.FORBIDDEN, "본인이 등록한 입찰만 수정하거나 취소할 수 있습니다."),
	CANNOT_UPDATE_BID(HttpStatus.BAD_REQUEST, "이미 체결되었거나 취소된 입찰은 수정할 수 없습니다."),
	CANNOT_CANCEL_UNMATCHED(HttpStatus.BAD_REQUEST, "체결 완료(MATCHED) 상태인 입찰만 체결 취소가 가능합니다."),
	CANNOT_CANCEL_NON_PENDING_BID(HttpStatus.BAD_REQUEST,"대기 상태의 입찰만 취소할 수 있습니다."),
	BID_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 입찰입니다");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public HttpStatus getStatus() {
		return this.status;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
