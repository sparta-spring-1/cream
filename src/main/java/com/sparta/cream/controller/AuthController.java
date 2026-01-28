package com.sparta.cream.controller;

import com.sparta.cream.dto.auth.SignupRequestDto;
import com.sparta.cream.dto.auth.SignupResponseDto;
import com.sparta.cream.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API를 처리하는 컨트롤러
 * 회원가입 기능을 제공합니다.
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
}
