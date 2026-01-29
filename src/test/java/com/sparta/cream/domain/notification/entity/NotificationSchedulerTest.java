package com.sparta.cream.domain.notification.entity;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.sparta.cream.domain.notification.repository.NotificationRepository;
import com.sparta.cream.domain.notification.service.NotificationService;

import jakarta.transaction.Transactional;

/**
 * NotificationScheduler의 알림 폴링 및 상태 업데이트 기능을 검증하는 테스트 클래스입니다.
 * 실제 스프링 컨텍스트를 로드하여, 서비스, 레포지토리, 스케줄러 간의 동합 동작을 확인합니다.
 * NotificationSchedulerTest.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "file:.env")
class NotificationSchedulerTest {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationScheduler notificationScheduler;

	@Autowired
	private NotificationRepository notificationRepository;

	/**
	 * 스케줄러의 알림 처리 흐름을 테스트합니다
	 * 새로운 알림을 생성하여 DB에 저장
	 * 스케줄러의 폴링 메서드를 수동 호출하여 전송 로직 수행
	 * DB 재조회시 해당 알림의 전송항태가 true로 변경되었는지 검증
	 * 전송 완료시간이 정상적으로 기록되었는지 확인
	 */
	@Test
	@DisplayName("스케줄러 폴링 테스트 - 미발송 알림이 발송 완료 상태로 변경되어야 함")
	void pollNotifications_UpdateStatus() {
		// given
		notificationService.createNotification(1L, "스케줄러 테스트용");

		// when:
		notificationScheduler.pollNotifications();

		// then
		List<Notification> results = notificationRepository.findAll();
		Notification updatedNotification = results.get(results.size() - 1);

		assertThat(updatedNotification.isSent()).isTrue();
		assertThat(updatedNotification.getSentAt()).isNotNull();
	}
}
