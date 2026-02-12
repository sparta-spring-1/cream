package com.sparta.cream.dto.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 마이페이지(내 정보 조회) 응답 DTO 입니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class MeResponseDto {
	private final Long id;
	private final String email;
	private final String name;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
}


