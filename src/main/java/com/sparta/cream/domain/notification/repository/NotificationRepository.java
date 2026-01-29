package com.sparta.cream.domain.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sparta.cream.domain.notification.entity.Notification;

/**
 * Notification 엔티티에 대한 데이터 접근 기능을 제공하는 리포지토리 인터페이스입니다.
 * 사용자별 알림 데이터 저장, 미발송 알림 조회등 알림 서비스에 필요한
 * 데이터베이스 연동 작업을 처리합니다.
 * NotificationRepository.java
 *
 * @author kimsehyun
 * @since 2026. 1. 29.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	/**
	 * 시스템에서 아직 전송하지 않은 모든 알림 목록을 조회합니다.
	 * 주기적으로 미발송 알림을 수집하여 처리하기 위해 사용됩니다.
	 * @return 전송 대기 상태인 알림 리스트
	 */
	List<Notification> findAllByIsSentFalse();
}
