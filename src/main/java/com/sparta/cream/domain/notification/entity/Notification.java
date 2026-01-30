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

	@Column(nullable = false)
	private String message;

	@Column(nullable = false)
	private boolean isSent = false;

	private LocalDateTime sentAt;

	@Column(length = 100)
	private String title;

	@Column(length = 500)
	private String content;

	private LocalDateTime readAt;

	/**
	 * 새로운 알림 객체를 생성하기 위한 생성자입니다.
	 * 기본적으로 전송 여부는 false로 설정합니다.
	 * 기존 발송 로직과의 호환성을 위해 message를 title과 content로 자동 설정합니다.
	 * @param userId 알림 수신 대상자ID
	 * @param message 전송할 알림 내용
	 */
	public Notification(Long userId, String message) {
		this.userId = userId;
		this.message = message;
		this.title = message; // 기존 호환성: message를 title로 사용
		this.content = message; // 기존 호환성: message를 content로 사용
		this.isSent = false;
	}

	/**
	 * title과 content를 분리하여 알림을 생성하는 생성자입니다.
	 * @param userId 알림 수신 대상자ID
	 * @param title 알림 제목
	 * @param content 알림 내용
	 */
	public Notification(Long userId, String title, String content) {
		this.userId = userId;
		this.message = title; // 기존 호환성: title을 message로도 저장
		this.title = title;
		this.content = content;
		this.isSent = false;
	}

	/**
	 * 알림 전송이 완료되었을때 상태를 변경하고 전송 시간을 기록합니다.
	 * 이 메서드는 실제 메시지 전송 로직이 성공한 후 호출되어야 합니다.
	 */
	public void markAsSent() {
		this.isSent = true;
		this.sentAt = LocalDateTime.now();
	}

	/**
	 * 알림을 읽음 처리합니다.
	 * readAt 시간을 현재 시간으로 설정합니다.
	 */
	public void markAsRead() {
		this.readAt = LocalDateTime.now();
	}
}
