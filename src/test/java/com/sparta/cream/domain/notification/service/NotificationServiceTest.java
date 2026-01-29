package com.sparta.cream.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.sparta.cream.domain.notification.entity.Notification;
import com.sparta.cream.domain.notification.repository.NotificationRepository;

import jakarta.transaction.Transactional;

/**
 * NotificationService의 핵심 비즈니스 로직을 검증하는 테스트 클래스입니다.
 * 알림 데이터의 생성, 저장 및 초기 상태 설정이 데이터 베이스 계층까지
 * 정상적으로 전달 도는지 통합 테스트르 수행합니다.
 * NotificationServiceTest.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "file:.env")
class NotificationServiceTest {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationRepository notificationRepository;

	/**
	 * 알림 생성 기능의 정상 동작 여부를 검증합니다.
	 * 검증사항:
	 * 입력된 사용자 ID와 메시지가 DB에 기록되는지 확인
	 * 알림 생성 직후의 초기 발송상태가 false인지 확인
	 * NotificationRepository를 통한 데이터 조회 정합성 확인
	 */
	@Test
	@DisplayName("알림 생성 테스트 - 데이터가 DB에 정상 적재되어야 함")
	void createNotification_Success() {
		// given
		Long userId = 1L;
		String message = "테스트 알림 메시지입니다.";

		// when
		notificationService.createNotification(userId, message);

		// then
		List<Notification> notifications = notificationRepository.findAll();
		Notification target = notifications.get(notifications.size() - 1);

		assertThat(target.getUserId()).isEqualTo(userId);
		assertThat(target.getMessage()).isEqualTo(message);
		assertThat(target.isSent()).isFalse();
	}
}
