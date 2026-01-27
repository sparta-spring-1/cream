package com.sparta.cream.controller;

import com.sparta.cream.dto.auth.LoginRequestDto;
import com.sparta.cream.dto.auth.LoginResponseDto;
import com.sparta.cream.dto.auth.ReissueResponseDto;
import com.sparta.cream.dto.auth.SignupRequestDto;
import com.sparta.cream.dto.auth.SignupResponseDto;
import com.sparta.cream.service.AuthService;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API를 처리하는 컨트롤러
 * 회원가입 및 로그인 기능을 제공합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원가입 API
	 * 이메일, 비밀번호, 이름을 입력받아 새로운 사용자를 등록합니다.
	 * 비밀번호는 BCrypt로 암호화되어 저장됩니다.
	 * 기본 역할은 USER로 설정됩니다.
	 *
	 * @param request 회원가입 요청 DTO (이메일, 비밀번호, 이름)
	 * @return 회원가입 성공 응답 (201 Created)
	 */
	@PostMapping("/v1/auth/signup")
	public ResponseEntity<SignupResponseDto> signup(
		@RequestBody @Valid SignupRequestDto request) {
		SignupResponseDto response = authService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 로그인 API
	 * 이메일과 비밀번호를 검증하고 JWT 토큰을 발급합니다.
	 * Access Token은 응답 body에, Refresh Token은 HttpOnly Cookie로 전달됩니다.
	 *
	 * @param request 로그인 요청 DTO (이메일, 비밀번호)
	 * @return 로그인 성공 응답 (200 OK) 및 Refresh Token Cookie
	 */
	@PostMapping("/v1/auth/login")
	public ResponseEntity<LoginResponseDto> login(
		@RequestBody @Valid LoginRequestDto request) {
		AuthService.LoginResult result = authService.login(request);

		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
			.httpOnly(true)
			.secure(false) // 로컬 개발 환경에서는 false
			.sameSite("Strict")
			.path("/v1/auth/reissue")
			.maxAge(Duration.ofSeconds(result.getRefreshExpSec()))
			.build();

		LoginResponseDto response = result.getResponse();
		return ResponseEntity.status(HttpStatus.OK)
			.header("Set-Cookie", refreshCookie.toString())
			.body(response);
	}

	/**
	 * 토큰 재발급 API
	 * Refresh Token을 검증하고 새로운 Access Token을 발급합니다.
	 * Refresh Token은 Cookie에서 전달받습니다.
	 *
	 * @param refreshToken Cookie에서 전달받은 Refresh Token
	 * @return 재발급 성공 응답 (200 OK) 및 새 Access Token
	 */
	@PostMapping("/v1/auth/reissue")
	public ResponseEntity<ReissueResponseDto> reissue(
		@CookieValue(value = "refreshToken", required = false) String refreshToken) {
		if (refreshToken == null || refreshToken.isEmpty()) {
			throw new com.sparta.cream.exception.BusinessException(
				com.sparta.cream.exception.ErrorCode.AUTH_LOGIN_FAILED);
		}

		ReissueResponseDto response = authService.reissue(refreshToken);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
