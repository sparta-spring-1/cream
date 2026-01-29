package com.sparta.cream.service;

import com.sparta.cream.dto.user.MeResponseDto;
import com.sparta.cream.entity.Users;
import com.sparta.cream.exception.BusinessException;
import com.sparta.cream.exception.ErrorCode;
import com.sparta.cream.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지(내 정보) 관련 비즈니스 로직을 처리합니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class MyPageService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public MeResponseDto getMe(Long userId) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		return new MeResponseDto(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}


