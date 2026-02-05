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

	/**
	 * JWT 토큰에서 사용자 ID 추출
	 * 토큰의 subject(sub) 클레임에서 사용자 ID를 추출합니다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 사용자 ID
	 * @throws io.jsonwebtoken.JwtException 토큰 파싱 실패 시 예외 발생
	 */
	public Long getUserIdFromToken(String token) {
		String subject = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("sub", String.class);
		return Long.parseLong(subject);
	}

	/**
	 * JWT 토큰에서 만료 시간 추출
	 * 토큰의 expiration(exp) 클레임에서 만료 시간을 추출합니다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 만료 시간 (Instant)
	 * @throws io.jsonwebtoken.JwtException 토큰 파싱 실패 시 예외 발생
	 */
	public Instant getExpirationFromToken(String token) {
		Long exp = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("exp", Long.class);
		return Instant.ofEpochSecond(exp);
	}

	/**
	 * JWT 토큰에서 사용자 ID 추출 (만료 검증 무시)
	 * 로그아웃 등 만료된 토큰도 처리해야 하는 경우에 사용합니다.
	 * 서명 검증은 수행하지만 만료 시간 검증은 건너뜁니다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 사용자 ID
	 * @throws io.jsonwebtoken.JwtException 토큰 파싱 실패 시 예외 발생
	 */
	public Long getUserIdFromTokenIgnoringExpiration(String token) {
		String subject = Jwts.parserBuilder()
			.setSigningKey(key)
			.setAllowedClockSkewSeconds(Long.MAX_VALUE / 1000) // 만료 검증 무시
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("sub", String.class);
		return Long.parseLong(subject);
	}

	/**
	 * JWT 토큰에서 만료 시간 추출 (만료 검증 무시)
	 * 로그아웃 등 만료된 토큰도 처리해야 하는 경우에 사용합니다.
	 * 서명 검증은 수행하지만 만료 시간 검증은 건너뜁니다.
	 *
	 * @param token JWT 토큰 문자열
	 * @return 만료 시간 (Instant)
	 * @throws io.jsonwebtoken.JwtException 토큰 파싱 실패 시 예외 발생
	 */
	public Instant getExpirationFromTokenIgnoringExpiration(String token) {
		Long exp = Jwts.parserBuilder()
			.setSigningKey(key)
			.setAllowedClockSkewSeconds(Long.MAX_VALUE / 1000) // 만료 검증 무시
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("exp", Long.class);
		return Instant.ofEpochSecond(exp);
	}
}

