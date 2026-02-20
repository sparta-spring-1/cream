package com.sparta.cream.domain.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자별 SSE(Server-Sent Events) 연결을 관리하고 메시지 발송을 담당하는 컴포넌트입니다.
 * 서버와 클라이언트 간의 지속적인 HTTP 연결 객체인 {@link SseEmitter}를 메모리에 유지하며,
 * 특정 사용자에게 실시간 알림을 직접 전송하는 기능을 수행합니다.
 * SseEmitters.java
 *
 * @author kimsehyun
 * @since 2026. 02. 12.
 */
@Component
@Slf4j
public class SseEmitters {

	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	/**
	 * 새로운 사용자 연결을 등록하고 관리 대상에 추가합니다.
	 * 30분의 타임아웃 설정을 가진 Emitter를 생성하며, 연결 만료/에러 발생 시
	 * 저장소에서 자동으로 삭제되도록 콜백 핸들러를 등록합니다.
	 *
	 * @param userId 연결할 사용자의 식별자
	 * @return 생성된 {@link SseEmitter} 객체
	 */
	public SseEmitter add(Long userId) {
		SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
		this.emitters.put(userId, emitter);
		log.info(">>> [SSE 연결 성공] 유저 ID: {}, 현재 연결된 총 인원: {}", userId, emitters.size()); // 이 로그 추가
		emitter.onCompletion(() -> this.emitters.remove(userId));
		emitter.onTimeout(() -> this.emitters.remove(userId));
		emitter.onError((e) -> this.emitters.remove(userId));

		try {
			emitter.send(SseEmitter.event()
				.name("connect")
				.data("connected!"));
		} catch (IOException e) {
			log.error("SSE 연결 알림 전송 실패: {}", userId);
		}

		return emitter;
	}

	/**
	 * 접속 중인 특정 사용자에게 실시간 데이터를 전송합니다.
	 * 저장소에서 사용자 ID에 해당하는 Emitter를 찾아 데이터를 발송합니다.
	 * 만약 네트워크 오류 등으로 발송에 실패할 경우, 즉시 해당 Emitter를 제거합니다.
	 *
	 * @param userId 수신 대상 사용자 식별자
	 * @param data   전송할 알림 데이터 객체
	 */
	public void sendToUser(Long userId, Object data) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name("notification")
					.data(data));
			} catch (IOException e) {
				log.error("SSE 전송 실패, 연결 제거: {}", userId);
				emitters.remove(userId);
			}
		}
	}
}
