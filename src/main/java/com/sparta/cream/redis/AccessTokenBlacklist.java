package com.sparta.cream.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Access Token 블랙리스트를 Redis에 저장하고 관리하는 클래스
 * 로그아웃된 Access Token을 블랙리스트에 등록하여 만료 전까지 사용을 차단합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Component
public class AccessTokenBlacklist {

	private final StringRedisTemplate redis;

	/**
	 * AccessTokenBlacklist 생성자
	 *
	 * @param redis Redis 템플릿
	 */
	public AccessTokenBlacklist(StringRedisTemplate redis) {
		this.redis = redis;
	}

	/**
	 * Redis 키 생성
	 *
	 * @param accessToken Access Token 문자열
	 * @return Redis 키 문자열 ("blacklist:{accessToken}")
	 */
	private String key(String accessToken) {
		return "blacklist:" + accessToken;
	}

	/**
	 * Access Token을 블랙리스트에 등록
	 * TTL(Time To Live)을 설정하여 토큰의 남은 만료 시간만큼 블랙리스트에 유지합니다.
	 *
	 * @param accessToken 블랙리스트에 등록할 Access Token
	 * @param ttl 만료 시간 (토큰의 남은 만료 시간)
	 */
	public void add(String accessToken, Duration ttl) {
		redis.opsForValue().set(key(accessToken), "blacklisted", ttl);
	}

	/**
	 * Access Token이 블랙리스트에 있는지 확인
	 *
	 * @param accessToken 확인할 Access Token
	 * @return 블랙리스트에 있으면 true, 없으면 false
	 */
	public boolean isBlacklisted(String accessToken) {
		return Boolean.TRUE.equals(redis.hasKey(key(accessToken)));
	}
}

