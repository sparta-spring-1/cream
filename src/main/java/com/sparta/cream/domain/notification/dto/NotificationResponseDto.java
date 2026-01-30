package com.sparta.cream.domain.notification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 조회 응답 DTO
 * 사용자에게 전달되는 알림 정보를 담습니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class NotificationResponseDto {
	private final Long id;
	private final String title;
	private final String content;
	private final LocalDateTime createdAt;
	private final LocalDateTime readAt;
}

