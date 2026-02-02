package com.sparta.cream.domain.notification.entity;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.cream.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 발송을 자동화하고 주기적으로 처리하는 스케줄러 클래스입니다.
 * 전송대기 상태(false)인 알림들을 주기적으로 확인하여
 * 실제 사용자에게 발송처리를 수행하고 상태를 갱신하는 역할을 합니다.
 * NotificationScheduler.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

	private final NotificationService notificationService;

	/**
	 * 발송 대기 중인 알림을 주기적으로 조회하여 전송 프로세스를 실행합니다.
	 * {@code fixedDelay = 5000} 설정에 따라 이전 작업이 종료된 시점으로부터
	 * 5초후에 다음 작업이 시작됩니다.
	 */
	@Scheduled(fixedDelay = 5000)
	public void pollNotifications() {
		List<Notification> pendingList = notificationService.getPendingNotifications();

		if (pendingList.isEmpty()) {
			return;
		}

		log.info("발송 대기 중인 알림 {}건 처리 시작", pendingList.size());

		for (Notification notification : pendingList) {
			try {
				sendNotification(notification);

				notificationService.markAsSent(notification.getId());
			} catch (Exception e) {
				log.error("알림 발송 실패 - ID: {}, 사유: {}", notification.getId(), e.getMessage());
			}
		}
	}

	/**
	 * 생성된 알림을 시스템 내부에 최종 반영합니다.
	 * 현재는 외부 발송 대신 사용자가 마이페이지에서
	 * 알림을 조회할 수 있도록 데이터 상태를 확정하는 역할을 수행합니다.
	 * 추후 실시간 알림(SSE) 연동시 이 메서드에서 푸시 로직을 추가할 할 있습니다
	 * @param notification 시스템 내부에 확정할 알림 엔티티 객체
	 */
	private void sendNotification(Notification notification) {
		log.info("[알림 발송 완료] 전송 대상 유저: {}, 메시지: {}",
			notification.getUserId(), notification.getMessage());
	}
}
