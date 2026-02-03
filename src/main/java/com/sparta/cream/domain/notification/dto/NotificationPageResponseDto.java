package com.sparta.cream.domain.notification.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 목록 페이징 응답 DTO
 * 페이징된 알림 목록과 페이징 정보를 담습니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class NotificationPageResponseDto {
	private final List<NotificationResponseDto> items;
	private final int page;
	private final int size;
	private final boolean hasNext;
}

