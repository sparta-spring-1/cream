package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements BaseCode {
	SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정산 내역을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
