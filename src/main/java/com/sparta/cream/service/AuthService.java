package com.sparta.cream.service;

import com.sparta.cream.dto.auth.SignupRequestDto;
import com.sparta.cream.dto.auth.SignupResponseDto;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;;
import com.sparta.cream.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 * 회원가입 기능을 제공합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

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

}

