package com.sparta.cream.domain.notification.entity;

import com.sparta.cream.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 사용자에게 전다뢰는 알림 정보를 관리하는 엔티티 클래스입니다.
 * 입찰 매칭성공, 결제 완료등 시스템 내의 주요 이벤트를 사용자에게 알리기 위해 사용됩니다.
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notifications")
public class Notification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationType type;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 500)
	private String message;

	private Long tradeId;

	@Column(nullable = false)
	private boolean isSent = false;

	private LocalDateTime sentAt;

	@Column(nullable = false)
	private boolean isRead = false;

	private LocalDateTime readAt;

	/**
	 * title과 content를 분리하여 알림을 생성하는 생성자입니다.
	 * @param userId 알림 수신 대상자ID
	 * @param type 알림 타입
	 * @param title 알림 제목
	 * @param message 알림 내용
	 * @param tradeId 체결 ID
	 */
	public Notification(Long userId, NotificationType type, String title, String message, Long tradeId
	) {
		this.userId = userId;
		this.type = type;
		this.title = title;
		this.message = message;
		this.tradeId = tradeId;
	}

	/**
	 * 알림 전송이 완료되었을때 상태를 변경하고 전송 시간을 기록합니다.
	 * 이 메서드는 실제 메시지 전송 로직이 성공한 후 호출되어야 합니다.
	 */
	public void markAsSent() {
		this.isSent = true;
		this.sentAt = LocalDateTime.now();
	}
}
