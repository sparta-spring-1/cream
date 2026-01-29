package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

/**
 * 입찰 도메인에서 발생하는 비즈니스 예외코드를 정희한 Enum 클래ㅐ스 입니다.
 * 각 에러코드는 {@link BaseCode}를 구현하며,
 *  HTTP 상태 코드({@link HttpStatus})와 사용자에게 전달할 메시지를 함께 관리합니다.
 * BidErrorCode.java
 *
 * @author kimsehyun
 * @since 2026. 1. 26.
 */
public enum BidErrorCode implements BaseCode {

	PRODUCT_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품 옵션(사이즈)입니다."),
	INVALID_BID_PRICE(HttpStatus.BAD_REQUEST, "입찰 가격은 0원보다 커야 합니다."),
	BID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 입찰 정보를 찾을 수 없습니다."),
	NOT_YOUR_BID(HttpStatus.FORBIDDEN, "본인이 등록한 입찰만 수정하거나 취소할 수 있습니다."),
	CANNOT_UPDATE_BID(HttpStatus.BAD_REQUEST, "이미 체결되었거나 취소된 입찰은 수정할 수 없습니다."),
	CANNOT_CANCEL_NON_PENDING_BID(HttpStatus.BAD_REQUEST,"대기 상태의 입찰만 취소할 수 있습니다."),
	BID_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 입찰입니다"),
	CANNOT_CANCEL_UNMATCHED(HttpStatus.BAD_REQUEST, "체결 완료(MATCHED) 상태인 입찰만 체결 취소가 가능합니다.");

	private final HttpStatus status;
	private final String message;

	BidErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * 에러에  매핑된 HTTP 상태코드를 반환합니다.
	 * @return HTTP 상태 코드
	 */
	@Override
	public HttpStatus getStatus() {
		return this.status;
	}

	/**
	 * 에러 발새ㅐㅇ시 클라이이언트에게 전달할  메시지를 반환합니다.
	 * @return 에러메시지
	 */
	@Override
	public String getMessage() {
		return this.message;
	}
}
