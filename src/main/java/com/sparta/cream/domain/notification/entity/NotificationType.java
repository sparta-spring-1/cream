package com.sparta.cream.domain.notification.entity;

/**
 * 시스템에서 발행되는 알림의 유형을 정의 하는 클래스입니다.
 * NotificationType.java
 *
 * @author kimsehyun
 * @since 2026. 2. 12.
 */
public enum NotificationType {
	BID_REGISTERED, //입찰등록
	BID_UPDATED, //입찰수정
	TRADE_MATCH, //체결완료
	BID_CANCELLED, //입찰 취소
	TRADE_CANCELLED,//체결취소
	PAYMENT_COMPLETE //결제완료
}
