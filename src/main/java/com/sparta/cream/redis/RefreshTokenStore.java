package com.sparta.cream.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Refresh Token을 Redis에 저장하고 관리하는 클래스
 * 사용자 ID를 키로 하여 Refresh Token을 저장합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Component
public class RefreshTokenStore {

	private final StringRedisTemplate redis;

	/**
	 * RefreshTokenStore 생성자
	 *
	 * @param redis Redis 템플릿
	 */
	public RefreshTokenStore(StringRedisTemplate redis) {
		this.redis = redis;
	}

	/**
	 * Redis 키 생성
	 *
	 * @param userId 사용자 ID
	 * @return Redis 키 문자열 ("refresh:{userId}")
	 */
	private String key(Long userId) {
		return "refresh:" + userId;
	}

	/**
	 * Refresh Token 저장
	 * 사용자 ID를 키로 하여 Refresh Token을 Redis에 저장합니다.
	 *
	 * @param userId 사용자 ID
	 * @param refreshToken Refresh Token 문자열
	 * @param ttl 만료 시간
	 */
	public void save(Long userId, String refreshToken, Duration ttl) {
		redis.opsForValue().set(key(userId), refreshToken, ttl);
	}

	/**
	 * Refresh Token 조회
	 *
	 * @param userId 사용자 ID
	 * @return 저장된 Refresh Token, 없으면 null
	 */
	public String get(Long userId) {
		return redis.opsForValue().get(key(userId));
	}

	/**
	 * Refresh Token 삭제
	 *
	 * @param userId 사용자 ID
	 */
	public void delete(Long userId) {
		redis.delete(key(userId));
	}
}

