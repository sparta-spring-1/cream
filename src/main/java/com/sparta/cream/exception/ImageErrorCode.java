package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이미지 도메인에서 발생하는 예외 상황을 정의한 에러 코드 enum입니다.
 * <p>
 * 본 에러 코드는 전역 예외 처리(GlobalExceptionHandler)와 연동되어
 * 클라이언트에 명확한 오류 원인과 상태 코드를 전달하는 역할을 합니다.
 * </p>
 *  @author heoarim
 *  @since 2026. 2. 2
 */
@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements BaseCode {
	NOT_EXIST_FILE(HttpStatus.BAD_REQUEST, "존재하지 않는 이미지입니다."),
	NOT_EXIST_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "확장자가 없습니다."),
	INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 확장자를 사용했습니다."),
	IO_EXCEPTION_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 도중 에러가 발생했습니다.");

	private final HttpStatus status;
	private final String message;
}
