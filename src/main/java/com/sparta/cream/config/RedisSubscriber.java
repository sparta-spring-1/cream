package com.sparta.cream.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cream.domain.notification.dto.NotificationResponseDto;
import com.sparta.cream.domain.notification.controller.SseEmitters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 채널을 통해 방송되는 알림 메시지를 수신하는 리스너 클래스입니다.
 * 분산 서버 환경에서 각 서버 노드는 본 리스너를 통해 실시간 알림 이벤트를 공유받습니다.
 * 수신된 메시지는 해당 사용자가 현재 이 서버 인스턴스에 연결(SSE)되어 있는지 확인한 후,
 * 연결이 확인되면 실시간으로 데이터를 전송합니다.
 * RedisSubscriber.java
 *
 * @author kimsehyun
 * @since 2026. 02. 12.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final SseEmitters sseEmitters;
	private final RedisTemplate<String, NotificationResponseDto> notificationRedisTemplate;

	/**
	 * Redis 채널에서 메시지가 발행(Publish)되었을 때 자동으로 호출되는 콜백 메서드입니다.
	 * @param message Redis로부터 수신된 원시 메시
	 * @param pattern 구독 패턴
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			System.out.println(message.toString());
			Object obj = notificationRedisTemplate.getValueSerializer().deserialize(message.getBody());

			NotificationResponseDto dto = objectMapper.convertValue(obj, NotificationResponseDto.class);

			sseEmitters.sendToUser(dto.getUserId(), dto);
			log.info("클라이언트로 SSE 발송 성공: 유저ID {}", dto.getUserId());
		} catch (Exception e) {
			log.error("알림 발송 실패: {}", e.getMessage(), e);
		}
	}
}


