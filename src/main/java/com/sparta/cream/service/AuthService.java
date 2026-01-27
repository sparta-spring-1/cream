package com.sparta.cream.service;

import com.sparta.cream.dto.auth.LoginRequestDto;
import com.sparta.cream.dto.auth.LoginResponseDto;
import com.sparta.cream.dto.auth.ReissueResponseDto;
import com.sparta.cream.dto.auth.SignupRequestDto;
import com.sparta.cream.dto.auth.SignupResponseDto;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.jwt.JwtProperties;
import com.sparta.cream.jwt.JwtTokenProvider;
import com.sparta.cream.redis.RefreshTokenStore;
import com.sparta.cream.repository.UserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 * 회원가입, 로그인, JWT 토큰 발급 등의 기능을 제공합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenStore refreshTokenStore;
	private final JwtProperties props;

	/**
	 * 회원가입 처리
	 * 이메일 중복 검증 후 비밀번호를 암호화하여 사용자를 저장합니다.
	 * 기본 역할은 USER로 설정됩니다.
	 *
	 * @param req 회원가입 요청 DTO
	 * @return 회원가입 응답 DTO (사용자 ID, 이메일, 이름, 역할, 생성일시)
	 * @throws BusinessException 이메일이 이미 존재하는 경우 AUTH_EMAIL_DUPLICATED 예외 발생
	 */
	@Transactional
	public SignupResponseDto signup(SignupRequestDto req) {
		if (userRepository.existsByEmail(req.getEmail())) {
			throw new BusinessException(ErrorCode.AUTH_EMAIL_DUPLICATED);
		}

		String encoded = passwordEncoder.encode(req.getPassword());
		Users user = userRepository.save(new Users(req.getEmail(), encoded, req.getName()));

		return new SignupResponseDto(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getRole(),
			user.getCreatedAt()
		);
	}

	/**
	 * 로그인 처리
	 * 이메일과 비밀번호를 검증하고 JWT Access Token과 Refresh Token을 발급합니다.
	 * Refresh Token은 Redis에 저장됩니다.
	 *
	 * @param req 로그인 요청 DTO
	 * @return 로그인 결과 (응답 DTO, Refresh Token, 만료 시간)
	 * @throws BusinessException 사용자를 찾을 수 없거나 비밀번호가 일치하지 않는 경우 AUTH_LOGIN_FAILED 예외 발생
	 * @throws BusinessException Refresh Token 저장 실패 시 AUTH_REFRESH_STORE_FAILED 예외 발생
	 */
	@Transactional(readOnly = true)
	public LoginResult login(LoginRequestDto req) {
		Users user = userRepository.findByEmail(req.getEmail())
			.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_LOGIN_FAILED));

		if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
			throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
		}

		String access = jwtTokenProvider.createAccessToken(user.getId());
		String refresh = jwtTokenProvider.createRefreshToken(user.getId());

		try {
			refreshTokenStore.save(user.getId(), refresh, Duration.ofSeconds(props.refreshExpSec()));
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.AUTH_REFRESH_STORE_FAILED);
		}

		LoginResponseDto body = new LoginResponseDto(
			access,
			"Bearer",
			props.accessExpSec(),
			new LoginResponseDto.UserSummary(user.getId(), user.getEmail(), user.getName(), user.getRole())
		);

		return new LoginResult(body, refresh, props.refreshExpSec());
	}

	/**
	 * 토큰 재발급 처리
	 * Refresh Token을 검증하고 새로운 Access Token을 발급합니다.
	 * 전달된 Refresh Token과 Redis에 저장된 Refresh Token을 비교하여 검증합니다.
	 *
	 * @param refreshToken 전달된 Refresh Token
	 * @return 재발급 응답 DTO (새 Access Token, 토큰 타입, 만료 시간)
	 * @throws BusinessException Refresh Token이 없거나 유효하지 않은 경우 AUTH_LOGIN_FAILED 예외 발생
	 * @throws BusinessException 저장된 토큰과 불일치하는 경우 AUTH_LOGIN_FAILED 예외 발생
	 * @throws BusinessException Access Token 생성 실패 시 AUTH_TOKEN_GENERATION_FAILED 예외 발생
	 */
	@Transactional(readOnly = true)
	public ReissueResponseDto reissue(String refreshToken) {

		Long userId;
		try {
			userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
		}

		String storedToken = refreshTokenStore.get(userId);
		if (storedToken == null) {
			throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
		}

		if (!refreshToken.equals(storedToken)) {
			throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
		}

		String newAccessToken;
		try {
			newAccessToken = jwtTokenProvider.createAccessToken(userId);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.AUTH_TOKEN_GENERATION_FAILED);
		}

		return new ReissueResponseDto(
			newAccessToken,
			"Bearer",
			props.accessExpSec()
		);
	}

	/**
	 * 로그인 결과를 담는 클래스
	 *
	 * @author 오정빈
	 * @version 1.0
	 */
	@lombok.Getter
	@lombok.AllArgsConstructor
	public static class LoginResult {

		private final LoginResponseDto response;
		private final String refreshToken;
		private final long refreshExpSec;
	}
}

