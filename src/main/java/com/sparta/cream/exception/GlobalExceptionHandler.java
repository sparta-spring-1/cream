package com.sparta.cream.exception;

import static org.springframework.http.HttpStatus.*;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	/**
	 * 1. @Valid 유효성 검사 실패 시 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonErrorResponse<Map<String, String>>> handleValidationExceptions(
		MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		// 각 필드별 오류 메시지를 Map에 저장
		ex.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});

		return ResponseEntity
			.status(BAD_REQUEST)
			.body(CommonErrorResponse.of(ErrorCode.VALIDATION_ERROR, errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<CommonErrorResponse<String>> handleHttpMessageNotReadable(
		HttpMessageNotReadableException ex
	) {
		String errorDetail = ex.getMessage();

		// Enum 파싱 에러인 경우 더 자세한 메시지
		if (errorDetail != null && errorDetail.contains("Enum")) {
			String detailMessage = "올바른 Enum 값을 입력해주세요. 에러: " + errorDetail;
			return ResponseEntity
				.status(BAD_REQUEST)
				.body(CommonErrorResponse.of(ErrorCode.INVALID_JSON, detailMessage));
		}

		return ResponseEntity
			.status(BAD_REQUEST)
			.body(CommonErrorResponse.of(ErrorCode.INVALID_JSON));
	}


	/**
	 * 404 Not Found
	 * URL 자체가 없을 때
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<CommonErrorResponse<Void>> handleNoResourceFoundException(
		NoResourceFoundException ex) {

		return ResponseEntity
			.status(NOT_FOUND)
			.body(CommonErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonErrorResponse<Object>> handleServerError(Exception ex) {
		log.error("Unhandled Exception occurred: ", ex);

		return ResponseEntity
			.status(INTERNAL_SERVER_ERROR)
			.body(CommonErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	/**
	 * 비즈니스 예외 처리 (도메인별 ErrorCode 모두 처리)
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<CommonErrorResponse<Void>> handleBusinessException(
		BusinessException ex) {

		log.warn("Business logic exception: {} - {}", ex.getErrorCode(), ex.getMessage());

		return ResponseEntity
			.status(ex.getErrorCode().getStatus())
			.body(CommonErrorResponse.of(ex.getErrorCode()));
	}

	/**
	 * JPA 낙관적 락(Optimistic Lock) 충돌 발생 시 예외를 처리합니다.
	 * 동시에 동일한 입찰/거래 자원에 접근하여 데이터 수정이 경합을 벌일 때 발생하며,
	 * 사용자에게는 현재 데이터가 이미 변경되었음을 알리기 위해 409 Conflict 상태코드를 반환합니다.
	 *
	 * @param ex 낙관적 락 실패 예외 객체
	 * @return 409 상태코드와 비즈니스 에러 메시지를 담은 응답 객체
	 */
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<CommonErrorResponse<Void>> handleOptimisticLockingFailureException(
		ObjectOptimisticLockingFailureException ex) {

		log.warn("낙관적 락 충돌 발생: {}", ex.getMessage());

		return ResponseEntity
			.status(HttpStatus.CONFLICT)
			.body(CommonErrorResponse.of(BidErrorCode.CANNOT_CANCEL_NON_PENDING_BID));
	}
}
