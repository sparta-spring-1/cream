package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements BaseCode {

	PRODUCT_MODELNUMBER_CONFLICT(HttpStatus.CONFLICT,"이미 존재하는 모델번호입니다."),
	PRODUCT_NOTFOUND_CATEGORY(HttpStatus.NOT_FOUND,"존재하지 않는 카테고리입니다.");

	private final HttpStatus status;
	private final String message;
}
