package com.sparta.cream.domain.bid.event;

import com.sparta.cream.domain.notification.entity.NotificationType;

/**
 * 입찰 상태 변경 이벤트
 * 입찰 등록, 수정, 취소 시 알림 발송을 위해 사용됩니다.
 */
public record BidChangedEvent(
	Long userId,           // 알림을 받을 유저 ID
	NotificationType type, // 알림 타입
	String title,          // 알림 제목
	String message,        // 알림 내용
	Long bidId             // 관련 입찰 ID
) {}
