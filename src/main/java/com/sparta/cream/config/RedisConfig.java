package com.sparta.cream.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sparta.cream.domain.notification.dto.NotificationResponseDto;

/**
 * Redis 설정 클래스
 * Redis 연결 팩토리와 StringRedisTemplate을 설정합니다.
 * Refresh Token 저장에 사용됩니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	/**
	 * RedissonClient Bean 등록
	 * Redisson은 분산 환경에서의 락, 세마포어,
	 * 그리고 정렬된 셋 등을 구현할 때 사용됩니다.
	 * 특히 TradeService의 실시간 체결 로직에서 핵심적인 역할을 합니다.
	 *
	 * @return RedissonClient 인스턴스
	 */
	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		String address = "redis://" + host + ":" + port;

		config.useSingleServer()
			.setAddress(address)
			.setPassword(password.isEmpty() ? null : password);

		return Redisson.create(config);
	}

	/**
	 * Redis 메시지 리스너 컨테이너 설정
	 * Redis의 pup/sup 기능을 이용하여 notificationChannel로부터부터
	 * 발생되는 메시지를 비동기적으로 수신하는 컨테이너를 관리합니다.
	 * @param connectionFactory Redis 연결 팩토리
	 * @param listenerAdapter 메시지를 처리할 리스터 어뎁터
	 * @return RedisMessageListenerContainer 인스턴스
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new ChannelTopic("notificationChannel"));
		return container;
	}

	/**
	 * Redis 메시지 리스너 어댑터 설정
	 * 수신된 Redis 메시지를 실제 비즈니스 로직을 수행할 subscriber 객체와 연결합니다.
	 * @param subscriber 메시지 수신 로직을 수행할 RedisSubscriber 객체
	 * @return MessageListenerAdapter 인스턴스
	 */
	@Bean
	public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
		return new MessageListenerAdapter(subscriber, "onMessage");
	}

	/**
	 * 알림 전용 RedisTemplate 설정
	 * SSE를 통한 실시간 알림 전송시 사용하는 템플릿입니다.
	 * 기존의 GenericJackson2JsonRedisSerializer 사용 시 발생하던 역직렬화 이슈를 해결하기 위해
	 * {@link NotificationResponseDto}에 특화된 {@link Jackson2JsonRedisSerializer}를 적용하였습니다.
	 * 주요설정
	 * - Java 8 날짜/시간 API 지원
	 * - 타임스탬프 형식의 날짜 저장 비활성화
	 * - 타입 명시를 통한 안정적인 역직렬화 보장
	 * @param connectionFactory Redis 연결 팩토리
	 * @return NotificationResponseDto 전용 RedisTemplate 인스턴스
	 */
	@Bean
	public RedisTemplate<String, NotificationResponseDto> notificationRedisTemplate(
		RedisConnectionFactory connectionFactory
	) {
		RedisTemplate<String, NotificationResponseDto> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<NotificationResponseDto> serializer =
			new Jackson2JsonRedisSerializer<>(objectMapper, NotificationResponseDto.class);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.afterPropertiesSet();

		return template;
	}

	/**
	 * Redis 연결 팩토리 Bean 등록
	 * 호스트, 포트, 비밀번호를 설정하여 Redis에 연결합니다.
	 *
	 * @return RedisConnectionFactory
	 */
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		config.setHostName(host);
		config.setPort(port);
		config.setPassword(password);
		return new LettuceConnectionFactory(config);
	}

	/**
	 * StringRedisTemplate Bean 등록
	 * 문자열 기반 Redis 작업을 위한 템플릿입니다.
	 *
	 * @param connectionFactory Redis 연결 팩토리
	 * @return StringRedisTemplate
	 */
	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}
}

