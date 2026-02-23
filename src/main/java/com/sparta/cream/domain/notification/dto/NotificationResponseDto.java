package com.sparta.cream.domain.notification.dto;

import java.time.LocalDateTime;
import com.sparta.cream.domain.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 조회 응답 DTO
 * 사용자에게 전달되는 알림 정보를 담습니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
	private Long id;
	private Long userId;
	private Long tradeId;
	private String title;
	private String message;
	private LocalDateTime createdAt;
	private LocalDateTime readAt;

	/**
	 * 엔티티를 DTO로 변환할 때 사용하기 편하도록 만든 생성자입니다.
	 * 필드 유효성 검사 및 기본값 설정을 통해 클라이언트에게 안전한 데이터를 전달합니다.
	 */
	public NotificationResponseDto(Notification notification) {
		this.id = notification.getId();
		this.userId = notification.getUserId();
		this.tradeId = notification.getTradeId();
		this.title = (notification.getTitle() != null && !notification.getTitle().isBlank())
			? notification.getTitle() : "알림";
		this.message = (notification.getMessage() != null && !notification.getMessage().isBlank())
			? notification.getMessage() : "새로운 알림이 도착했습니다.";
		this.createdAt = notification.getCreatedAt();
		this.readAt = notification.getReadAt();
	}
}

