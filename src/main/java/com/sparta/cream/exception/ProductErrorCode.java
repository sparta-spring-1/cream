package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements BaseCode {

	PRODUCT_MODELNUMBER_CONFLICT(HttpStatus.CONFLICT,"이미 존재하는 모델번호입니다."),
	PRODUCT_NOT_FOUND_ID(HttpStatus.NOT_FOUND,"존재하지 않는 상품입니다."),
	PRODUCT_NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND,"존재하지 않는 카테고리입니다."),
	PRODUCT_IMAGE_SIZE_LIMIT(HttpStatus.BAD_REQUEST,"이미지는 최대 10개까지 저장할 수 있습니다."),
	PRODUCT_CANNOT_DELETE_ON_SALE(HttpStatus.BAD_REQUEST,"거래 중인 상품은 삭제할 수 없습니다.");
	private final HttpStatus status;
	private final String message;
}
