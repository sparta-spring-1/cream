package com.sparta.cream.domain.notification.service;

import com.sparta.cream.domain.notification.dto.NotificationPageResponseDto;
import com.sparta.cream.domain.notification.dto.NotificationResponseDto;
import com.sparta.cream.domain.notification.entity.Notification;
import com.sparta.cream.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 조회 전용 서비스
 * 사용자 알림 목록 조회 기능을 제공합니다.
 * 기존 NotificationService와 분리하여 조회 기능만 담당합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class NotificationQueryService {

	private final NotificationRepository notificationRepository;

	/**
	 * 사용자의 알림 목록을 페이징하여 조회합니다.
	 * 생성일시 내림차순으로 정렬됩니다.
	 *
	 * @param userId 사용자 ID
	 * @param page 페이지 번호 (0부터 시작)
	 * @param size 페이지 크기
	 * @return 페이징된 알림 목록 응답 DTO
	 */
	@Transactional(readOnly = true)
	public NotificationPageResponseDto getNotifications(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Notification> notificationPage = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

		List<NotificationResponseDto> items = notificationPage.getContent().stream()
			.map(this::toResponseDto)
			.collect(Collectors.toList());

		return new NotificationPageResponseDto(
			items,
			page,
			size,
			notificationPage.hasNext()
		);
	}

	/**
	 * Notification 엔티티를 NotificationResponseDto로 변환합니다.
	 *
	 * @param notification 알림 엔티티
	 * @return 알림 응답 DTO
	 */
	private NotificationResponseDto toResponseDto(Notification notification) {
		return new NotificationResponseDto(notification);
	}
}

