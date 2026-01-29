package com.sparta.cream.domain.notification.service;

import java.util.List;

import com.sparta.cream.domain.notification.entity.Notification;
import com.sparta.cream.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스의 핵심 비즈니스 로직을 처리하는 서비스 글래스입니다.
 * 알림 생성, 미발송 목록 조회, 발송 상태 갱신등의 기능을 제공하며
 * 주로 거래 체결 서비스나 스케줄러와 협업하여 작동합니다.
 * NotificationService.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepository;

	/**
	 * 새로운 알림을 생성하고 데이터 베이스에 저장합니다.
	 * 주로 거래 체결 성공시 호출되며, 구매자와 판매자 각각의
	 * UserId를 기반으로 개별적인 알림 메시지를 생성할 . 사용됩니다.
	 * @param userId 알림을 수신할 사용자 식별자
	 * @param message 사용자에게 전달할 알림 메시지 내용
	 */
	@Transactional
	public void createNotification(Long userId, String message) {
		Notification notification = new Notification(userId, message);
		notificationRepository.save(notification);
	}

	/**
	 * 아직 사용자에게 전달되지 않는 알림 목록을 조회합니다.
	 * 주기적으로 호출되어 배치 처리의 대상이 됩니다
	 * @return 발송 대기중인 객체 리스트
	 */
	@Transactional(readOnly = true)
	public List<Notification> getPendingNotifications() {
		return notificationRepository.findAllByIsSentFalse();
	}

	/**
	 * 특정 알림의 상태를 '발송완료'로 업데이트 합니다
	 * 알림 데이터가 마이페이지 노풀 준비를 마쳤거나, 실시간 푸시 발송에
	 * 성공했을때 호출하여 중복 발송을 방지합니다.
	 * @param notificationId 상태를 변경할 알림의 고유 식별자
	 */
	@Transactional
	public void markAsSent(Long notificationId) {
		notificationRepository.findById(notificationId)
			.ifPresent(Notification::markAsSent);
	}
}
