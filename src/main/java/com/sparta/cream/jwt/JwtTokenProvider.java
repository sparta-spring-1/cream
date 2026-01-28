package com.sparta.cream.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;

/**
 * JWT 토큰 생성 및 관리 클래스
 * Access Token과 Refresh Token을 생성합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
public class JwtTokenProvider {

	private final SecretKey key;
	private final JwtProperties props;

	/**
	 * JwtTokenProvider 생성자
	 *
	 * @param props JWT 설정 프로퍼티
	 */
	public JwtTokenProvider(JwtProperties props) {
		this.props = props;
		this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Access Token 생성
	 * 사용자 ID를 기반으로 JWT Access Token을 생성합니다.
	 * 토큰에는 issuer(iss), subject(sub), JWT ID(jti), issued at(iat), expiration(exp) 클레임이 포함됩니다.
	 *
	 * @param userId 사용자 ID
	 * @return JWT Access Token 문자열
	 */
	public String createAccessToken(Long userId) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(props.accessExpSec());

		return Jwts.builder()
			.claim("iss", props.issuer())
			.claim("sub", String.valueOf(userId))
			.claim("jti", UUID.randomUUID().toString())
			.claim("iat", now.getEpochSecond())
			.claim("exp", exp.getEpochSecond())
			.signWith(key)
			.compact();
	}

	/**
	 * Refresh Token 생성
	 * 사용자 ID를 기반으로 JWT Refresh Token을 생성합니다.
	 * Access Token보다 긴 만료 시간을 가집니다.
	 *
	 * @param userId 사용자 ID
	 * @return JWT Refresh Token 문자열
	 */
	public String createRefreshToken(Long userId) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(props.refreshExpSec());

		return Jwts.builder()
			.claim("iss", props.issuer())
			.claim("sub", String.valueOf(userId))
			.claim("jti", UUID.randomUUID().toString())
			.claim("iat", now.getEpochSecond())
			.claim("exp", exp.getEpochSecond())
			.signWith(key)
			.compact();
	}
}

