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
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 user 에 권한이 없습니다");

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
