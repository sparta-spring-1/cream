package com.sparta.cream.domain.notification.service;

import java.util.List;

import com.sparta.cream.domain.notification.dto.NotificationResponseDto;
import com.sparta.cream.domain.notification.entity.Notification;
import com.sparta.cream.domain.notification.entity.NotificationType;
import com.sparta.cream.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
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
	private final RedisTemplate<String, NotificationResponseDto> notificationRedisTemplate;

	/**
	 * 새로운 알림을 생성하여 DB에 저장하고, 실시간 전송을 위해 Redis 채널로 이벤트를 발행합니다.
	 * 입찰, 체결, 결제 등 시스템 내의 다양한 도메인 이벤트를 사용자 알림 데이터로 변환하여 기록하며,
	 * 저장 직후 {@link #publish(NotificationResponseDto)}를 호출하여
	 * 접속 중인 사용자에게 실시간 푸시(SSE)가 전달되도록 합니다.
	 *
	 * @param userId    알림을 수신할 사용자의 식별자
	 * @param type      알림의 유형 (입찰, 체결, 결제 등)
	 * @param title     알림 제목
	 * @param message   알림 본문 내용
	 * @param tradeId   관련 거래/입찰 식별자 (필요 시 참조용으로 저장)
	 * @return 저장된 {@link Notification} 엔티티 객체
	 */
	@Transactional
	public Notification createNotification(
		Long userId,
		NotificationType type,
		String title,
		String message,
		Long tradeId
	) {
		Notification notification = new Notification(userId, type, title, message, tradeId);

		notificationRepository.save(notification);

		NotificationResponseDto dto = new NotificationResponseDto(notification);

		publish(dto);

		return notification;
	}

	/**
	 * 알림 데이터를 Redis Pub/Sub 채널로 발행합니다.
	 * 이 메서드는 비동기 @Async로 동작하여 Redis 와의 통신 지연이
	 * 호출 측의 트랜잭션 시간에 영향을 주지 않도록 설계되었습니다.
	 * 발행된 메시지는 분산 환경의 모든 서버 노드에 전달되어,
	 * 수신 대상자가 접속 중인 서버에서 SSE를 통해 전송됩니다.
	 *
	 * @param dto Redis 채널을 통해 전달할 알림 응답 데이터 객체
	 */
	@Async
	public void publish(NotificationResponseDto dto) {
		notificationRedisTemplate.convertAndSend("notificationChannel", dto);
		log.info("Redis Pub/Sub 발행 완료: 유저ID {}", dto.getUserId());
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
