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
import org.springframework.data.redis.core.StringRedisTemplate;

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

